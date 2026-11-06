package com.ruoyi.business.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.business.service.IReplenishService;

/**
 * 补货计算定时任务
 *
 * 默认每天 02:00 执行，可通过 cangxiaozhu.task.replenish-cron 调整。
 *
 * @author cangxiaozhu
 */
@Component
public class ReplenishTask
{
    private static final Logger log = LoggerFactory.getLogger(ReplenishTask.class);

    @Autowired
    private IReplenishService replenishService;

    @Scheduled(cron = "${cangxiaozhu.task.replenish-cron:0 0 2 * * ?}")
    public void run()
    {
        try
        {
            int count = replenishService.calcAll();
            log.info("库存巡检与补货计算完成，生成建议 {} 条", count);
        }
        catch (Exception e)
        {
            log.error("库存巡检与补货计算失败", e);
        }
    }
}
