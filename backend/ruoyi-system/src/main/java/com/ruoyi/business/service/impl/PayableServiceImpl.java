package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.Payable;
import com.ruoyi.business.domain.PaymentRecord;
import com.ruoyi.business.mapper.PayableMapper;
import com.ruoyi.business.mapper.PaymentRecordMapper;
import com.ruoyi.business.service.IPayableService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 应付账款业务实现
 *
 * @author cangxiaozhu
 */
@Service
public class PayableServiceImpl implements IPayableService
{
    @Autowired
    private PayableMapper payableMapper;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Override
    public List<Payable> list(Payable query)
    {
        List<Payable> list = payableMapper.selectPayableList(query);
        list.forEach(this::fill);
        return list;
    }

    @Override
    public Payable detail(Long enterpriseId, Long payableId)
    {
        Payable payable = payableMapper.selectPayableById(enterpriseId, payableId);
        if (payable == null)
        {
            throw new ServiceException("应付账款不存在");
        }
        fill(payable);
        return payable;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long enterpriseId, Long userId, String operator, Long payableId, BigDecimal amount,
            String payMethod, String remark)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("付款金额必须大于 0");
        }
        Payable payable = payableMapper.selectPayableById(enterpriseId, payableId);
        if (payable == null)
        {
            throw new ServiceException("应付账款不存在");
        }
        if ("2".equals(payable.getStatus()))
        {
            throw new ServiceException("该账款已结清");
        }
        BigDecimal paid = payable.getPaidAmount() == null ? BigDecimal.ZERO : payable.getPaidAmount();
        BigDecimal remaining = payable.getTotalAmount().subtract(paid);
        if (amount.compareTo(remaining) > 0)
        {
            throw new ServiceException("付款金额不能超过未付金额 " + remaining.toPlainString());
        }

        PaymentRecord record = new PaymentRecord();
        record.setEnterpriseId(enterpriseId);
        record.setPayableId(payableId);
        record.setAmount(amount);
        record.setPayMethod(payMethod);
        record.setOperatorId(userId);
        record.setOperatorName(operator);
        record.setRemark(remark);
        paymentRecordMapper.insertPayment(record);

        BigDecimal newPaid = paid.add(amount);
        String status = newPaid.compareTo(payable.getTotalAmount()) >= 0 ? "2" : "1";
        if (payableMapper.updatePaidAmount(enterpriseId, payableId, newPaid, status) == 0)
        {
            throw new ServiceException("付款失败，请刷新后重试");
        }
    }

    private void fill(Payable payable)
    {
        BigDecimal total = payable.getTotalAmount() == null ? BigDecimal.ZERO : payable.getTotalAmount();
        BigDecimal paid = payable.getPaidAmount() == null ? BigDecimal.ZERO : payable.getPaidAmount();
        payable.setRemainingAmount(total.subtract(paid));
        if ("2".equals(payable.getStatus()))
        {
            payable.setDueStatus("3");
            return;
        }
        if (payable.getDueDate() == null)
        {
            payable.setDueStatus("0");
            return;
        }
        long today = System.currentTimeMillis() / 86400000L;
        long due = payable.getDueDate().getTime() / 86400000L;
        if (due < today)
        {
            payable.setDueStatus("2");
        }
        else if (due - today <= 7)
        {
            payable.setDueStatus("1");
        }
        else
        {
            payable.setDueStatus("0");
        }
    }
}
