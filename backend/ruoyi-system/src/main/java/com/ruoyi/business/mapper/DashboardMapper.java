package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 首页预警与趋势统计
 *
 * @author cangxiaozhu
 */
public interface DashboardMapper
{
    int countLowStock(@Param("enterpriseId") Long enterpriseId);

    int countPendingApproval(@Param("enterpriseId") Long enterpriseId);

    int countPayableDue(@Param("enterpriseId") Long enterpriseId, @Param("days") int days);

    int countPayableOverdue(@Param("enterpriseId") Long enterpriseId);

    java.math.BigDecimal sumUnpaidAmount(@Param("enterpriseId") Long enterpriseId);

    List<Map<String, Object>> purchaseTrend(@Param("enterpriseId") Long enterpriseId, @Param("days") int days);

    List<Map<String, Object>> stockTrend(@Param("enterpriseId") Long enterpriseId, @Param("days") int days);
}
