package com.ruoyi.business.ai.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.ai.domain.KbDocument;
import com.ruoyi.business.ai.domain.KbHit;

/**
 * 知识库服务：文档入库、切片、向量化、检索
 *
 * @author cangxiaozhu
 */
public interface IKbService
{
    /**
     * 文档列表
     */
    List<KbDocument> listDocuments(Long enterpriseId);

    /**
     * 导入文本内容（切片 + 向量化）
     */
    KbDocument importText(Long enterpriseId, String title, String fileName, String filePath, String docType,
            String content, String uploadBy);

    /**
     * 导入服务器本地文件（.md / .txt / .csv）
     */
    KbDocument importFile(Long enterpriseId, String filePath, String docType, String uploadBy);

    /**
     * 重新向量化（切片变了或换模型时用）
     */
    void reindex(Long enterpriseId, Long documentId);

    /**
     * 删除文档及其片段
     */
    void deleteDocument(Long enterpriseId, Long documentId);

    /**
     * 语义检索
     *
     * @param query 问题
     * @param topK 召回条数
     * @param minScore 相似度下限
     */
    List<KbHit> search(Long enterpriseId, String query, int topK, double minScore);

    /**
     * 知识库统计
     */
    Map<String, Object> stats(Long enterpriseId);
}
