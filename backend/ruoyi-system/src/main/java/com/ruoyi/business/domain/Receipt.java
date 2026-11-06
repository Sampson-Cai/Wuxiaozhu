package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收货单
 *
 * @author cangxiaozhu
 */
public class Receipt
{
    private Long receiptId;

    private Long enterpriseId;

    private String receiptNo;

    private Long orderId;

    private String receiptType;

    private BigDecimal totalQty;

    private Long receiverId;

    private String receiverName;

    private Date receiptTime;

    private String remark;

    /** 采购单号（联表） */
    private String orderNo;

    private Date createTime;

    public Long getReceiptId()
    {
        return receiptId;
    }

    public void setReceiptId(Long receiptId)
    {
        this.receiptId = receiptId;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public String getReceiptNo()
    {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo)
    {
        this.receiptNo = receiptNo;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getReceiptType()
    {
        return receiptType;
    }

    public void setReceiptType(String receiptType)
    {
        this.receiptType = receiptType;
    }

    public BigDecimal getTotalQty()
    {
        return totalQty;
    }

    public void setTotalQty(BigDecimal totalQty)
    {
        this.totalQty = totalQty;
    }

    public Long getReceiverId()
    {
        return receiverId;
    }

    public void setReceiverId(Long receiverId)
    {
        this.receiverId = receiverId;
    }

    public String getReceiverName()
    {
        return receiverName;
    }

    public void setReceiverName(String receiverName)
    {
        this.receiverName = receiverName;
    }

    public Date getReceiptTime()
    {
        return receiptTime;
    }

    public void setReceiptTime(Date receiptTime)
    {
        this.receiptTime = receiptTime;
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

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }
}
