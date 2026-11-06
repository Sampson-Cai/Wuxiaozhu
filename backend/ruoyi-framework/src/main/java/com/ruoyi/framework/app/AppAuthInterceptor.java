package com.ruoyi.framework.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson.JSON;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.utils.ServletUtils;

/**
 * 小程序接口鉴权拦截器
 *
 * 校验 Bearer Token 并写入 AppContext；校验失败统一返回 200 + {"code":401}，
 * 由前端 request.js 触发重新登录（与 RuoYi-Vue 的错误码约定一致）。
 *
 * @author cangxiaozhu
 */
@Component
public class AppAuthInterceptor implements HandlerInterceptor
{
    @Autowired
    private AppTokenService appTokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception
    {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()))
        {
            return true;
        }

        String token = AppTokenUtils.resolve(request);
        AppToken payload = appTokenService.getToken(token);
        if (payload == null)
        {
            ServletUtils.renderString(response,
                    JSON.toJSONString(AppResult.error(AppResult.UNAUTHORIZED, "登录状态已过期，请重新登录")));
            return false;
        }

        AppContext.set(payload);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception
    {
        AppContext.clear();
    }
}
