package com.ruoyi.web.app.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSON;
import com.ruoyi.business.ai.AiStreamListener;
import com.ruoyi.business.ai.domain.AiChatMessage;
import com.ruoyi.business.ai.domain.AiChatSession;
import com.ruoyi.business.ai.domain.KbDocument;
import com.ruoyi.business.ai.service.IAiChatService;
import com.ruoyi.business.ai.service.IKbService;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.web.app.dto.AiChatRequest;
import com.ruoyi.web.app.dto.KbImportRequest;

/**
 * AI 助手：RAG 对话与知识库管理
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/ai")
public class AppAiController extends AppBaseController
{
    @Autowired
    private IAiChatService aiChatService;

    @Autowired
    private IKbService kbService;

    /**
     * 能力状态：是否启用、模型名、知识库规模
     */
    @GetMapping("/status")
    public AppResult status()
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        Map<String, Object> result = new HashMap<>(kbService.stats(eid));
        result.put("enabled", aiChatService.enabled());
        return AppResult.ok(result);
    }

    /**
     * 提问（RAG）
     */
    @PostMapping("/chat")
    public AppResult chat(@RequestBody AiChatRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty())
        {
            return AppResult.error("问题不能为空");
        }
        return AppResult.ok(aiChatService.ask(eid, userId(), operator(), request.getSessionId(),
                request.getQuestion()));
    }

    /**
     * 流式提问（SSE）
     *
     * 事件：meta（sessionId + sources）→ delta（增量文本，多次）→ done；出错为 error。
     */
    @PostMapping(value = "/chat/stream", produces = "text/event-stream;charset=UTF-8")
    public void chatStream(@RequestBody AiChatRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");
        PrintWriter writer = response.getWriter();

        Long eid = enterpriseId();
        if (eid == null)
        {
            writeEvent(writer, "error", single("message", "登录状态已过期，请重新登录"));
            return;
        }
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty())
        {
            writeEvent(writer, "error", single("message", "问题不能为空"));
            return;
        }

        try
        {
            aiChatService.streamAsk(eid, userId(), operator(), request.getSessionId(), request.getQuestion(),
                    new AiStreamListener()
                    {
                        @Override
                        public void onMeta(Long sessionId)
                        {
                            Map<String, Object> meta = new LinkedHashMap<>();
                            meta.put("sessionId", sessionId);
                            writeEvent(writer, "meta", JSON.toJSONString(meta));
                        }

                        @Override
                        public void onSources(List<Map<String, Object>> sources)
                        {
                            writeEvent(writer, "sources", JSON.toJSONString(sources));
                        }

                        @Override
                        public void onDelta(String text)
                        {
                            writeEvent(writer, "delta", single("text", text));
                        }

                        @Override
                        public void onDone(String answer, int tokens, long costMs)
                        {
                            Map<String, Object> done = new LinkedHashMap<>();
                            done.put("tokens", tokens);
                            done.put("costMs", costMs);
                            done.put("length", answer == null ? 0 : answer.length());
                            writeEvent(writer, "done", JSON.toJSONString(done));
                        }

                        @Override
                        public void onError(String message)
                        {
                            writeEvent(writer, "error", single("message", message));
                        }
                    });
        }
        catch (RuntimeException e)
        {
            writeEvent(writer, "error", single("message", e.getMessage()));
        }
    }

    /**
     * 写一个 SSE 事件并立即 flush，保证前端能逐段收到
     */
    private void writeEvent(PrintWriter writer, String event, String data)
    {
        writer.write("event: " + event + "\n");
        writer.write("data: " + data + "\n\n");
        writer.flush();
        if (writer.checkError())
        {
            throw new IllegalStateException("客户端已断开");
        }
    }

    private String single(String key, String value)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value == null ? "" : value);
        return JSON.toJSONString(map);
    }

    /**
     * 我的会话
     */
    @GetMapping("/sessions")
    public AppResult sessions(@RequestParam(required = false, defaultValue = "20") Integer limit)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        List<AiChatSession> list = aiChatService.sessions(eid, userId(), limit);
        return AppResult.ok().put("rows", list).put("total", list.size());
    }

    /**
     * 会话消息
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public AppResult messages(@PathVariable Long sessionId,
            @RequestParam(required = false, defaultValue = "50") Integer limit)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        List<AiChatMessage> list = aiChatService.messages(sessionId, limit);
        return AppResult.ok().put("rows", list).put("total", list.size());
    }

    /**
     * 知识库文档列表
     */
    @GetMapping("/kb/documents")
    public AppResult documents()
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        List<KbDocument> list = kbService.listDocuments(eid);
        return AppResult.ok().put("rows", list).put("total", list.size());
    }

    /**
     * 导入服务器本地文件（.md / .txt / .csv）
     */
    @PostMapping("/kb/import")
    @AppPermission("business:kb:edit")
    public AppResult importFile(@RequestBody KbImportRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (request == null || request.getPath() == null || request.getPath().trim().isEmpty())
        {
            return AppResult.error("文件路径不能为空");
        }
        KbDocument document = kbService.importFile(eid, request.getPath().trim(), request.getDocType(),
                operator());
        return AppResult.ok("导入完成，切片 " + document.getChunkCount() + " 条").put("data", document);
    }

    /**
     * 重新向量化
     */
    @PostMapping("/kb/{documentId}/reindex")
    @AppPermission("business:kb:edit")
    public AppResult reindex(@PathVariable Long documentId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        kbService.reindex(eid, documentId);
        return AppResult.ok("已重新向量化");
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/kb/{documentId}")
    @AppPermission("business:kb:edit")
    public AppResult remove(@PathVariable Long documentId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        kbService.deleteDocument(eid, documentId);
        return AppResult.ok("删除成功");
    }
}
