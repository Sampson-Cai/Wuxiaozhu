package com.ruoyi.web.app.dto;

import java.math.BigDecimal;

/**
 * 付款登记请求
 *
 * @author cangxiaozhu
 */
public class PaymentRequest
{
    private BigDecimal amount;

    private String payMethod;

    private String voucherUrl;

    private String remark;

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

    public String getVoucherUrl()
    {
        return voucherUrl;
    }

    public void setVoucherUrl(String voucherUrl)
    {
        this.voucherUrl = voucherUrl;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
