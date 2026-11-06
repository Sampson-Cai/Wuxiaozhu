package com.ruoyi.business.domain;

import java.util.Date;

/**
 * 幂等记录
 *
 * 状态：0 处理中 / 1 已成功 / 2 已失败（失败可用同一 requestId 重试）
 *
 * @author cangxiaozhu
 */
public class IdemRecord
{
    private Long idemId;

    private Long enterpriseId;

    private Long userId;

    private String requestId;

    private String api;

    private String status;

    private String resultSummary;

    private Date createTime;

    public Long getIdemId()
    {
        return idemId;
    }

    public void setIdemId(Long idemId)
    {
        this.idemId = idemId;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getApi()
    {
        return api;
    }

    public void setApi(String api)
    {
        this.api = api;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getResultSummary()
    {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary)
    {
        this.resultSummary = resultSummary;
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
