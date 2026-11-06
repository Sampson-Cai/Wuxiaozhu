package com.ruoyi.business.ai;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

/**
 * 向量工具：余弦相似度与存取编码
 *
 * 4096 维 float 用 JSON 存一份约 80KB，改用 float32 + Base64 后约 22KB，
 * 库里存 t_kb_chunk.embedding，读取时解码成 float[]。
 *
 * @author cangxiaozhu
 */
public final class VectorUtils
{
    private VectorUtils()
    {
    }

    /**
     * 余弦相似度，输入向量先做归一化，返回 -1 ~ 1
     */
    public static double cosine(float[] a, float[] b)
    {
        if (a == null || b == null || a.length != b.length || a.length == 0)
        {
            return 0D;
        }
        double dot = 0D;
        double normA = 0D;
        double normB = 0D;
        for (int i = 0; i < a.length; i++)
        {
            dot += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        if (normA == 0D || normB == 0D)
        {
            return 0D;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static String encode(float[] vector)
    {
        if (vector == null)
        {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float value : vector)
        {
            buffer.putFloat(value);
        }
        return Base64.getEncoder().encodeToString(buffer.array());
    }

    public static float[] decode(String encoded)
    {
        if (encoded == null || encoded.isEmpty())
        {
            return null;
        }
        byte[] bytes = Base64.getDecoder().decode(encoded);
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        float[] vector = new float[bytes.length / 4];
        for (int i = 0; i < vector.length; i++)
        {
            vector[i] = buffer.getFloat();
        }
        return vector;
    }
}
