package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.ReplenishParam;

/**
 * 补货参数
 *
 * @author cangxiaozhu
 */
public interface ReplenishParamMapper
{
    ReplenishParam selectByProduct(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId);

    List<ReplenishParam> selectEnabled(@Param("enterpriseId") Long enterpriseId);

    int upsert(ReplenishParam param);
}
