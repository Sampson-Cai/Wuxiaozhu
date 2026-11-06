package com.ruoyi.business.ai.domain;

import java.util.Date;

/**
 * 知识库片段（含向量）
 *
 * @author cangxiaozhu
 */
public class KbChunk
{
    private Long chunkId;

    private Long enterpriseId;

    private Long documentId;

    private Integer chunkIndex;

    private Integer pageNo;

    private String content;

    private Integer tokenCount;

    private String vectorRef;

    /** float32 向量的 Base64，见 VectorUtils */
    private String embedding;

    private String embeddingModel;

    private Integer dim;

    private Date createTime;

    /** 文档标题，检索结果展示用 */
    private String docTitle;

    public Long getChunkId()
    {
        return chunkId;
    }

    public void setChunkId(Long chunkId)
    {
        this.chunkId = chunkId;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(Long documentId)
    {
        this.documentId = documentId;
    }

    public Integer getChunkIndex()
    {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex)
    {
        this.chunkIndex = chunkIndex;
    }

    public Integer getPageNo()
    {
        return pageNo;
    }

    public void setPageNo(Integer pageNo)
    {
        this.pageNo = pageNo;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Integer getTokenCount()
    {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount)
    {
        this.tokenCount = tokenCount;
    }

    public String getVectorRef()
    {
        return vectorRef;
    }

    public void setVectorRef(String vectorRef)
    {
        this.vectorRef = vectorRef;
    }

    public String getEmbedding()
    {
        return embedding;
    }

    public void setEmbedding(String embedding)
    {
        this.embedding = embedding;
    }

    public String getEmbeddingModel()
    {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel)
    {
        this.embeddingModel = embeddingModel;
    }

    public Integer getDim()
    {
        return dim;
    }

    public void setDim(Integer dim)
    {
        this.dim = dim;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public String getDocTitle()
    {
        return docTitle;
    }

    public void setDocTitle(String docTitle)
    {
        this.docTitle = docTitle;
    }
}
