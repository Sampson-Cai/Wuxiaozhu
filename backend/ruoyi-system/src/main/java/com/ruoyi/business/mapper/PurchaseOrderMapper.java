package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.PurchaseApproval;
import com.ruoyi.business.domain.PurchaseOrder;
import com.ruoyi.business.domain.PurchaseOrderItem;

/**
 * 采购单
 *
 * @author cangxiaozhu
 */
public interface PurchaseOrderMapper
{
    List<PurchaseOrder> selectOrderList(PurchaseOrder query);

    PurchaseOrder selectOrderById(@Param("enterpriseId") Long enterpriseId, @Param("orderId") Long orderId);

    List<PurchaseOrderItem> selectItems(@Param("orderId") Long orderId);

    PurchaseOrderItem selectItemById(@Param("enterpriseId") Long enterpriseId, @Param("itemId") Long itemId);

    List<PurchaseApproval> selectApprovals(@Param("orderId") Long orderId);

    int insertOrder(PurchaseOrder order);

    int updateDraft(PurchaseOrder order);

    int deleteItems(@Param("orderId") Long orderId);

    int insertItem(PurchaseOrderItem item);

    int insertApproval(PurchaseApproval approval);

    int submitOrder(@Param("enterpriseId") Long enterpriseId, @Param("orderId") Long orderId,
            @Param("submitBy") String submitBy);

    int approveOrder(@Param("enterpriseId") Long enterpriseId, @Param("orderId") Long orderId,
            @Param("approveBy") String approveBy);

    int rejectOrder(@Param("enterpriseId") Long enterpriseId, @Param("orderId") Long orderId,
            @Param("approveBy") String approveBy);

    int cancelOrder(@Param("enterpriseId") Long enterpriseId, @Param("orderId") Long orderId,
            @Param("cancelReason") String cancelReason, @Param("updateBy") String updateBy);

    /**
     * 草稿作废：逻辑删除未提交的采购单
     *
     * 命名规则：逻辑删除统一 deleteXxxById；条件必须同时带 status 与 del_flag，
     * 见 doc/开发规范.md 3.3。
     */
    int deleteOrderById(@Param("enterpriseId") Long enterpriseId, @Param("orderId") Long orderId,
            @Param("updateBy") String updateBy);

    int completeOrder(@Param("enterpriseId") Long enterpriseId, @Param("orderId") Long orderId,
            @Param("receiveBy") String receiveBy);

    int updateItemReceived(@Param("enterpriseId") Long enterpriseId, @Param("itemId") Long itemId,
            @Param("qty") BigDecimal qty);
}
