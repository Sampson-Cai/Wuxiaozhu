package com.ruoyi.business.ai.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.ai.AiStreamListener;
import com.ruoyi.business.ai.domain.AiChatMessage;
import com.ruoyi.business.ai.domain.AiChatSession;

/**
 * AI 对话服务（RAG）
 *
 * @author cangxiaozhu
 */
public interface IAiChatService
{
    /**
     * 提问
     *
     * @param enterpriseId 单位
     * @param userId 用户
     * @param operator 操作人（登录名）
     * @param sessionId 会话ID，空则新建
     * @param question 问题
     * @return sessionId、answer、sources、usage
     */
    Map<String, Object> ask(Long enterpriseId, Long userId, String operator, Long sessionId, String question);

    /**
     * 流式提问（SSE）
     *
     * @param listener 逐段回调，由调用方负责写出
     */
    void streamAsk(Long enterpriseId, Long userId, String operator, Long sessionId, String question,
            AiStreamListener listener);

    /**
     * 我的会话列表
     */
    List<AiChatSession> sessions(Long enterpriseId, Long userId, int limit);

    /**
     * 会话消息
     */
    List<AiChatMessage> messages(Long sessionId, int limit);

    /**
     * AI 是否可用
     */
    boolean enabled();
}
