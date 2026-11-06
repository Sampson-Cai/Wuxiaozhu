package com.ruoyi.web.app.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.ruoyi.business.domain.PurchaseOrder;
import com.ruoyi.business.service.IPurchaseService;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ServletUtils;

/**
 * 采购单 PDF
 *
 * 仅已通过、已完成的单据可导出；审批意见随单据输出。
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/purchase")
public class AppPurchasePdfController extends AppBaseController
{
    @Autowired
    private IPurchaseService purchaseService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    /** 中文字体，默认取 Windows 自带等线；部署到 Linux 时改为服务器字体路径 */
    @Value("${cangxiaozhu.pdf.font-path:C:/Windows/Fonts/Deng.ttf}")
    private String fontPath;

    @GetMapping("/{orderId}/pdf")
    public void pdf(@PathVariable Long orderId, HttpServletResponse response) throws IOException
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            renderLoginError(response);
            return;
        }
        try
        {
            PurchaseOrder order = purchaseService.detail(eid, orderId);
            if (!"2".equals(order.getStatus()) && !"4".equals(order.getStatus()))
            {
                ServletUtils.renderString(response,
                        com.alibaba.fastjson.JSON.toJSONString(AppResult.error("仅已通过或已完成的采购单可导出 PDF")));
                return;
            }

            Context context = new Context(Locale.CHINA);
            context.setVariable("order", order);
            context.setVariable("items", order.getItems());
            context.setVariable("approvals", order.getApprovals());
            String html = templateEngine.process("business/purchase-pdf", context);

            response.setContentType("application/pdf");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(order.getOrderNo() + ".pdf", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "inline; filename=" + fileName);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            File font = new File(fontPath);
            if (font.exists())
            {
                builder.useFont(font, "PdfCJK");
            }
            builder.withHtmlContent(html, null);
            builder.toStream(response.getOutputStream());
            builder.run();
        }
        catch (ServiceException e)
        {
            ServletUtils.renderString(response,
                    com.alibaba.fastjson.JSON.toJSONString(AppResult.error(e.getMessage())));
        }
    }

    private void renderLoginError(HttpServletResponse response)
    {
        ServletUtils.renderString(response,
                com.alibaba.fastjson.JSON.toJSONString(AppResult.error(AppResult.UNAUTHORIZED, "登录状态已过期，请重新登录")));
    }
}
