package com.ruoyi.business.ai.domain;

import java.util.Date;

/**
 * AI 消息
 *
 * @author cangxiaozhu
 */
public class AiChatMessage
{
    private Long messageId;

    private Long enterpriseId;

    private Long sessionId;

    /** user / assistant / tool */
    private String role;

    private String content;

    /** 引用来源 JSON 数组 */
    private String sources;

    private String model;

    private Integer tokenUsage;

    private Integer costMs;

    private Date createTime;

    public Long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getSources()
    {
        return sources;
    }

    public void setSources(String sources)
    {
        this.sources = sources;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public Integer getTokenUsage()
    {
        return tokenUsage;
    }

    public void setTokenUsage(Integer tokenUsage)
    {
        this.tokenUsage = tokenUsage;
    }

    public Integer getCostMs()
    {
        return costMs;
    }

    public void setCostMs(Integer costMs)
    {
        this.costMs = costMs;
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
