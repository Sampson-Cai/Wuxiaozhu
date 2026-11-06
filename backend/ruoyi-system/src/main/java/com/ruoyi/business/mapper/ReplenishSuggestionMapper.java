package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.ReplenishSuggestion;

/**
 * 补货建议
 *
 * @author cangxiaozhu
 */
public interface ReplenishSuggestionMapper
{
    List<ReplenishSuggestion> selectList(@Param("enterpriseId") Long enterpriseId,
            @Param("status") String status, @Param("keyword") String keyword);

    int upsert(ReplenishSuggestion suggestion);

    int deleteByProductDate(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId,
            @Param("calcDate") Date calcDate);

    ReplenishSuggestion selectById(@Param("enterpriseId") Long enterpriseId,
            @Param("suggestionId") Long suggestionId);

    int updateStatus(@Param("enterpriseId") Long enterpriseId, @Param("suggestionId") Long suggestionId,
            @Param("status") String status, @Param("orderId") Long orderId);
}
