package com.ruoyi.framework.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson.JSON;
import com.ruoyi.business.domain.IdemRecord;
import com.ruoyi.business.mapper.IdemRecordMapper;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 写接口幂等
 *
 * 客户端为每个写请求生成 X-Request-Id；服务端以 (enterprise_id, request_id) 唯一键占位：
 *   - 首次请求：落"处理中"，继续执行，结束后标记成功/失败；
 *   - 重复请求：处理中或已成功 → 返回 409，不再执行业务；
 *   - 上次失败：允许用同一 requestId 重试。
 *
 * GET/HEAD/OPTIONS 与导出下载不参与。
 *
 * @author cangxiaozhu
 */
@Component
public class AppIdempotentInterceptor implements HandlerInterceptor
{
    private static final Logger log = LoggerFactory.getLogger(AppIdempotentInterceptor.class);

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    private static final String ATTR_IDEM_ID = "cangxiaozhu.idemId";

    @Value("${cangxiaozhu.idempotent.enabled:true}")
    private boolean enabled;

    @Autowired
    private IdemRecordMapper idemRecordMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception
    {
        if (!enabled || isSafeMethod(request.getMethod()))
        {
            return true;
        }
        Long enterpriseId = AppContext.getEnterpriseId();
        Long userId = AppContext.getUserId();
        if (enterpriseId == null)
        {
            return true;
        }

        String requestId = request.getHeader(HEADER_REQUEST_ID);
        if (StringUtils.isEmpty(requestId))
        {
            requestId = request.getParameter("requestId");
        }
        if (StringUtils.isEmpty(requestId) || requestId.length() > 64)
        {
            reject(request, response, 500, "缺少幂等键：请在请求头携带 X-Request-Id（UUID）");
            return false;
        }

        IdemRecord record = new IdemRecord();
        record.setEnterpriseId(enterpriseId);
        record.setUserId(userId);
        record.setRequestId(requestId);
        record.setApi(request.getMethod() + " " + request.getRequestURI());

        try
        {
            idemRecordMapper.insertRecord(record);
            request.setAttribute(ATTR_IDEM_ID, record.getIdemId());
            return true;
        }
        catch (DuplicateKeyException e)
        {
            IdemRecord exists = idemRecordMapper.selectByKey(enterpriseId, requestId);
            if (exists != null && "2".equals(exists.getStatus()))
            {
                // 上次执行失败，允许重试
                idemRecordMapper.updateToProcessing(exists.getIdemId());
                request.setAttribute(ATTR_IDEM_ID, exists.getIdemId());
                return true;
            }
            reject(request, response, 409, "该请求已处理，请勿重复提交");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception
    {
        Object idemId = request.getAttribute(ATTR_IDEM_ID);
        if (idemId == null)
        {
            return;
        }
        try
        {
            Object code = request.getAttribute("cangxiaozhu.result.code");
            boolean failed = ex != null || (code != null && !"200".equals(String.valueOf(code)));
            String status = failed ? "2" : "1";
            String summary = ex != null ? truncate(ex.getMessage())
                    : (failed ? truncate(String.valueOf(request.getAttribute("cangxiaozhu.result.msg"))) : "OK");
            idemRecordMapper.updateStatus((Long) idemId, status, summary);
        }
        catch (Exception e)
        {
            log.warn("幂等记录回写失败: {}", e.getMessage());
        }
    }

    /**
     * 拒绝请求：同时写入结果属性，保证审计能记录为失败
     */
    private void reject(HttpServletRequest request, HttpServletResponse response, int code, String msg)
    {
        AppResult result = AppResult.error(code, msg);
        request.setAttribute("cangxiaozhu.result.code", code);
        request.setAttribute("cangxiaozhu.result.msg", msg);
        request.setAttribute("cangxiaozhu.result.json", JSON.toJSONString(result));
        ServletUtils.renderString(response, JSON.toJSONString(result));
    }

    private boolean isSafeMethod(String method)
    {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method);
    }

    private String truncate(String text)
    {
        if (text == null)
        {
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }
}
