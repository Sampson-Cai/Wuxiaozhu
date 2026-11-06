package com.ruoyi.web.app.dto;

/**
 * AI 提问请求
 *
 * @author cangxiaozhu
 */
public class AiChatRequest
{
    /** 会话ID，为空则新建会话 */
    private Long sessionId;

    /** 问题 */
    private String question;

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getQuestion()
    {
        return question;
    }

    public void setQuestion(String question)
    {
        this.question = question;
    }
}
