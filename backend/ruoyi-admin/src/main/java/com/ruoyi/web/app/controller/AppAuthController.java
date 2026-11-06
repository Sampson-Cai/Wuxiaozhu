package com.ruoyi.web.app.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.framework.app.AppCaptchaService;
import com.ruoyi.framework.app.AppContext;
import com.ruoyi.framework.app.AppToken;
import com.ruoyi.framework.app.AppTokenService;
import com.ruoyi.framework.app.AppTokenUtils;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 小程序认证接口
 *
 * 约定与前端 request.js 一致：
 * 登录成功后返回 {code:200, token}；
 * /getInfo 返回 {code:200, user, roles, permissions}；
 * 令牌失效返回 {code:401}。
 *
 * @author cangxiaozhu
 */
@Controller
public class AppAuthController
{
    @Autowired
    private AppTokenService appTokenService;

    @Autowired
    private AppCaptchaService appCaptchaService;

    @Autowired
    private ISysUserService userService;

    /** 验证码开关，关闭时前端不再渲染验证码输入框 */
    @Value("${shiro.user.captchaEnabled:true}")
    private boolean captchaEnabled;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysMenuService menuService;

    /**
     * 图形验证码，返回 Base64 图片与 uuid
     */
    @GetMapping("/captchaImage")
    @ResponseBody
    public AppResult captchaImage(@RequestParam(value = "type", required = false) String type) throws IOException
    {
        if (!captchaEnabled)
        {
            // 关闭验证码时不生成图片，前端据此隐藏输入框
            return AppResult.ok().put("captchaEnabled", false);
        }
        Map<String, Object> captcha = appCaptchaService.generate(type);
        return AppResult.ok().put("captchaEnabled", true)
                .put("uuid", captcha.get("uuid")).put("img", captcha.get("img"));
    }

    /**
     * 当前登录用户、角色与权限
     */
    @GetMapping("/getInfo")
    @ResponseBody
    public AppResult getInfo()
    {
        AppToken token = AppContext.get();
        if (token == null)
        {
            return AppResult.error(AppResult.UNAUTHORIZED, "登录状态已过期，请重新登录");
        }
        SysUser user = userService.selectUserById(token.getUserId());
        if (user == null || !"0".equals(user.getStatus()))
        {
            return AppResult.error(AppResult.UNAUTHORIZED, "账号不存在或已停用");
        }

        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        if (user.isAdmin())
        {
            roles.add("admin");
            permissions.add("*:*:*");
        }
        else
        {
            Set<String> roleKeys = roleService.selectRoleKeys(user.getUserId());
            if (roleKeys != null)
            {
                roles.addAll(roleKeys);
            }
            Set<String> perms = menuService.selectPermsByUserId(user.getUserId());
            if (perms != null)
            {
                permissions.addAll(perms);
            }
        }

        Map<String, Object> profile = new HashMap<>(16);
        profile.put("userId", user.getUserId());
        profile.put("userName", user.getLoginName());
        profile.put("nickName", user.getUserName());
        profile.put("email", user.getEmail());
        profile.put("phonenumber", user.getPhonenumber());
        profile.put("avatar", user.getAvatar());
        profile.put("deptId", user.getDeptId());
        profile.put("loginDate", user.getLoginDate());

        return AppResult.ok().put("user", profile).put("roles", roles).put("permissions", permissions);
    }

    /**
     * 小程序退出：清除服务端令牌
     */
    @PostMapping("/logout")
    @ResponseBody
    public AppResult logout(HttpServletRequest request)
    {
        appTokenService.removeToken(AppTokenUtils.resolve(request));
        return AppResult.ok("退出成功");
    }

    /**
     * 后台退出：沿用原有行为，清理会话后跳转登录页
     */
    @GetMapping("/logout")
    public String logoutPage()
    {
        SecurityUtils.getSubject().logout();
        return "redirect:/login";
    }
}
