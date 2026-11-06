package com.ruoyi.business.ai;

import java.util.List;
import java.util.Map;

/**
 * 流式问答回调
 *
 * 调用顺序：onMeta → onDelta(多次) → onDone；出错则 onError。
 *
 * @author cangxiaozhu
 */
public interface AiStreamListener
{
    /**
     * 首帧：会话ID（检索还没完成，先把气泡显示出来）
     */
    void onMeta(Long sessionId);

    /**
     * 检索完成：引用来源
     */
    void onSources(List<Map<String, Object>> sources);

    /**
     * 增量文本
     */
    void onDelta(String text);

    /**
     * 结束：完整回答、token 用量、耗时
     */
    void onDone(String answer, int tokens, long costMs);

    /**
     * 失败
     */
    void onError(String message);
}
