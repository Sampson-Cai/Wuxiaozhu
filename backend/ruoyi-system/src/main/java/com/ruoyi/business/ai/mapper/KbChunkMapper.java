package com.ruoyi.business.ai.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.ai.domain.KbChunk;

/**
 * 知识库片段 Mapper
 *
 * @author cangxiaozhu
 */
public interface KbChunkMapper
{
    int deleteByDocument(@Param("documentId") Long documentId);

    int insert(KbChunk chunk);

    /**
     * 取出单位下全部片段（含向量），用于内存检索
     */
    List<KbChunk> selectByEnterprise(@Param("enterpriseId") Long enterpriseId);

    int countByEnterprise(@Param("enterpriseId") Long enterpriseId);

    List<KbChunk> selectByDocument(@Param("documentId") Long documentId);
}
