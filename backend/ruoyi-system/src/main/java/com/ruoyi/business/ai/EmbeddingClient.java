package com.ruoyi.business.ai;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.exception.ServiceException;

/**
 * 向量模型客户端（硅基流动 Qwen/Qwen3-Embedding-8B，4096 维）
 *
 * @author cangxiaozhu
 */
@Component
public class EmbeddingClient
{
    private static final Logger log = LoggerFactory.getLogger(EmbeddingClient.class);

    @Autowired
    private AiProperties properties;

    /**
     * 单条文本向量
     */
    public float[] embedOne(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            return null;
        }
        List<float[]> vectors = embed(java.util.Collections.singletonList(text));
        return vectors.isEmpty() ? null : vectors.get(0);
    }

    /**
     * 批量向量，内部按 batchSize 分片
     */
    public List<float[]> embed(List<String> texts)
    {
        List<float[]> result = new ArrayList<>();
        if (texts == null || texts.isEmpty())
        {
            return result;
        }
        int batchSize = Math.max(1, properties.getEmbedding().getBatchSize());
        for (int i = 0; i < texts.size(); i += batchSize)
        {
            int end = Math.min(texts.size(), i + batchSize);
            result.addAll(request(texts.subList(i, end)));
        }
        return result;
    }

    private List<float[]> request(List<String> slice)
    {
        AiProperties.Embedding config = properties.getEmbedding();
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("input", slice);
        body.put("encoding_format", "float");

        long start = System.currentTimeMillis();
        String response = AiHttp.postJson(config.getBaseUrl(), config.getApiKey(), body.toJSONString(),
                config.getTimeoutSeconds());
        JSONObject root = JSON.parseObject(response);
        JSONArray data = root == null ? null : root.getJSONArray("data");
        if (data == null || data.isEmpty())
        {
            String message = root == null ? response : root.getString("message");
            throw new ServiceException("向量模型没有返回结果：" + message);
        }

        List<float[]> vectors = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++)
        {
            JSONArray array = data.getJSONObject(i).getJSONArray("embedding");
            float[] vector = new float[array.size()];
            for (int j = 0; j < array.size(); j++)
            {
                vector[j] = array.getFloatValue(j);
            }
            vectors.add(vector);
        }
        log.info("embedding {} 条，维度 {}，耗时 {}ms", vectors.size(),
                vectors.isEmpty() ? 0 : vectors.get(0).length, System.currentTimeMillis() - start);
        return vectors;
    }
}
