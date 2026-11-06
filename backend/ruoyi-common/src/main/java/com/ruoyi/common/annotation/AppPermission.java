package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 小程序接口权限校验
 *
 * 标注在 Controller 方法上，由 AppPermissionInterceptor 校验当前用户是否具备该权限；
 * 管理员（userId=1）与拥有 *:*:* 的角色直接放行。
 *
 * @author cangxiaozhu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AppPermission
{
    /** 权限标识，如 business:product:edit */
    String value();
}
