package com.ruoyi.web.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务开关
 *
 * 若依自带 Quartz 用于后台任务配置；补货巡检使用 Spring @Scheduled，
 * 由本配置开启调度器。
 *
 * @author cangxiaozhu
 */
@Configuration
@EnableScheduling
public class AppScheduleConfig
{
}
