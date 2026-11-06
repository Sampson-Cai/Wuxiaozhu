package com.ruoyi.business.ai.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.ai.AiProperties;
import com.ruoyi.business.ai.EmbeddingClient;
import com.ruoyi.business.ai.VectorUtils;
import com.ruoyi.business.ai.domain.KbChunk;
import com.ruoyi.business.ai.domain.KbDocument;
import com.ruoyi.business.ai.domain.KbHit;
import com.ruoyi.business.ai.mapper.KbChunkMapper;
import com.ruoyi.business.ai.mapper.KbDocumentMapper;
import com.ruoyi.business.ai.service.IKbService;
import com.ruoyi.business.ai.util.MarkdownChunker;
import com.ruoyi.common.exception.ServiceException;

/**
 * 知识库服务实现
 *
 * 向量直接存 MySQL（Base64 编码的 float32），检索在内存做余弦相似度。
 * 当前量级（几十到几千片段）足够；片段数上万再换向量库。
 *
 * @author cangxiaozhu
 */
@Service
public class KbServiceImpl implements IKbService
{
    private static final Logger log = LoggerFactory.getLogger(KbServiceImpl.class);

    /** 单块字符上限 */
    private static final int MAX_CHARS = 700;

    /** 相邻块重叠字符 */
    private static final int OVERLAP = 120;

    /** 索引缓存有效期 */
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    @Autowired
    private KbDocumentMapper documentMapper;

    @Autowired
    private KbChunkMapper chunkMapper;

    @Autowired
    private EmbeddingClient embeddingClient;

    @Autowired
    private AiProperties properties;

    private final Map<Long, Index> indexes = new ConcurrentHashMap<>();

    @Override
    public List<KbDocument> listDocuments(Long enterpriseId)
    {
        return documentMapper.selectList(enterpriseId);
    }

