package com.ruoyi.framework.app;

import jakarta.servlet.http.HttpServletRequest;
import com.ruoyi.common.utils.StringUtils;

/**
 * 小程序令牌解析工具
 *
 * 统一约定：Authorization: Bearer &lt;token&gt;，兼容 X-Token 头，
 * 便于个别不支持自定义头的场景调试。
 *
 * @author cangxiaozhu
 */
public class AppTokenUtils
{
    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String HEADER_TOKEN = "X-Token";

    private AppTokenUtils()
    {
    }

    public static String resolve(HttpServletRequest request)
    {
        String header = request.getHeader(HEADER_AUTHORIZATION);
        if (StringUtils.isNotEmpty(header) && header.startsWith("Bearer "))
        {
            return header.substring(7).trim();
        }
        String token = request.getHeader(HEADER_TOKEN);
        if (StringUtils.isNotEmpty(token))
        {
            return token.trim();
        }
        // 小程序下载文件的场景无法设置请求头，允许通过参数传递
        return request.getParameter("token");
    }
}
