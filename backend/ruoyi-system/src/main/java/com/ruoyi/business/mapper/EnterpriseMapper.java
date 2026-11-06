package com.ruoyi.business.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Enterprise;

/**
 * 单位信息
 *
 * @author cangxiaozhu
 */
public interface EnterpriseMapper
{
    Enterprise selectEnterpriseById(@Param("id") Long id);

    int updateEnterprise(Enterprise enterprise);

    java.util.List<Long> selectEnabledIds();
}
