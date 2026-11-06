package com.ruoyi.business.ai.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.ai.domain.AiChatMessage;
import com.ruoyi.business.ai.domain.AiChatSession;

/**
 * AI 会话与消息 Mapper
 *
 * @author cangxiaozhu
 */
public interface AiChatMapper
{
    int insertSession(AiChatSession session);

    int touchSession(@Param("sessionId") Long sessionId, @Param("title") String title);

    AiChatSession selectSession(@Param("enterpriseId") Long enterpriseId, @Param("sessionId") Long sessionId);

    List<AiChatSession> selectSessions(@Param("enterpriseId") Long enterpriseId, @Param("userId") Long userId,
            @Param("limit") Integer limit);

    int insertMessage(AiChatMessage message);

    List<AiChatMessage> selectMessages(@Param("sessionId") Long sessionId, @Param("limit") Integer limit);
}
