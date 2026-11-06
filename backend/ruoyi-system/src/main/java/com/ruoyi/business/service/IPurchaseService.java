package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.PurchaseOrder;
import com.ruoyi.business.domain.PurchaseOrderItem;

/**
 * 采购单业务
 *
 * @author cangxiaozhu
 */
public interface IPurchaseService
{
    PurchaseOrder detail(Long enterpriseId, Long orderId);

    PurchaseOrder create(Long enterpriseId, String operator, PurchaseOrder order);

    void update(Long enterpriseId, String operator, PurchaseOrder order);

    void submit(Long enterpriseId, String operator, Long orderId);

    void approve(Long enterpriseId, Long userId, String operator, Long orderId, String opinion);

    void reject(Long enterpriseId, Long userId, String operator, Long orderId, String opinion);

    void cancel(Long enterpriseId, String operator, Long orderId, String reason);

    /**
     * 删除草稿采购单（逻辑删除），仅 status=0 可删
     */
    void delete(Long enterpriseId, String operator, Long orderId);

    void receive(Long enterpriseId, Long userId, String operator, Long orderId, String remark);

    /**
     * 分批收货：receiveItems 为空表示全部收货；全部收完才置为已完成并生成应付
     */
    void receive(Long enterpriseId, Long userId, String operator, Long orderId,
            List<PurchaseOrderItem> receiveItems, String remark);
}
