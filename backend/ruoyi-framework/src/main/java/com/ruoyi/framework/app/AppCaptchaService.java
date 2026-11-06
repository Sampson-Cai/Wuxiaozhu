package com.ruoyi.framework.app;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.annotation.Resource;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import com.google.code.kaptcha.Producer;
import com.ruoyi.common.utils.CacheUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 小程序图片验证码
 *
 * 与 Web 端基于 Session 的验证码不同，小程序使用 uuid 绑定验证码，
 * 服务端缓存 2 分钟，校验后立即失效，避免重复使用。
 *
 * @author cangxiaozhu
 */
@Service
public class AppCaptchaService
{
    /** 缓存名称，对应 ehcache/ehcache-shiro.xml 中的 app-captcha */
    public static final String CACHE_NAME = "app-captcha";

    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    /**
     * 生成验证码图片（Base64 JPEG）并返回 uuid
     */
    public Map<String, Object> generate(String type) throws IOException
    {
        String capStr;
        String code;
        BufferedImage image;
        if ("char".equals(type))
        {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }
        else
        {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf('@'));
            code = capText.substring(capText.lastIndexOf('@') + 1);
            image = captchaProducerMath.createImage(capStr);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);

        String uuid = UUID.randomUUID().toString().replace("-", "");
        CacheUtils.put(CACHE_NAME, uuid, code);

        Map<String, Object> result = new HashMap<>(4);
        result.put("uuid", uuid);
        result.put("img", Base64.getEncoder().encodeToString(out.toByteArray()));
        return result;
    }

    /**
     * 校验验证码
     *
     * @param input       用户输入
     * @param uuid        小程序端验证码标识，为空表示 Web 表单登录
     * @param sessionCode Web 会话中保存的验证码
     */
    public boolean validate(String input, String uuid, String sessionCode)
    {
        if (StringUtils.isEmpty(input))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(uuid))
        {
            Object cached = CacheUtils.get(CACHE_NAME, uuid);
            // 一次性使用：无论成功失败都清除
            CacheUtils.remove(CACHE_NAME, uuid);
            return cached != null && input.equalsIgnoreCase(String.valueOf(cached));
        }
        return StringUtils.isNotEmpty(sessionCode) && input.equalsIgnoreCase(sessionCode);
    }
}
