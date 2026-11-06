package com.ruoyi.web.controller.system;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.code.kaptcha.Constants;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.app.AppCaptchaService;
import com.ruoyi.framework.app.AppTokenService;
import com.ruoyi.framework.web.service.ConfigService;

/**
 * 登录验证
 *
 * 同时服务 Web 控制台与小程序：
 * Web 端依赖 Shiro 会话，小程序端使用返回的 token 访问 /getInfo 与业务接口。
 *
 * @author ruoyi
 */
@Controller
public class SysLoginController
{
    /**
     * 是否开启记住我功能
     */
    @Value("${shiro.rememberMe.enabled: false}")
    private boolean rememberMe;

    /**
     * 是否开启验证码
     */
    @Value("${shiro.user.captchaEnabled: true}")
    private boolean captchaEnabled;

    @Autowired
    private ConfigService configService;

    @Autowired
    private AppTokenService appTokenService;

    @Autowired
    private AppCaptchaService appCaptchaService;

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, ModelMap mmap)
    {
        // 如果是Ajax请求，返回Json字符串。
        if (ServletUtils.isAjaxRequest(request))
        {
            return ServletUtils.renderString(response,
                    "{\"code\":401,\"msg\":\"未登录或登录超时。请重新登录\"}");
        }
        // 是否开启记住我
        mmap.put("isRemembered", rememberMe);
        // 是否开启用户注册
        mmap.put("isAllowRegister", Convert.toBool(configService.getKey("sys.account.registerUser"), false));
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public AppResult ajaxLogin(String username, String password, Boolean rememberMe,
            String validateCode, String code, String uuid)
    {
        if (!validateCaptcha(code, validateCode, uuid))
        {
            return AppResult.error(AppResult.FAIL, "验证码错误");
        }

        UsernamePasswordToken token = new UsernamePasswordToken(username, password,
                rememberMe != null && rememberMe);
        Subject subject = SecurityUtils.getSubject();
        try
        {
            subject.login(token);
        }
        catch (AuthenticationException e)
        {
            String msg = "用户或密码错误";
            if (StringUtils.isNotEmpty(e.getMessage()))
            {
                msg = e.getMessage();
            }
            return AppResult.error(AppResult.FAIL, msg);
        }

        SysUser user = ShiroUtils.getSysUser();
        String accessToken = appTokenService.createToken(user);
        return AppResult.ok("登录成功").put("token", accessToken);
    }

    /**
     * 验证码校验：小程序携带 uuid，Web 表单使用会话中的验证码
     */
    private boolean validateCaptcha(String code, String validateCode, String uuid)
    {
        if (!captchaEnabled)
        {
            return true;
        }
        String input = StringUtils.isNotEmpty(code) ? code : validateCode;
        String sessionCode = null;
        if (StringUtils.isEmpty(uuid))
        {
            Object cached = ShiroUtils.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);
            sessionCode = cached == null ? null : String.valueOf(cached);
            ShiroUtils.getSession().removeAttribute(Constants.KAPTCHA_SESSION_KEY);
        }
        return appCaptchaService.validate(input, uuid, sessionCode);
    }

    @GetMapping("/unauth")
    public String unauth()
    {
        return "error/unauth";
    }
}
