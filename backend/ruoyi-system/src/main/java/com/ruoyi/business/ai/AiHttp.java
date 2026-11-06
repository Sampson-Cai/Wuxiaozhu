package com.ruoyi.business.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import com.ruoyi.common.exception.ServiceException;

/**
 * 模型服务 HTTP 调用（JDK 17 HttpClient，不额外引依赖）
 *
 * @author cangxiaozhu
 */
public final class AiHttp
{
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private AiHttp()
    {
    }

    /**
     * POST JSON
     *
     * @param url 完整地址
     * @param apiKey 令牌，空则不带头
     * @param jsonBody 请求体
     * @param timeoutSeconds 超时秒数
     * @return 响应体
     */
    public static String postJson(String url, String apiKey, String jsonBody, int timeoutSeconds)
    {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds <= 0 ? 60 : timeoutSeconds))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
        if (apiKey != null && !apiKey.isEmpty())
        {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        try
        {
            HttpResponse<String> response = CLIENT.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2)
            {
                throw new ServiceException("模型服务返回 " + response.statusCode() + "：" + brief(response.body()));
            }
            return response.body();
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("模型服务调用被中断");
        }
        catch (Exception e)
        {
            throw new ServiceException("模型服务连接失败：" + e.getMessage());
        }
    }

    private static String brief(String body)
    {
        if (body == null)
        {
            return "";
        }
        String text = body.replaceAll("\\s+", " ").trim();
        return text.length() > 300 ? text.substring(0, 300) + "…" : text;
    }
}
