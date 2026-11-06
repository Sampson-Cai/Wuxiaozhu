package com.ruoyi.web.app.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.ProductSearchService;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.utils.StringUtils;

/**
 * 智能检索
 *
 * 输入自然语言或关键词，返回按相关度排序的商品，供"我要找配套螺丝"这类查询使用。
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/search")
public class AppSearchController extends AppBaseController
{
    @Autowired
    private ProductSearchService productSearchService;

    @GetMapping("/product")
    public AppResult searchProduct(@RequestParam("q") String question,
            @RequestParam(required = false, defaultValue = "10") Integer limit)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (StringUtils.isEmpty(question))
        {
            return AppResult.error("请输入检索内容");
        }
        int size = limit == null || limit <= 0 || limit > 50 ? 10 : limit;
        List<Map<String, Object>> rows = productSearchService.search(eid, question, size);
        return AppResult.ok().put("rows", rows).put("total", rows.size()).put("query", question);
    }
}
