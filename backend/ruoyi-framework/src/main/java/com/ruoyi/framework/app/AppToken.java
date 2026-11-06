package com.ruoyi.framework.app;

import java.io.Serializable;

/**
 * 小程序访问令牌载荷
 *
 * 仅保存服务端需要的身份信息，不含任何可被客户端解析的凭据。
 *
 * @author cangxiaozhu
 */
public class AppToken implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 登录名称 */
    private String loginName;

    /** 所属单位ID，服务端注入，客户端不可指定 */
    private Long enterpriseId;

    /** 签发时间（毫秒） */
    private Long loginTime;

    /** 过期时间（毫秒） */
    private Long expireTime;

    public AppToken()
    {
    }

    public AppToken(Long userId, String loginName, Long enterpriseId, Long loginTime, Long expireTime)
    {
        this.userId = userId;
        this.loginName = loginName;
        this.enterpriseId = enterpriseId;
        this.loginTime = loginTime;
        this.expireTime = expireTime;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getLoginName()
    {
        return loginName;
    }

    public void setLoginName(String loginName)
    {
        this.loginName = loginName;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    public Long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }
}
