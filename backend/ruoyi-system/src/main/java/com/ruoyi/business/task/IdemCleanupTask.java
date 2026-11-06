package com.ruoyi.business.task;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.business.mapper.IdemRecordMapper;

/**
 * 幂等记录清理
 *
 * 默认保留 7 天，每天 03:30 清理，可用 cangxiaozhu.task.idem-clean-cron 调整。
 *
 * @author cangxiaozhu
 */
@Component
public class IdemCleanupTask
{
    private static final Logger log = LoggerFactory.getLogger(IdemCleanupTask.class);

    private static final long KEEP_DAYS = 7;

    @Autowired
    private IdemRecordMapper idemRecordMapper;

    @Scheduled(cron = "${cangxiaozhu.task.idem-clean-cron:0 30 3 * * ?}")
    public void run()
    {
        try
        {
            Date before = new Date(System.currentTimeMillis() - KEEP_DAYS * 86400000L);
            int count = idemRecordMapper.deleteBefore(before);
            log.info("幂等记录清理完成，删除 {} 条", count);
        }
        catch (Exception e)
        {
            log.error("幂等记录清理失败", e);
        }
    }
}
