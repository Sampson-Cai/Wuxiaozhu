package com.ruoyi.business.ai.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.ai.domain.KbDocument;

/**
 * 知识库文档 Mapper
 *
 * @author cangxiaozhu
 */
public interface KbDocumentMapper
{
    List<KbDocument> selectList(@Param("enterpriseId") Long enterpriseId);

    KbDocument selectById(@Param("enterpriseId") Long enterpriseId, @Param("documentId") Long documentId);

    KbDocument selectByPath(@Param("enterpriseId") Long enterpriseId, @Param("filePath") String filePath);

    int insert(KbDocument document);

    int updateStatus(@Param("documentId") Long documentId, @Param("status") String status,
            @Param("chunkCount") Integer chunkCount, @Param("errorMsg") String errorMsg);

    int deleteById(@Param("enterpriseId") Long enterpriseId, @Param("documentId") Long documentId);
}
