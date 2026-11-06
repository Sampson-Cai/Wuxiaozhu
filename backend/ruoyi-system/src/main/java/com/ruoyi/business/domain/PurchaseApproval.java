package com.ruoyi.business.domain;

import java.util.Date;

/**
 * 采购审批记录
 *
 * 结果：1 通过 / 2 驳回
 *
 * @author cangxiaozhu
 */
public class PurchaseApproval
{
    private Long approvalId;

    private Long enterpriseId;

    private Long orderId;

    private Long approverId;

    private String approverName;

    private String result;

    private String opinion;

    private Date approveTime;

    private Date createTime;

    public Long getApprovalId()
    {
        return approvalId;
    }

    public void setApprovalId(Long approvalId)
    {
        this.approvalId = approvalId;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getApproverId()
    {
        return approverId;
    }

    public void setApproverId(Long approverId)
    {
        this.approverId = approverId;
    }

    public String getApproverName()
    {
        return approverName;
    }

    public void setApproverName(String approverName)
    {
        this.approverName = approverName;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public String getOpinion()
    {
        return opinion;
    }

    public void setOpinion(String opinion)
    {
        this.opinion = opinion;
    }

    public Date getApproveTime()
    {
        return approveTime;
    }

    public void setApproveTime(Date approveTime)
    {
        this.approveTime = approveTime;
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
