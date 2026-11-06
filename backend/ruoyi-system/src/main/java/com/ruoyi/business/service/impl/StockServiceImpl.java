package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.Product;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.domain.StockRecord;
import com.ruoyi.business.mapper.ProductMapper;
import com.ruoyi.business.mapper.StockMapper;
import com.ruoyi.business.mapper.StockRecordMapper;
import com.ruoyi.business.service.IStockService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 库存业务实现
 *
 * 出入库统一走「行锁 + 条件更新」，保证并发下不会出现负库存。
 *
 * @author cangxiaozhu
 */
@Service
public class StockServiceImpl implements IStockService
{
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inbound(Long enterpriseId, Long operatorId, String operatorName, Long productId, BigDecimal qty,
            String bizType, String sourceNo, String batchNo, String remark)
    {
        checkQty(qty);
        checkProduct(enterpriseId, productId);

        Stock stock = stockMapper.selectStockForUpdate(enterpriseId, productId);
        BigDecimal before = stock == null ? BigDecimal.ZERO : stock.getQty();
        if (stock == null)
        {
            Stock row = new Stock();
            row.setEnterpriseId(enterpriseId);
            row.setProductId(productId);
            row.setQty(qty);
            stockMapper.insertStock(row);
        }
        else if (stockMapper.addStockQty(enterpriseId, productId, qty) == 0)
        {
            throw new ServiceException("库存更新失败，请重试");
        }

        saveRecord(enterpriseId, operatorId, operatorName, productId, "1", bizType, qty, before,
                before.add(qty), sourceNo, batchNo, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outbound(Long enterpriseId, Long operatorId, String operatorName, Long productId, BigDecimal qty,
            String bizType, String remark)
    {
        checkQty(qty);
        checkProduct(enterpriseId, productId);

        Stock stock = stockMapper.selectStockForUpdate(enterpriseId, productId);
        if (stock == null || stock.getQty().compareTo(qty) < 0)
        {
            throw new ServiceException("库存不足，无法出库");
        }
        BigDecimal before = stock.getQty();
        if (stockMapper.subtractStockQty(enterpriseId, productId, qty) == 0)
        {
            throw new ServiceException("可用库存不足，无法出库");
        }

        saveRecord(enterpriseId, operatorId, operatorName, productId, "2", bizType, qty, before,
                before.subtract(qty), null, null, remark);
    }

    /**
     * 冲销手工流水：原入库生成出库、原出库生成入库，并回写原流水冲销标记。
     *
     * 单据来源流水（采购入库等）不允许直接冲销，避免库存与采购单、应付账款脱节。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reverse(Long enterpriseId, Long operatorId, String operatorName, Long recordId, String remark)
    {
        if (recordId == null)
        {
            throw new ServiceException("请选择要冲销的流水");
        }
        StockRecord origin = stockRecordMapper.selectRecordById(enterpriseId, recordId);
        if (origin == null)
        {
            throw new ServiceException("流水不存在");
        }
        if ("1".equals(origin.getReverseFlag()))
        {
            throw new ServiceException("该流水已冲销，不能重复冲销");
        }
        if (StringUtils.isNotEmpty(origin.getSourceNo()))
        {
            throw new ServiceException("单据来源的流水请通过原单据处理，不能直接冲销");
        }

        BigDecimal qty = origin.getQty();
        Stock stock = stockMapper.selectStockForUpdate(enterpriseId, origin.getProductId());
        BigDecimal before = stock == null ? BigDecimal.ZERO : stock.getQty();
        BigDecimal after;
        String reverseType;
        if ("1".equals(origin.getRecordType()))
        {
            // 原入库 → 冲销为出库，库存必须足够
            if (stock == null || stock.getQty().compareTo(qty) < 0)
            {
                throw new ServiceException("当前库存不足以冲销该入库流水");
            }
            if (stockMapper.subtractStockQty(enterpriseId, origin.getProductId(), qty) == 0)
            {
                throw new ServiceException("库存更新失败，请重试");
            }
            after = before.subtract(qty);
            reverseType = "2";
        }
        else
        {
            if (stock == null)
            {
                Stock row = new Stock();
                row.setEnterpriseId(enterpriseId);
                row.setProductId(origin.getProductId());
                row.setQty(qty);
                stockMapper.insertStock(row);
            }
            else if (stockMapper.addStockQty(enterpriseId, origin.getProductId(), qty) == 0)
            {
                throw new ServiceException("库存更新失败，请重试");
            }
            after = before.add(qty);
            reverseType = "1";
        }

        StockRecord reverse = new StockRecord();
        reverse.setEnterpriseId(enterpriseId);
        reverse.setProductId(origin.getProductId());
        reverse.setRecordType(reverseType);
        reverse.setBizType("冲销");
        reverse.setQty(qty);
        reverse.setBeforeQty(before);
        reverse.setAfterQty(after);
        reverse.setOperatorId(operatorId);
        reverse.setOperatorName(operatorName);
        reverse.setRemark(StringUtils.isEmpty(remark) ? "冲销流水#" + recordId : remark);
        reverse.setReverseFlag("0");
        stockRecordMapper.insertRecord(reverse);

        if (stockRecordMapper.markReversed(enterpriseId, recordId, reverse.getRecordId()) == 0)
        {
            throw new ServiceException("该流水已冲销，不能重复冲销");
        }
    }

    private void checkQty(BigDecimal qty)    {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("数量必须大于 0");
        }
    }

    private void checkProduct(Long enterpriseId, Long productId)
    {
        if (productId == null)
        {
            throw new ServiceException("请选择商品");
        }
        Product product = productMapper.selectProductById(enterpriseId, productId);
        if (product == null)
        {
            throw new ServiceException("商品不存在或已删除");
        }
        if (!"0".equals(product.getStatus()))
        {
            throw new ServiceException("商品已停用，不能出入库");
        }
    }

    private void saveRecord(Long enterpriseId, Long operatorId, String operatorName, Long productId,
            String recordType, String bizType, BigDecimal qty, BigDecimal before, BigDecimal after,
            String sourceNo, String batchNo, String remark)
    {
        StockRecord record = new StockRecord();
        record.setEnterpriseId(enterpriseId);
        record.setProductId(productId);
        record.setRecordType(recordType);
        record.setBizType(bizType);
        record.setQty(qty);
        record.setBeforeQty(before);
        record.setAfterQty(after);
        record.setSourceNo(sourceNo);
        record.setBatchNo(batchNo);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setRemark(remark);
        stockRecordMapper.insertRecord(record);
    }
}
