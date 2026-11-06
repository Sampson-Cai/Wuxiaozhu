package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.PaymentRecord;

/**
 * 付款记录
 *
 * @author cangxiaozhu
 */
public interface PaymentRecordMapper
{
    int insertPayment(PaymentRecord payment);

    List<PaymentRecord> selectByPayable(@Param("enterpriseId") Long enterpriseId,
            @Param("payableId") Long payableId);
}
