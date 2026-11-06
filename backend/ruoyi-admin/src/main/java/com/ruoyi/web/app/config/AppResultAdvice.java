package com.ruoyi.web.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import com.alibaba.fastjson.JSON;
import com.ruoyi.common.core.domain.AppResult;

/**
 * 响应结果捕获
 *
 * 小程序接口 HTTP 状态恒为 200，业务结果在 body 的 code 里。
 * 这里把 code/msg/JSON 写入 request 属性，供审计与幂等拦截器在 afterCompletion 中判断成败。
 *
 * @author cangxiaozhu
 */
@ControllerAdvice
public class AppResultAdvice implements ResponseBodyAdvice<Object>
{
    public static final String ATTR_CODE = "cangxiaozhu.result.code";

    public static final String ATTR_MSG = "cangxiaozhu.result.msg";

    public static final String ATTR_JSON = "cangxiaozhu.result.json";

    @Override
    public boolean supports(MethodParameter returnType, Class converterType)
    {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response)
    {
        if (!(body instanceof AppResult))
        {
            return body;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null)
        {
            HttpServletRequest servletRequest = attributes.getRequest();
            AppResult result = (AppResult) body;
            servletRequest.setAttribute(ATTR_CODE, result.get(AppResult.CODE_TAG));
            servletRequest.setAttribute(ATTR_MSG, result.get(AppResult.MSG_TAG));
            String json = JSON.toJSONString(result);
            servletRequest.setAttribute(ATTR_JSON, json.length() > 2000 ? json.substring(0, 2000) : json);
        }
        return body;
    }
}
