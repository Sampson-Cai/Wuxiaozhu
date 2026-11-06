package com.ruoyi.framework.app;

import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.ruoyi.common.utils.IpUtils;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.service.ISysOperLogService;

/**
 * 写操作审计
 *
 * 对 /business/** 下的非查询请求统一记录到若依 sys_oper_log：
 * 谁、什么时候、调了哪个接口、耗时、成功与否、IP。
 *
 * 不使用 @Log 注解，是因为若依的 LogAspect 依赖 Shiro 会话，
 * 而小程序请求走无状态令牌，拿不到 SysUser。
 *
 * @author cangxiaozhu
 */
@Component
public class AppAuditInterceptor implements HandlerInterceptor
{
    private static final Logger log = LoggerFactory.getLogger(AppAuditInterceptor.class);

    /** 若依操作类型：0 其它 */
    private static final int BUSINESS_TYPE_OTHER = 0;

    /** 若依操作人类别：2 移动端 */
    private static final int OPERATOR_TYPE_MOBILE = 2;

    private static final String ATTR_START = "cangxiaozhu.auditStart";

    @Autowired
    private ISysOperLogService operLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception
    {
        if (!isSafeMethod(request.getMethod()))
        {
            request.setAttribute(ATTR_START, System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception
    {
        Object start = request.getAttribute(ATTR_START);
        if (start == null)
        {
            return;
        }
        try
        {
            SysOperLog operLog = new SysOperLog();
            operLog.setTitle(truncate(request.getRequestURI(), 50));
            operLog.setBusinessType(BUSINESS_TYPE_OTHER);
            operLog.setMethod(handler instanceof HandlerMethod
                    ? truncate(((HandlerMethod) handler).getBeanType().getSimpleName() + "."
                            + ((HandlerMethod) handler).getMethod().getName(), 200)
                    : "");
            operLog.setRequestMethod(request.getMethod());
            operLog.setOperatorType(OPERATOR_TYPE_MOBILE);
            operLog.setOperName(AppContext.getLoginName());
            operLog.setOperUrl(truncate(request.getRequestURI(), 255));
            operLog.setOperIp(truncate(IpUtils.getIpAddr(request), 128));
            operLog.setOperParam(truncate(request.getQueryString(), 2000));
            Object code = request.getAttribute("cangxiaozhu.result.code");
            boolean failed = ex != null || (code != null && !"200".equals(String.valueOf(code)));
            operLog.setStatus(failed ? 1 : 0);
            if (ex != null)
            {
                operLog.setErrorMsg(truncate(ex.getMessage(), 2000));
            }
            else if (failed)
            {
                Object msg = request.getAttribute("cangxiaozhu.result.msg");
                operLog.setErrorMsg(msg == null ? null : truncate(String.valueOf(msg), 2000));
            }
            Object json = request.getAttribute("cangxiaozhu.result.json");
            operLog.setJsonResult(json == null ? null : truncate(String.valueOf(json), 2000));
            operLog.setCostTime(System.currentTimeMillis() - (Long) start);
            operLog.setOperTime(new Date());
            operLogService.insertOperlog(operLog);
        }
        catch (Exception e)
        {
            // 审计失败不能影响业务
            log.warn("写操作审计落库失败: {}", e.getMessage());
        }
    }

    private boolean isSafeMethod(String method)
    {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method);
    }

    private String truncate(String text, int max)
    {
        if (text == null)
        {
            return null;
        }
        return text.length() > max ? text.substring(0, max) : text;
    }
}
