package com.ruoyi.framework.app;

/**
 * 小程序请求上下文
 *
 * 由 AppAuthInterceptor 在请求开始时写入、结束时清除；
 * 业务代码只能从这里取当前用户，不接受前端传入的用户标识。
 *
 * @author cangxiaozhu
 */
public class AppContext
{
    private static final ThreadLocal<AppToken> HOLDER = new ThreadLocal<>();

    private AppContext()
    {
    }

    public static void set(AppToken token)
    {
        HOLDER.set(token);
    }

    public static AppToken get()
    {
        return HOLDER.get();
    }

    public static Long getUserId()
    {
        AppToken token = HOLDER.get();
        return token == null ? null : token.getUserId();
    }

    public static String getLoginName()
    {
        AppToken token = HOLDER.get();
        return token == null ? null : token.getLoginName();
    }

    /**
     * 当前请求所属单位，只能来自服务端令牌
     */
    public static Long getEnterpriseId()
    {
        AppToken token = HOLDER.get();
        return token == null ? null : token.getEnterpriseId();
    }

    public static void clear()
    {
        HOLDER.remove();
    }
}
