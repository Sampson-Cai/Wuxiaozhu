package com.ruoyi.web.app.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.PurchaseOrder;
import com.ruoyi.business.mapper.PurchaseOrderMapper;
import com.ruoyi.business.mapper.ReceiptMapper;
import com.ruoyi.business.service.IPurchaseService;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.app.dto.PurchaseActionRequest;

/**
 * 采购单与单级审批
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/purchase")
public class AppPurchaseController extends AppBaseController
{
    @Autowired
    private IPurchaseService purchaseService;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private ReceiptMapper receiptMapper;

    @GetMapping("/list")
    public AppResult list(PurchaseOrder query)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        query.setEnterpriseId(eid);
        List<PurchaseOrder> list = purchaseOrderMapper.selectOrderList(query);
        return pageResult(list);
    }

    /**
     * 审批待办
     */
    @GetMapping("/todo")
    public AppResult todo()
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        PurchaseOrder query = new PurchaseOrder();
        query.setEnterpriseId(eid);
        query.setStatus("1");
        return pageResult(purchaseOrderMapper.selectOrderList(query));
    }

    @GetMapping("/{orderId}")
    public AppResult detail(@PathVariable Long orderId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        try
        {
            return AppResult.ok().put("data", purchaseService.detail(eid, orderId))
                    .put("receipt", receiptMapper.selectByOrder(eid, orderId));
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PostMapping
    @AppPermission("business:purchase:edit")
    public AppResult create(@RequestBody PurchaseOrder order)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        try
        {
            PurchaseOrder created = purchaseService.create(eid, operator(), order);
            return AppResult.ok("创建成功").put("orderId", created.getOrderId())
                    .put("orderNo", created.getOrderNo()).put("totalAmount", created.getTotalAmount());
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PutMapping("/{orderId}")
    @AppPermission("business:purchase:edit")
    public AppResult edit(@PathVariable Long orderId, @RequestBody PurchaseOrder order)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        order.setOrderId(orderId);
        try
        {
            purchaseService.update(eid, operator(), order);
            return AppResult.ok("保存成功");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PostMapping("/{orderId}/submit")
    @AppPermission("business:purchase:edit")
    public AppResult submit(@PathVariable Long orderId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        try
        {
            purchaseService.submit(eid, operator(), orderId);
            return AppResult.ok("已提交审批");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PostMapping("/{orderId}/approve")
    @AppPermission("business:purchase:approve")
    public AppResult approve(@PathVariable Long orderId, @RequestBody(required = false) PurchaseActionRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        String opinion = request == null ? null : request.getOpinion();
        try
        {
            purchaseService.approve(eid, userId(), operator(), orderId, opinion);
            return AppResult.ok("审批通过");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PostMapping("/{orderId}/reject")
    @AppPermission("business:purchase:approve")
    public AppResult reject(@PathVariable Long orderId, @RequestBody(required = false) PurchaseActionRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        String opinion = request == null ? null : request.getOpinion();
        try
        {
            purchaseService.reject(eid, userId(), operator(), orderId, opinion);
            return AppResult.ok("已驳回");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PostMapping("/{orderId}/cancel")
    @AppPermission("business:purchase:edit")
    public AppResult cancel(@PathVariable Long orderId, @RequestBody(required = false) PurchaseActionRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        String reason = request == null ? null : request.getReason();
        try
        {
            purchaseService.cancel(eid, operator(), orderId, reason);
            return AppResult.ok("已取消");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    /**
     * 删除草稿采购单
     *
     * 逻辑删除，仅 status=0 可删；控制器方法名沿用 remove，与 category/product/supplier 一致。
     */
    @DeleteMapping("/{orderId}")
    @AppPermission("business:purchase:edit")
    public AppResult remove(@PathVariable Long orderId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        try
        {
            purchaseService.delete(eid, operator(), orderId);
            return AppResult.ok("删除成功");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PostMapping("/{orderId}/receive")
    @AppPermission("business:stock:edit")
    public AppResult receive(@PathVariable Long orderId, @RequestBody(required = false) PurchaseActionRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        String remark = request == null ? null : request.getRemark();
        try
        {
            purchaseService.receive(eid, userId(), operator(), orderId,
                    request == null ? null : request.getItems(), remark);
            return AppResult.ok("收货完成");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }
}
