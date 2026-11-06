package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 采购单
 *
 * 状态：0 草稿 / 1 待审批 / 2 已通过 / 3 已驳回 / 4 已完成 / 5 已取消
 *
 * @author cangxiaozhu
 */
public class PurchaseOrder extends BizBase
{
    private Long orderId;

    private String orderNo;

    private Long supplierId;

    private String supplierName;

    private BigDecimal totalAmount;

    private String status;

    private Integer version;

    private String submitBy;

    private Date submitTime;

    private String approveBy;

    private Date approveTime;

    private String receiveBy;

    private Date receiveTime;

    private String cancelReason;

    private String delFlag;

    /** 明细，仅用于请求与详情返回 */
    private List<PurchaseOrderItem> items;

    /** 审批记录，仅用于详情返回 */
    private List<PurchaseApproval> approvals;

    /** 查询条件：关键字（单号或供应商） */
    private String keyword;

    /** 查询条件：创建时间起（yyyy-MM-dd） */
    private String beginTime;

    /** 查询条件：创建时间止（yyyy-MM-dd） */
    private String endTime;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getSubmitBy()
    {
        return submitBy;
    }

    public void setSubmitBy(String submitBy)
    {
        this.submitBy = submitBy;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public String getApproveBy()
    {
        return approveBy;
    }

    public void setApproveBy(String approveBy)
    {
        this.approveBy = approveBy;
    }

    public Date getApproveTime()
    {
        return approveTime;
    }

    public void setApproveTime(Date approveTime)
    {
        this.approveTime = approveTime;
    }

    public String getReceiveBy()
    {
        return receiveBy;
    }

    public void setReceiveBy(String receiveBy)
    {
        this.receiveBy = receiveBy;
    }

    public Date getReceiveTime()
    {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime)
    {
        this.receiveTime = receiveTime;
    }

    public String getCancelReason()
    {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason)
    {
        this.cancelReason = cancelReason;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public List<PurchaseOrderItem> getItems()
    {
        return items;
    }

    public void setItems(List<PurchaseOrderItem> items)
    {
        this.items = items;
    }

    public List<PurchaseApproval> getApprovals()
    {
        return approvals;
    }

    public void setApprovals(List<PurchaseApproval> approvals)
    {
        this.approvals = approvals;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getBeginTime()
    {
        return beginTime;
    }

    public void setBeginTime(String beginTime)
    {
        this.beginTime = beginTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }
}
