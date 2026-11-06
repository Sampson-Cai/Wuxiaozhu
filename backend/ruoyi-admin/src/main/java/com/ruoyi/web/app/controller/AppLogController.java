package com.ruoyi.web.app.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.service.ISysOperLogService;

/**
 * 操作日志查询
 *
 * 只读接口，供管理员按人、模块、时间、结果排查；写日志由审计拦截器自动完成。
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/log")
public class AppLogController extends AppBaseController
{
    @Autowired
    private ISysOperLogService operLogService;

    @GetMapping("/list")
    @AppPermission("business:log:view")
    public AppResult list(@RequestParam(required = false) String title,
            @RequestParam(required = false) String operName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        SysOperLog query = new SysOperLog();
        query.setTitle(title);
        query.setOperName(operName);
        query.setStatus(status);
        if (StringUtils.isNotEmpty(beginTime))
        {
            query.getParams().put("beginTime", beginTime);
        }
        if (StringUtils.isNotEmpty(endTime))
        {
            query.getParams().put("endTime", endTime);
        }
        List<SysOperLog> list = operLogService.selectOperLogList(query);
        return pageResult(list);
    }

    @GetMapping("/{operId}")
    @AppPermission("business:log:view")
    public AppResult detail(@PathVariable Long operId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        SysOperLog log = operLogService.selectOperLogById(operId);
        return log == null ? AppResult.error("日志不存在") : AppResult.ok().put("data", log);
    }
}