    @Override
    public KbDocument importFile(Long enterpriseId, String filePath, String docType, String uploadBy)
    {
        if (filePath == null || filePath.trim().isEmpty())
        {
            throw new ServiceException("文件路径不能为空");
        }
        File file = new File(filePath.trim());
        if (!file.exists() || !file.isFile())
        {
            throw new ServiceException("文件不存在：" + filePath);
        }
        String content;
        try
        {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new ServiceException("读取文件失败：" + e.getMessage());
        }
        String name = file.getName();
        String title = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
        return importText(enterpriseId, title, name, file.getAbsolutePath(), docType, content, uploadBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KbDocument importText(Long enterpriseId, String title, String fileName, String filePath, String docType,
            String content, String uploadBy)
    {
        if (content == null || content.trim().isEmpty())
        {
            throw new ServiceException("文档内容为空");
        }
        List<String> pieces = MarkdownChunker.split(content, MAX_CHARS, OVERLAP);
        if (pieces.isEmpty())
        {
            throw new ServiceException("切片结果为空");
        }

        KbDocument document = filePath == null ? null : documentMapper.selectByPath(enterpriseId, filePath);
        if (document == null)
        {
            document = new KbDocument();
            document.setEnterpriseId(enterpriseId);
            document.setTitle(title == null || title.isEmpty() ? "未命名文档" : title);
            document.setFileName(fileName);
            document.setFilePath(filePath);
            document.setFileType(extensionOf(fileName));
            document.setFileSize((long) content.getBytes(StandardCharsets.UTF_8).length);
            document.setDocType(docType == null ? "资料" : docType);
            document.setStatus("0");
            document.setChunkCount(0);
            document.setUploadBy(uploadBy);
            documentMapper.insert(document);
        }

        chunkMapper.deleteByDocument(document.getDocumentId());
        try
        {
            List<float[]> vectors = embeddingClient.embed(pieces);
            if (vectors.size() != pieces.size())
            {
                throw new ServiceException("向量数量与切片数量不一致");
            }
            String model = properties.getEmbedding().getModel();
            for (int i = 0; i < pieces.size(); i++)
            {
                KbChunk chunk = new KbChunk();
                chunk.setEnterpriseId(enterpriseId);
                chunk.setDocumentId(document.getDocumentId());
                chunk.setChunkIndex(i);
                chunk.setContent(pieces.get(i));
                chunk.setTokenCount(pieces.get(i).length());
                chunk.setEmbedding(VectorUtils.encode(vectors.get(i)));
                chunk.setEmbeddingModel(model);
                chunk.setDim(vectors.get(i).length);
                chunk.setVectorRef("kb-" + document.getDocumentId() + "-" + i);
                chunkMapper.insert(chunk);
            }
            documentMapper.updateStatus(document.getDocumentId(), "1", pieces.size(), null);
            log.info("知识库导入完成：{}，切片 {} 条", document.getTitle(), pieces.size());
        }
        catch (RuntimeException e)
        {
            documentMapper.updateStatus(document.getDocumentId(), "2", 0, brief(e.getMessage()));
            throw e;
        }
        finally
        {
            indexes.remove(enterpriseId);
        }
        return documentMapper.selectById(enterpriseId, document.getDocumentId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reindex(Long enterpriseId, Long documentId)
    {
        KbDocument document = documentMapper.selectById(enterpriseId, documentId);
        if (document == null)
        {
            throw new ServiceException("文档不存在");
        }
        if (document.getFilePath() != null && new File(document.getFilePath()).isFile())
        {
            importFile(enterpriseId, document.getFilePath(), document.getDocType(), document.getUploadBy());
            return;
        }
        List<KbChunk> chunks = chunkMapper.selectByDocument(documentId);
        if (chunks.isEmpty())
        {
            throw new ServiceException("文档没有切片，请重新导入");
        }
        List<String> contents = new ArrayList<>();
        for (KbChunk chunk : chunks)
        {
            contents.add(chunk.getContent());
        }
        chunkMapper.deleteByDocument(documentId);
        List<float[]> vectors = embeddingClient.embed(contents);
        String model = properties.getEmbedding().getModel();
        for (int i = 0; i < contents.size(); i++)
        {
            KbChunk chunk = new KbChunk();
            chunk.setEnterpriseId(enterpriseId);
            chunk.setDocumentId(documentId);
            chunk.setChunkIndex(i);
            chunk.setContent(contents.get(i));
            chunk.setTokenCount(contents.get(i).length());
            chunk.setEmbedding(VectorUtils.encode(vectors.get(i)));
            chunk.setEmbeddingModel(model);
            chunk.setDim(vectors.get(i).length);
            chunkMapper.insert(chunk);
        }
        documentMapper.updateStatus(documentId, "1", contents.size(), null);
        indexes.remove(enterpriseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long enterpriseId, Long documentId)
    {
        chunkMapper.deleteByDocument(documentId);
        documentMapper.deleteById(enterpriseId, documentId);
        indexes.remove(enterpriseId);
    }

    @Override
    public List<KbHit> search(Long enterpriseId, String query, int topK, double minScore)
    {
        if (query == null || query.trim().isEmpty())
        {
            return Collections.emptyList();
        }
        Index index = index(enterpriseId);
        if (index.chunks.isEmpty())
        {
            return Collections.emptyList();
        }
        float[] queryVector = embeddingClient.embedOne(query);
        if (queryVector == null)
        {
            return Collections.emptyList();
        }
        List<KbHit> hits = new ArrayList<>();
        for (int i = 0; i < index.chunks.size(); i++)
        {
            float[] vector = index.vectors.get(i);
            if (vector == null)
            {
                continue;
            }
            double score = VectorUtils.cosine(queryVector, vector);
            if (score >= minScore)
            {
                KbChunk chunk = index.chunks.get(i);
                hits.add(new KbHit(chunk.getChunkId(), chunk.getDocumentId(), chunk.getDocTitle(),
                        chunk.getContent(), score));
            }
        }
        hits.sort(Comparator.comparingDouble(KbHit::getScore).reversed());
        int size = Math.min(Math.max(1, topK), hits.size());
        return hits.isEmpty() ? hits : new ArrayList<>(hits.subList(0, size));
    }

    @Override
    public Map<String, Object> stats(Long enterpriseId)
    {
        Map<String, Object> result = new HashMap<>();
        List<KbDocument> documents = documentMapper.selectList(enterpriseId);
        int chunks = chunkMapper.countByEnterprise(enterpriseId);
        int ready = 0;
        for (KbDocument document : documents)
        {
            if ("1".equals(document.getStatus()))
            {
                ready++;
            }
        }
        result.put("documents", documents.size());
        result.put("readyDocuments", ready);
        result.put("chunks", chunks);
        result.put("embeddingModel", properties.getEmbedding().getModel());
        result.put("chatModel", properties.getChat().getModel());
        return result;
    }

    private Index index(Long enterpriseId)
    {
        long now = System.currentTimeMillis();
        Index cached = indexes.get(enterpriseId);
        if (cached != null && now - cached.loadedAt < CACHE_TTL_MS)
        {
            return cached;
        }
        List<KbChunk> chunks = chunkMapper.selectByEnterprise(enterpriseId);
        List<float[]> vectors = new ArrayList<>(chunks.size());
        for (KbChunk chunk : chunks)
        {
            vectors.add(VectorUtils.decode(chunk.getEmbedding()));
        }
        Index fresh = new Index(now, chunks, vectors);
        indexes.put(enterpriseId, fresh);
        return fresh;
    }

    private String extensionOf(String fileName)
    {
        if (fileName == null || !fileName.contains("."))
        {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String brief(String message)
    {
        if (message == null)
        {
            return "未知错误";
        }
        return message.length() > 400 ? message.substring(0, 400) : message;
    }

    /**
     * 单位级内存索引
     */
    private static class Index
    {
        private final long loadedAt;

        private final List<KbChunk> chunks;

        private final List<float[]> vectors;

        Index(long loadedAt, List<KbChunk> chunks, List<float[]> vectors)
        {
            this.loadedAt = loadedAt;
            this.chunks = chunks;
            this.vectors = vectors;
        }
    }
}
