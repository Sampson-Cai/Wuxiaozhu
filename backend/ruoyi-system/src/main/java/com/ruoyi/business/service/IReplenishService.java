package com.ruoyi.business.service;

import com.ruoyi.business.domain.ReplenishParam;

/**
 * 补货参数与建议
 *
 * @author cangxiaozhu
 */
public interface IReplenishService
{
    ReplenishParam getParam(Long enterpriseId, Long productId);

    void saveParam(Long enterpriseId, String operator, Long productId, ReplenishParam param);

    /** 按单位计算补货建议，返回生成条数 */
    int calc(Long enterpriseId);

    /** 定时任务入口：遍历所有启用单位 */
    int calcAll();

    /** 建议转采购草稿，返回采购单ID */
    Long toPurchaseDraft(Long enterpriseId, String operator, Long suggestionId);

    /** 忽略建议 */
    void ignore(Long enterpriseId, Long suggestionId);
}
