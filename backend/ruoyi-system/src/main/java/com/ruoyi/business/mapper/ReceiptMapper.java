package com.ruoyi.business.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Receipt;
import com.ruoyi.business.domain.ReceiptItem;

/**
 * 收货单
 *
 * @author cangxiaozhu
 */
public interface ReceiptMapper
{
    int insertReceipt(Receipt receipt);

    int insertReceiptItem(ReceiptItem item);

    Receipt selectByOrder(@Param("enterpriseId") Long enterpriseId, @Param("orderId") Long orderId);

    java.util.List<Receipt> selectReceiptList(@Param("enterpriseId") Long enterpriseId,
            @Param("orderId") Long orderId);

    Receipt selectReceiptById(@Param("enterpriseId") Long enterpriseId, @Param("receiptId") Long receiptId);

    java.util.List<ReceiptItem> selectItems(@Param("receiptId") Long receiptId);
}
