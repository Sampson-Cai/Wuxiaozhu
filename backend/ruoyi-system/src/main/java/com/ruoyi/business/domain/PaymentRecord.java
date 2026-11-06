package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 付款记录
 *
 * @author cangxiaozhu
 */
public class PaymentRecord
{
    private Long paymentId;

    private Long enterpriseId;

    private Long payableId;

    private BigDecimal amount;

    private String payMethod;

    private Date payTime;

    private String voucherUrl;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private Date createTime;

    public Long getPaymentId()
    {
        return paymentId;
    }

    public void setPaymentId(Long paymentId)
    {
        this.paymentId = paymentId;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getPayableId()
    {
        return payableId;
    }

    public void setPayableId(Long payableId)
    {
        this.payableId = payableId;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getPayMethod()
    {
        return payMethod;
    }

    public void setPayMethod(String payMethod)
    {
        this.payMethod = payMethod;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public String getVoucherUrl()
    {
        return voucherUrl;
    }

    public void setVoucherUrl(String voucherUrl)
    {
        this.voucherUrl = voucherUrl;
    }

    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    public String getOperatorName()
    {
        return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
