package com.ruoyi.web.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.mapper.DashboardMapper;
import com.ruoyi.common.core.domain.AppResult;

/**
 * 首页预警与趋势
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/dashboard")
public class AppDashboardController extends AppBaseController
{
    @Autowired
    private DashboardMapper dashboardMapper;

    /**
     * 首页预警卡片
     */
    @GetMapping("/summary")
    public AppResult summary()
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        return AppResult.ok()
                .put("lowStock", dashboardMapper.countLowStock(eid))
                .put("pendingApproval", dashboardMapper.countPendingApproval(eid))
                .put("payableDue", dashboardMapper.countPayableDue(eid, 7))
                .put("payableOverdue", dashboardMapper.countPayableOverdue(eid))
                .put("unpaidAmount", dashboardMapper.sumUnpaidAmount(eid));
    }

    /**
     * 库存与采购趋势（近 N 天，默认 30）
     */
    @GetMapping("/trend")
    public AppResult trend(@RequestParam(required = false, defaultValue = "30") Integer days)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        int range = days == null || days <= 0 || days > 365 ? 30 : days;
        return AppResult.ok()
                .put("purchase", dashboardMapper.purchaseTrend(eid, range))
                .put("stock", dashboardMapper.stockTrend(eid, range));
    }
}
