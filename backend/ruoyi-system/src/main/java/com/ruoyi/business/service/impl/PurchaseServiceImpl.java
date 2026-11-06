package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.Payable;
import com.ruoyi.business.domain.Product;
import com.ruoyi.business.domain.PurchaseApproval;
import com.ruoyi.business.domain.PurchaseOrder;
import com.ruoyi.business.domain.PurchaseOrderItem;
import com.ruoyi.business.domain.Receipt;
import com.ruoyi.business.domain.ReceiptItem;
import com.ruoyi.business.domain.Supplier;
import com.ruoyi.business.mapper.PayableMapper;
import com.ruoyi.business.mapper.ProductMapper;
import com.ruoyi.business.mapper.PurchaseOrderMapper;
import com.ruoyi.business.mapper.ReceiptMapper;
import com.ruoyi.business.mapper.StockMapper;
import com.ruoyi.business.mapper.SupplierMapper;
import com.ruoyi.business.service.IPurchaseService;
import com.ruoyi.business.service.IStockService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 采购单业务实现
 *
 * 单级审批：草稿 → 待审批 → 已通过/已驳回 → 已完成；取消可从待审批或已通过进入。
 * 审批通过累加在途量，收货时冲减在途、增加库存并生成应付。
 *
 * @author cangxiaozhu
 */
@Service
public class PurchaseServiceImpl implements IPurchaseService
{
    private static final Random RANDOM = new Random();

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private ReceiptMapper receiptMapper;

    @Autowired
    private PayableMapper payableMapper;

    @Autowired
    private IStockService stockService;

