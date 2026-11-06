package com.ruoyi.business.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 能力配置（对话 / 向量 / RAG）
 *
 * 全部走 OpenAI 兼容协议：
 * - 对话：DeepSeek https://api.deepseek.com/chat/completions
 * - 向量：硅基流动 https://api.siliconflow.cn/v1/embeddings
 *
 * @author cangxiaozhu
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties
{
    /** 是否启用 AI（关闭后接口返回未启用，小程序自动回落到本地问答） */
    private boolean enabled = true;

    private Chat chat = new Chat();

    private Embedding embedding = new Embedding();

    private Rag rag = new Rag();

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public Chat getChat()
    {
        return chat;
    }

    public void setChat(Chat chat)
    {
        this.chat = chat;
    }

    public Embedding getEmbedding()
    {
        return embedding;
    }

    public void setEmbedding(Embedding embedding)
    {
        this.embedding = embedding;
    }

    public Rag getRag()
    {
        return rag;
    }

    public void setRag(Rag rag)
    {
        this.rag = rag;
    }

    /**
     * 对话模型配置
     */
    public static class Chat
    {
        /** 接口地址（含 /chat/completions） */
        private String baseUrl = "https://api.deepseek.com/chat/completions";

        private String apiKey = "";

        /** 模型标识，DeepSeek-V4.1-Flash 的 API id 为 deepseek-flash */
        private String model = "deepseek-flash";

        private double temperature = 0.3;

        private int maxTokens = 1200;

        /** 推理强度：low 首字更快，high 更稳但慢 1~2 秒；空串表示不传 */
        private String reasoningEffort = "low";

        /** 是否允许模型先思考：关闭后首字再快约 0.8 秒，RAG 场景不需要 */
        private boolean thinkingEnabled = false;

        private int timeoutSeconds = 60;

        public String getBaseUrl()
        {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl)
        {
            this.baseUrl = baseUrl;
        }

        public String getApiKey()
        {
            return apiKey;
        }

        public void setApiKey(String apiKey)
        {
            this.apiKey = apiKey;
        }

        public String getModel()
        {
            return model;
        }

        public void setModel(String model)
        {
            this.model = model;
        }

        public double getTemperature()
        {
            return temperature;
        }

        public void setTemperature(double temperature)
        {
            this.temperature = temperature;
        }

        public boolean isThinkingEnabled()
        {
            return thinkingEnabled;
        }

        public void setThinkingEnabled(boolean thinkingEnabled)
        {
            this.thinkingEnabled = thinkingEnabled;
        }

        public String getReasoningEffort()
        {
            return reasoningEffort;
        }

        public void setReasoningEffort(String reasoningEffort)
        {
            this.reasoningEffort = reasoningEffort;
        }

        public int getMaxTokens()
        {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens)
        {
            this.maxTokens = maxTokens;
        }

        public int getTimeoutSeconds()
        {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds)
        {
            this.timeoutSeconds = timeoutSeconds;
        }
    }

    /**
     * 向量模型配置
     */
    public static class Embedding
    {
        private String baseUrl = "https://api.siliconflow.cn/v1/embeddings";

        private String apiKey = "";

        /** Qwen/Qwen3-Embedding-8B，输出 4096 维 */
        private String model = "Qwen/Qwen3-Embedding-8B";

        private int dim = 4096;

        /** 单次请求最多送多少条文本 */
        private int batchSize = 16;

        private int timeoutSeconds = 60;

        public String getBaseUrl()
        {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl)
        {
            this.baseUrl = baseUrl;
        }

        public String getApiKey()
        {
            return apiKey;
        }

        public void setApiKey(String apiKey)
        {
            this.apiKey = apiKey;
        }

        public String getModel()
        {
            return model;
        }

        public void setModel(String model)
        {
            this.model = model;
        }

        public int getDim()
        {
            return dim;
        }

        public void setDim(int dim)
        {
            this.dim = dim;
        }

        public int getBatchSize()
        {
            return batchSize;
        }

        public void setBatchSize(int batchSize)
        {
            this.batchSize = batchSize;
        }

        public int getTimeoutSeconds()
        {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds)
        {
            this.timeoutSeconds = timeoutSeconds;
        }
    }

    /**
     * 检索增强配置
     */
    public static class Rag
    {
        /** 召回条数 */
        private int topK = 5;

        /** 相似度下限，低于该值不作为参考资料 */
        private double minScore = 0.32;

        /** 拼进提示词的资料字符上限 */
        private int maxContextChars = 4000;

        /** 带多少条历史消息 */
        private int historyMessages = 6;

        public int getTopK()
        {
            return topK;
        }

        public void setTopK(int topK)
        {
            this.topK = topK;
        }

        public double getMinScore()
        {
            return minScore;
        }

        public void setMinScore(double minScore)
        {
            this.minScore = minScore;
        }

        public int getMaxContextChars()
        {
            return maxContextChars;
        }

        public void setMaxContextChars(int maxContextChars)
        {
            this.maxContextChars = maxContextChars;
        }

        public int getHistoryMessages()
        {
            return historyMessages;
        }

        public void setHistoryMessages(int historyMessages)
        {
            this.historyMessages = historyMessages;
        }
    }
}
