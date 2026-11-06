package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.Payable;

/**
 * 应付账款业务
 *
 * @author cangxiaozhu
 */
public interface IPayableService
{
    List<Payable> list(Payable query);

    Payable detail(Long enterpriseId, Long payableId);

    void pay(Long enterpriseId, Long userId, String operator, Long payableId, BigDecimal amount,
            String payMethod, String remark);
}