    @Override
    public PurchaseOrder detail(Long enterpriseId, Long orderId)
    {
        PurchaseOrder order = purchaseOrderMapper.selectOrderById(enterpriseId, orderId);
        if (order == null)
        {
            throw new ServiceException("采购单不存在");
        }
        order.setItems(purchaseOrderMapper.selectItems(orderId));
        order.setApprovals(purchaseOrderMapper.selectApprovals(orderId));
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder create(Long enterpriseId, String operator, PurchaseOrder order)
    {
        validate(enterpriseId, order);
        order.setEnterpriseId(enterpriseId);
        order.setCreateBy(operator);
        order.setStatus("0");
        order.setOrderNo(nextNo("PO"));
        purchaseOrderMapper.insertOrder(order);
        saveItems(enterpriseId, order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long enterpriseId, String operator, PurchaseOrder order)
    {
        PurchaseOrder exists = requireOrder(enterpriseId, order.getOrderId());
        if (!"0".equals(exists.getStatus()) && !"3".equals(exists.getStatus()))
        {
            throw new ServiceException("仅草稿或已驳回的采购单可以编辑");
        }
        validate(enterpriseId, order);
        order.setEnterpriseId(enterpriseId);
        order.setUpdateBy(operator);
        if (purchaseOrderMapper.updateDraft(order) == 0)
        {
            throw new ServiceException("采购单状态已变化，请刷新后重试");
        }
        purchaseOrderMapper.deleteItems(order.getOrderId());
        order.setOrderId(exists.getOrderId());
        saveItems(enterpriseId, order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long enterpriseId, String operator, Long orderId)
    {
        PurchaseOrder order = requireOrder(enterpriseId, orderId);
        if (!"0".equals(order.getStatus()) && !"3".equals(order.getStatus()))
        {
            throw new ServiceException("只有草稿或已驳回的采购单可以提交");
        }
        if (purchaseOrderMapper.selectItems(orderId).isEmpty())
        {
            throw new ServiceException("采购单没有明细，无法提交");
        }
        if (purchaseOrderMapper.submitOrder(enterpriseId, orderId, operator) == 0)
        {
            throw new ServiceException("提交失败，采购单状态已变化");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long enterpriseId, Long userId, String operator, Long orderId, String opinion)
    {
        PurchaseOrder order = requireOrder(enterpriseId, orderId);
        if (!"1".equals(order.getStatus()))
        {
            throw new ServiceException("只有待审批的采购单可以审批");
        }
        // 管理员可代审并留痕；普通审批人不得审批自己创建的单据
        if (userId != null && userId != 1L && operator != null && operator.equals(order.getCreateBy()))
        {
            throw new ServiceException("不能审批自己创建的单据");
        }
        if (purchaseOrderMapper.approveOrder(enterpriseId, orderId, operator) == 0)
        {
            throw new ServiceException("审批失败，采购单状态已变化");
        }
        saveApproval(enterpriseId, orderId, userId, operator, "1", opinion);

        // 审批通过后进入在途，避免补货计算重复下单
        for (PurchaseOrderItem item : purchaseOrderMapper.selectItems(orderId))
        {
            stockMapper.addInTransit(enterpriseId, item.getProductId(), item.getQty());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long enterpriseId, Long userId, String operator, Long orderId, String opinion)
    {
        if (StringUtils.isEmpty(opinion))
        {
            throw new ServiceException("驳回必须填写审批意见");
        }
        PurchaseOrder order = requireOrder(enterpriseId, orderId);
        if (!"1".equals(order.getStatus()))
        {
            throw new ServiceException("只有待审批的采购单可以驳回");
        }
        // 管理员可代审并留痕；普通审批人不得审批自己创建的单据
        if (userId != null && userId != 1L && operator != null && operator.equals(order.getCreateBy()))
        {
            throw new ServiceException("不能审批自己创建的单据");
        }
        if (purchaseOrderMapper.rejectOrder(enterpriseId, orderId, operator) == 0)
        {
            throw new ServiceException("驳回失败，采购单状态已变化");
        }
        saveApproval(enterpriseId, orderId, userId, operator, "2", opinion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long enterpriseId, String operator, Long orderId, String reason)
    {
        if (StringUtils.isEmpty(reason))
        {
            throw new ServiceException("取消必须填写原因");
        }
        PurchaseOrder order = requireOrder(enterpriseId, orderId);
        boolean approved = "2".equals(order.getStatus());
        if (!"1".equals(order.getStatus()) && !approved)
        {
            throw new ServiceException("只有待审批或已通过的采购单可以取消");
        }
        List<PurchaseOrderItem> items = purchaseOrderMapper.selectItems(orderId);
        if (approved)
        {
            // 已部分收货的单据取消会让在途量与实收库存对不上，直接拒绝
            for (PurchaseOrderItem item : items)
            {
                BigDecimal received = item.getReceivedQty() == null ? BigDecimal.ZERO : item.getReceivedQty();
                if (received.compareTo(BigDecimal.ZERO) > 0)
                {
                    throw new ServiceException("该采购单已收货，不能取消");
                }
            }
        }
        if (purchaseOrderMapper.cancelOrder(enterpriseId, orderId, reason, operator) == 0)
        {
            throw new ServiceException("取消失败，采购单状态已变化");
        }
        if (approved)
        {
            for (PurchaseOrderItem item : items)
            {
                stockMapper.subtractInTransit(enterpriseId, item.getProductId(), item.getQty());
            }
        }
    }

    /**
     * 草稿作废：仅未提交的采购单可以逻辑删除，明细随主单一起隐藏，不做物理删除。
     *
     * 业务动作用动词原形 delete，与 submit/approve/cancel/receive 并列。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long enterpriseId, String operator, Long orderId)
    {
        PurchaseOrder order = requireOrder(enterpriseId, orderId);
        if (!"0".equals(order.getStatus()))
        {
            throw new ServiceException("仅草稿可以删除，已提交的单据请使用取消");
        }
        if (purchaseOrderMapper.deleteOrderById(enterpriseId, orderId, operator) == 0)
        {
            throw new ServiceException("删除失败，采购单状态已变化");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long enterpriseId, Long userId, String operator, Long orderId, String remark)
    {
        receive(enterpriseId, userId, operator, orderId, null, remark);
    }

    /**
     * 收货入库：receiveItems 为空表示按待收数量全部收货，否则按明细部分收货。
     *
     * 部分收货时采购单保持「已通过」，可继续收货；全部明细收完才置为「已完成」并生成应付账款，
     * 避免一张单生成多笔应付。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long enterpriseId, Long userId, String operator, Long orderId,
            List<PurchaseOrderItem> receiveItems, String remark)
    {
        PurchaseOrder order = requireOrder(enterpriseId, orderId);
        if (!"2".equals(order.getStatus()))
        {
            throw new ServiceException("只有已通过的采购单可以收货");
        }
        List<PurchaseOrderItem> items = purchaseOrderMapper.selectItems(orderId);
        if (items.isEmpty())
        {
            throw new ServiceException("采购单没有明细，无法收货");
        }

        boolean receiveAll = receiveItems == null || receiveItems.isEmpty();
        Map<Long, BigDecimal> requestQty = new LinkedHashMap<>();
        if (!receiveAll)
        {
            for (PurchaseOrderItem request : receiveItems)
            {
                if (request.getItemId() == null || request.getQty() == null
                        || request.getQty().compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("收货明细与数量不能为空，且数量必须大于 0");
                }
                requestQty.merge(request.getItemId(), request.getQty(), BigDecimal::add);
            }
        }

        // 计算本次收货计划
        Map<Long, BigDecimal> plan = new LinkedHashMap<>();
        BigDecimal totalQty = BigDecimal.ZERO;
        for (PurchaseOrderItem item : items)
        {
            BigDecimal received = item.getReceivedQty() == null ? BigDecimal.ZERO : item.getReceivedQty();
            BigDecimal pending = item.getQty().subtract(received);
            if (pending.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            BigDecimal qty = receiveAll ? pending : requestQty.get(item.getItemId());
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            if (qty.compareTo(pending) > 0)
            {
                throw new ServiceException("收货数量超过待收数量：" + item.getProductName());
            }
            plan.put(item.getItemId(), qty);
            totalQty = totalQty.add(qty);
        }
        if (plan.isEmpty())
        {
            throw new ServiceException("没有可收货的明细");
        }

        Receipt receipt = new Receipt();
        receipt.setEnterpriseId(enterpriseId);
        receipt.setReceiptNo(nextNo("RC"));
        receipt.setOrderId(orderId);
        receipt.setReceiptType("1");
        receipt.setTotalQty(totalQty);
        receipt.setReceiverId(userId);
        receipt.setReceiverName(operator);
        receipt.setRemark(remark);
        receiptMapper.insertReceipt(receipt);
        for (PurchaseOrderItem item : items)
        {
            BigDecimal qty = plan.get(item.getItemId());
            if (qty == null)
            {
                continue;
            }
            // 入库流水 + 库存增加
            stockService.inbound(enterpriseId, userId, operator, item.getProductId(), qty, "采购入库",
                    order.getOrderNo(), null, remark);
            purchaseOrderMapper.updateItemReceived(enterpriseId, item.getItemId(), qty);
            stockMapper.subtractInTransit(enterpriseId, item.getProductId(), qty);

            ReceiptItem receiptItem = new ReceiptItem();
            receiptItem.setEnterpriseId(enterpriseId);
            receiptItem.setReceiptId(receipt.getReceiptId());
            receiptItem.setOrderItemId(item.getItemId());
            receiptItem.setProductId(item.getProductId());
            receiptItem.setQty(qty);
            receiptItem.setPrice(item.getPrice());
            receiptItem.setAmount(item.getPrice().multiply(qty));
            receiptMapper.insertReceiptItem(receiptItem);
        }

        // 是否全部收完：以落库后的 received_qty 为准
        for (PurchaseOrderItem item : purchaseOrderMapper.selectItems(orderId))
        {
            BigDecimal received = item.getReceivedQty() == null ? BigDecimal.ZERO : item.getReceivedQty();
            if (received.compareTo(item.getQty()) < 0)
            {
                return;
            }
        }

        if (purchaseOrderMapper.completeOrder(enterpriseId, orderId, operator) == 0)
        {
            throw new ServiceException("收货失败，采购单状态已变化");
        }
        createPayable(enterpriseId, order);
    }

    /**
     * 采购单收完生成应付账款，到期日取供应商账期；同一采购单只生成一笔
     */
    private void createPayable(Long enterpriseId, PurchaseOrder order)
    {
        if (payableMapper.selectByOrder(enterpriseId, order.getOrderId()) != null)
        {
            return;
        }
        Supplier supplier = supplierMapper.selectSupplierById(enterpriseId, order.getSupplierId());
        int payTerm = supplier != null && supplier.getPayTermDays() != null ? supplier.getPayTermDays() : 30;
        Payable payable = new Payable();
        payable.setEnterpriseId(enterpriseId);
        payable.setPayableNo(nextNo("AP"));
        payable.setOrderId(order.getOrderId());
        payable.setSupplierId(order.getSupplierId());
        payable.setTotalAmount(order.getTotalAmount());
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setDueDate(new Date(System.currentTimeMillis() + payTerm * 86400000L));
        payable.setStatus("0");
        payable.setRemark("采购单 " + order.getOrderNo() + " 收货自动生成");
        payableMapper.insertPayable(payable);
    }

    private PurchaseOrder requireOrder(Long enterpriseId, Long orderId)
    {
        if (orderId == null)
        {
            throw new ServiceException("采购单ID不能为空");
        }
        PurchaseOrder order = purchaseOrderMapper.selectOrderById(enterpriseId, orderId);
        if (order == null)
        {
            throw new ServiceException("采购单不存在");
        }
        return order;
    }

    private void validate(Long enterpriseId, PurchaseOrder order)
    {
        if (order.getSupplierId() == null)
        {
            throw new ServiceException("请选择供应商");
        }
        Supplier supplier = supplierMapper.selectSupplierById(enterpriseId, order.getSupplierId());
        if (supplier == null)
        {
            throw new ServiceException("供应商不存在");
        }
        if (!"0".equals(supplier.getStatus()))
        {
            throw new ServiceException("供应商已停用");
        }
        if (order.getItems() == null || order.getItems().isEmpty())
        {
            throw new ServiceException("采购单至少需要一条明细");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItem item : order.getItems())
        {
            if (item.getQty() == null || item.getQty().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("明细数量必须大于 0");
            }
            Product product = productMapper.selectProductById(enterpriseId, item.getProductId());
            if (product == null)
            {
                throw new ServiceException("明细商品不存在");
            }
            if (!"0".equals(product.getStatus()))
            {
                throw new ServiceException("商品【" + product.getProductName() + "】已停用");
            }
            BigDecimal price = item.getPrice() == null ? product.getRefPrice() : item.getPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("单价不能为负数");
            }
            item.setPrice(price);
            item.setAmount(price.multiply(item.getQty()));
            item.setProductName(product.getProductName());
            item.setSpec(product.getSpec());
            item.setUnit(product.getUnit());
            total = total.add(item.getAmount());
        }
        order.setTotalAmount(total);
        order.setSupplierName(supplier.getSupplierName());
    }

    private void saveItems(Long enterpriseId, PurchaseOrder order)
    {
        for (PurchaseOrderItem item : order.getItems())
        {
            item.setEnterpriseId(enterpriseId);
            item.setOrderId(order.getOrderId());
            purchaseOrderMapper.insertItem(item);
        }
    }

    private void saveApproval(Long enterpriseId, Long orderId, Long userId, String operator, String result,
            String opinion)
    {
        PurchaseApproval approval = new PurchaseApproval();
        approval.setEnterpriseId(enterpriseId);
        approval.setOrderId(orderId);
        approval.setApproverId(userId);
        approval.setApproverName(operator);
        approval.setResult(result);
        approval.setOpinion(opinion);
        purchaseOrderMapper.insertApproval(approval);
    }

    private String nextNo(String prefix)
    {
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        return prefix + time + String.format("%03d", RANDOM.nextInt(1000));
    }
}
