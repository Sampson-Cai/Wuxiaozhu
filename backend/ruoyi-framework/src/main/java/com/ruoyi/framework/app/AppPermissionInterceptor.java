package com.ruoyi.framework.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson.JSON;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.utils.ServletUtils;

/**
 * 小程序接口权限拦截器
 *
 * 只处理标注了 {@link AppPermission} 的方法；未标注视为登录即可访问。
 *
 * @author cangxiaozhu
 */
@Component
public class AppPermissionInterceptor implements HandlerInterceptor
{
    @Autowired
    private AppPermissionService appPermissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception
    {
        if (!(handler instanceof HandlerMethod))
        {
            return true;
        }
        AppPermission annotation = ((HandlerMethod) handler).getMethodAnnotation(AppPermission.class);
        if (annotation == null)
        {
            return true;
        }
        Long userId = AppContext.getUserId();
        if (userId == null)
        {
            ServletUtils.renderString(response,
                    JSON.toJSONString(AppResult.error(AppResult.UNAUTHORIZED, "登录状态已过期，请重新登录")));
            return false;
        }
        if (!appPermissionService.hasPermission(userId, annotation.value()))
        {
            ServletUtils.renderString(response,
                    JSON.toJSONString(AppResult.error(AppResult.FORBIDDEN, "没有操作权限：" + annotation.value())));
            return false;
        }
        return true;
    }
}
