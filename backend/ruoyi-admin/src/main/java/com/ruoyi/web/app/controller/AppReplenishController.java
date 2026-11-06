package com.ruoyi.web.app.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.ReplenishParam;
import com.ruoyi.business.domain.ReplenishSuggestion;
import com.ruoyi.business.mapper.ReplenishSuggestionMapper;
import com.ruoyi.business.service.IReplenishService;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.exception.ServiceException;

/**
 * 补货参数与建议
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/replenish")
public class AppReplenishController extends AppBaseController
{
    @Autowired
    private IReplenishService replenishService;

    @Autowired
    private ReplenishSuggestionMapper suggestionMapper;

    @GetMapping("/param/{productId}")
    public AppResult getParam(@PathVariable Long productId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        try
        {
            return AppResult.ok().put("data", replenishService.getParam(eid, productId));
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PutMapping("/param/{productId}")
    @AppPermission("business:replenish:edit")
    public AppResult saveParam(@PathVariable Long productId, @RequestBody ReplenishParam param)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        try
        {
            replenishService.saveParam(eid, operator(), productId, param);
            return AppResult.ok("保存成功");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @GetMapping("/suggestions")
    public AppResult suggestions(@RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        List<ReplenishSuggestion> list = suggestionMapper.selectList(eid, status, keyword);
        return pageResult(list);
    }

    /**
     * 建议转采购草稿
     */
    @PostMapping("/suggestion/{suggestionId}/to-purchase")
    @AppPermission("business:purchase:edit")
    public AppResult toPurchase(@PathVariable Long suggestionId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        try
        {
            Long orderId = replenishService.toPurchaseDraft(eid, operator(), suggestionId);
            return AppResult.ok("已生成采购草稿").put("orderId", orderId);
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    /**
     * 忽略建议
     */
    @PutMapping("/suggestion/{suggestionId}/status")
    @AppPermission("business:replenish:edit")
    public AppResult ignore(@PathVariable Long suggestionId,
            @RequestParam(defaultValue = "2") String status)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (!"2".equals(status))
        {
            return AppResult.error("当前仅支持忽略建议（status=2）");
        }
        try
        {
            replenishService.ignore(eid, suggestionId);
            return AppResult.ok("已忽略");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    /**
     * 手动触发补货计算
     */
    @PostMapping("/calc")
    @AppPermission("business:replenish:edit")
    public AppResult calc()
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        int count = replenishService.calc(eid);
        return AppResult.ok("计算完成").put("count", count);
    }
}
