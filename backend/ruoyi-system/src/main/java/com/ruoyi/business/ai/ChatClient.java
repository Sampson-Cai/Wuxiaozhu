package com.ruoyi.business.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.exception.ServiceException;

/**
 * 对话模型客户端（DeepSeek-V4.1-Flash，OpenAI 兼容协议）
 *
 * @author cangxiaozhu
 */
@Component
public class ChatClient
{
    private static final Logger log = LoggerFactory.getLogger(ChatClient.class);

    private static final HttpClient STREAM_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @Autowired
    private AiProperties properties;

    /**
     * 一轮对话
     *
     * @param messages role/content 列表
     * @return 回复内容与用量
     */
    public ChatReply chat(List<Map<String, String>> messages)
    {
        AiProperties.Chat config = properties.getChat();
        if (config.getApiKey() == null || config.getApiKey().isEmpty())
        {
            throw new ServiceException("未配置对话模型 API Key");
        }
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("messages", messages);
        body.put("temperature", config.getTemperature());
        body.put("max_tokens", config.getMaxTokens());
        body.put("stream", false);
        putReasoningEffort(body, config);

        long start = System.currentTimeMillis();
        String response = AiHttp.postJson(config.getBaseUrl(), config.getApiKey(), body.toJSONString(),
                config.getTimeoutSeconds());
        long cost = System.currentTimeMillis() - start;

        JSONObject root = JSON.parseObject(response);
        JSONArray choices = root == null ? null : root.getJSONArray("choices");
        if (choices == null || choices.isEmpty())
        {
            String message = root == null ? response : root.getString("message");
            throw new ServiceException("对话模型没有返回内容：" + message);
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        String content = message == null ? null : message.getString("content");
        int tokens = 0;
        JSONObject usage = root.getJSONObject("usage");
        if (usage != null)
        {
            tokens = usage.getIntValue("total_tokens");
        }
        log.info("chat 完成，model={}，tokens={}，耗时 {}ms", config.getModel(), tokens, cost);
        return new ChatReply(content == null ? "" : content.trim(), config.getModel(), tokens, cost);
    }

    /**
     * 流式对话：按 token 回调，返回完整结果
     *
     * @param messages 上下文
     * @param onDelta 每收到一个片段回调一次（在调用线程执行）
     */
    public ChatReply streamChat(List<Map<String, String>> messages, Consumer<String> onDelta)
    {
        AiProperties.Chat config = properties.getChat();
        if (config.getApiKey() == null || config.getApiKey().isEmpty())
        {
            throw new ServiceException("未配置对话模型 API Key");
        }
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("messages", messages);
        body.put("temperature", config.getTemperature());
        body.put("max_tokens", config.getMaxTokens());
        body.put("stream", true);
        putReasoningEffort(body, config);
        JSONObject options = new JSONObject();
        options.put("include_usage", true);
        body.put("stream_options", options);

        long start = System.currentTimeMillis();
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBaseUrl()))
                .timeout(Duration.ofSeconds(Math.max(120, config.getTimeoutSeconds())))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "text/event-stream")
                .header("Authorization", "Bearer " + config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString(), StandardCharsets.UTF_8))
                .build();

        StringBuilder answer = new StringBuilder();
        int tokens = 0;
        try
        {
            HttpResponse<Stream<String>> response = STREAM_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofLines());
            if (response.statusCode() / 100 != 2)
            {
                String detail = response.body().limit(5).reduce("", (a, b) -> a + b);
                throw new ServiceException("对话模型返回 " + response.statusCode() + "：" + detail);
            }
            try (Stream<String> lines = response.body())
            {
                Iterator<String> iterator = lines.iterator();
                while (iterator.hasNext())
                {
                    String line = iterator.next();
                    if (!line.startsWith("data:"))
                    {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if (data.isEmpty())
                    {
                        continue;
                    }
                    if ("[DONE]".equals(data))
                    {
                        break;
                    }
                    JSONObject chunk = JSON.parseObject(data);
                    JSONArray choices = chunk.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty())
                    {
                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                        String piece = delta == null ? null : delta.getString("content");
                        if (piece != null && !piece.isEmpty())
                        {
                            answer.append(piece);
                            onDelta.accept(piece);
                        }
                    }
                    JSONObject usage = chunk.getJSONObject("usage");
                    if (usage != null)
                    {
                        tokens = usage.getIntValue("total_tokens");
                    }
                }
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("流式对话被中断");
        }
        catch (Exception e)
        {
            throw new ServiceException("流式对话失败：" + e.getMessage());
        }

        long cost = System.currentTimeMillis() - start;
        log.info("stream chat 完成，tokens={}，耗时 {}ms", tokens, cost);
        return new ChatReply(answer.toString().trim(), config.getModel(), tokens, cost);
    }

    /**
     * 推理强度：low 能明显降低首字延迟
     */
    private void putReasoningEffort(JSONObject body, AiProperties.Chat config)
    {
        if (!config.isThinkingEnabled())
        {
            JSONObject thinking = new JSONObject();
            thinking.put("type", "disabled");
            body.put("thinking", thinking);
            return;
        }
        String effort = config.getReasoningEffort();
        if (effort != null && !effort.isEmpty())
        {
            body.put("reasoning_effort", effort);
        }
    }

    /**
     * 组装一条消息
     */
    public static Map<String, String> message(String role, String content)
    {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("role", role);
        item.put("content", content);
        return item;
    }

    public static List<Map<String, String>> newMessages()
    {
        return new ArrayList<>();
    }

    /**
     * 模型回复
     */
    public static class ChatReply
    {
        private final String content;

        private final String model;

        private final int tokens;

        private final long costMs;

        public ChatReply(String content, String model, int tokens, long costMs)
        {
            this.content = content;
            this.model = model;
            this.tokens = tokens;
            this.costMs = costMs;
        }

        public String getContent()
        {
            return content;
        }

        public String getModel()
        {
            return model;
        }

        public int getTokens()
        {
            return tokens;
        }

        public long getCostMs()
        {
            return costMs;
        }
    }
}
