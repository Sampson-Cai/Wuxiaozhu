package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 应付账款
 *
 * 状态：0 未付款 / 1 部分付款 / 2 已结清
 *
 * @author cangxiaozhu
 */
public class Payable extends BizBase
{
    private Long payableId;

    private String payableNo;

    private Long orderId;

    private Long supplierId;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private Date dueDate;

    private String status;

    /** 供应商名称（联表） */
    private String supplierName;

    /** 采购单号（联表） */
    private String orderNo;

    /** 查询条件：供应商 */
    private Long querySupplierId;

    /** 查询条件：是否只看未结清 */
    private Boolean unpaidOnly;

    /** 查询条件：N 天内到期（含已逾期） */
    private Integer dueWithinDays;

    /** 查询条件：只看已逾期 */
    private Boolean overdueOnly;

    /** 未付金额（服务端计算） */
    private BigDecimal remainingAmount;

    /** 到期状态：0 未到期 / 1 临期(7天内) / 2 逾期 / 3 已结清 */
    private String dueStatus;

    public Long getPayableId()
    {
        return payableId;
    }

    public void setPayableId(Long payableId)
    {
        this.payableId = payableId;
    }

    public String getPayableNo()
    {
        return payableNo;
    }

    public void setPayableNo(String payableNo)
    {
        this.payableNo = payableNo;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPaidAmount()
    {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount)
    {
        this.paidAmount = paidAmount;
    }

    public Date getDueDate()
    {
        return dueDate;
    }

    public void setDueDate(Date dueDate)
    {
        this.dueDate = dueDate;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getQuerySupplierId()
    {
        return querySupplierId;
    }

    public void setQuerySupplierId(Long querySupplierId)
    {
        this.querySupplierId = querySupplierId;
    }

    public Boolean getUnpaidOnly()
    {
        return unpaidOnly;
    }

    public void setUnpaidOnly(Boolean unpaidOnly)
    {
        this.unpaidOnly = unpaidOnly;
    }

    public BigDecimal getRemainingAmount()
    {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount)
    {
        this.remainingAmount = remainingAmount;
    }

    public String getDueStatus()
    {
        return dueStatus;
    }

    public void setDueStatus(String dueStatus)
    {
        this.dueStatus = dueStatus;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Integer getDueWithinDays()
    {
        return dueWithinDays;
    }

    public void setDueWithinDays(Integer dueWithinDays)
    {
        this.dueWithinDays = dueWithinDays;
    }

    public Boolean getOverdueOnly()
    {
        return overdueOnly;
    }

    public void setOverdueOnly(Boolean overdueOnly)
    {
        this.overdueOnly = overdueOnly;
    }
}
