package com.ruoyi.web.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.ruoyi.framework.app.AppAuditInterceptor;
import com.ruoyi.framework.app.AppAuthInterceptor;
import com.ruoyi.framework.app.AppIdempotentInterceptor;
import com.ruoyi.framework.app.AppPermissionInterceptor;

/**
 * 小程序接口配置
 *
 * 拦截顺序：鉴权 → 授权 → 审计 → 幂等。
 * 审计放在幂等之前，被幂等拦截的重复请求同样留痕。
 *
 * @author cangxiaozhu
 */
@Configuration
public class AppWebConfig implements WebMvcConfigurer
{
    @Autowired
    private AppAuthInterceptor appAuthInterceptor;

    @Autowired
    private AppPermissionInterceptor appPermissionInterceptor;

    @Autowired
    private AppAuditInterceptor appAuditInterceptor;

    @Autowired
    private AppIdempotentInterceptor appIdempotentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(appAuthInterceptor)
                .addPathPatterns("/getInfo", "/business/**", "/api/app/**")
                .excludePathPatterns("/business/public/**")
                .order(1);

        registry.addInterceptor(appPermissionInterceptor)
                .addPathPatterns("/business/**", "/api/app/**")
                .order(2);

        registry.addInterceptor(appAuditInterceptor)
                .addPathPatterns("/business/**")
                .order(3);

        registry.addInterceptor(appIdempotentInterceptor)
                .addPathPatterns("/business/**")
                .order(4);
    }
}
