package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Payable;

/**
 * 应付账款
 *
 * @author cangxiaozhu
 */
public interface PayableMapper
{
    int insertPayable(Payable payable);

    Payable selectByOrder(@Param("enterpriseId") Long enterpriseId, @Param("orderId") Long orderId);

    Payable selectPayableById(@Param("enterpriseId") Long enterpriseId, @Param("payableId") Long payableId);

    List<Payable> selectPayableList(Payable query);

    int updatePaidAmount(@Param("enterpriseId") Long enterpriseId, @Param("payableId") Long payableId,
            @Param("paidAmount") BigDecimal paidAmount, @Param("status") String status);
}
