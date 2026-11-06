package com.ruoyi.framework.app;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.CacheUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 小程序访问令牌服务
 *
 * 令牌为 32 字节随机串，服务端缓存保存身份与过期时间；
 * 客户端不持有任何可解析的身份信息，令牌失效由服务端主动清除。
 *
 * @author cangxiaozhu
 */
@Service
public class AppTokenService
{
    /** 缓存名称，对应 ehcache/ehcache-shiro.xml 中的 app-token */
    public static final String CACHE_NAME = "app-token";

    /** 剩余有效期低于该值时自动续期（1 天） */
    private static final long RENEW_THRESHOLD = 24 * 60 * 60 * 1000L;

    private final SecureRandom random = new SecureRandom();

    /** 令牌有效期（秒），默认 7 天 */
    @Value("${cangxiaozhu.token.expire-seconds:604800}")
    private long expireSeconds = 604800L;

    /**
     * 为用户签发令牌
     */
    public String createToken(SysUser user)
    {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        long now = System.currentTimeMillis();
        AppToken payload = new AppToken(user.getUserId(), user.getLoginName(), user.getEnterpriseId(), now,
                now + expireSeconds * 1000L);
        CacheUtils.put(CACHE_NAME, token, payload);
        return token;
    }

    /**
     * 解析令牌，过期或不存在返回 null
     */
    public AppToken getToken(String token)
    {
        if (StringUtils.isEmpty(token))
        {
            return null;
        }
        Object value = CacheUtils.get(CACHE_NAME, token);
        if (!(value instanceof AppToken))
        {
            return null;
        }
        AppToken payload = (AppToken) value;
        long now = System.currentTimeMillis();
        if (payload.getExpireTime() == null || payload.getExpireTime() < now)
        {
            CacheUtils.remove(CACHE_NAME, token);
            return null;
        }
        // 临期自动续期，避免用户在使用过程中被强制登出
        if (payload.getExpireTime() - now < RENEW_THRESHOLD)
        {
            payload.setExpireTime(now + expireSeconds * 1000L);
            CacheUtils.put(CACHE_NAME, token, payload);
        }
        return payload;
    }

    /**
     * 注销令牌
     */
    public void removeToken(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            CacheUtils.remove(CACHE_NAME, token);
        }
    }
}
