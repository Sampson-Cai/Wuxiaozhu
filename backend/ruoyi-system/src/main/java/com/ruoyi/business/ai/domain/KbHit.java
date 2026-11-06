package com.ruoyi.business.ai.domain;

/**
 * 检索命中的知识片段
 *
 * @author cangxiaozhu
 */
public class KbHit
{
    private Long chunkId;

    private Long documentId;

    private String docTitle;

    private String content;

    private double score;

    public KbHit()
    {
    }

    public KbHit(Long chunkId, Long documentId, String docTitle, String content, double score)
    {
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.docTitle = docTitle;
        this.content = content;
        this.score = score;
    }

    public Long getChunkId()
    {
        return chunkId;
    }

    public void setChunkId(Long chunkId)
    {
        this.chunkId = chunkId;
    }

    public Long getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(Long documentId)
    {
        this.documentId = documentId;
    }

    public String getDocTitle()
    {
        return docTitle;
    }

    public void setDocTitle(String docTitle)
    {
        this.docTitle = docTitle;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public double getScore()
    {
        return score;
    }

    public void setScore(double score)
    {
        this.score = score;
    }
}
