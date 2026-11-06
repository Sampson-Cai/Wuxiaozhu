package com.ruoyi.web.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Enterprise;
import com.ruoyi.business.mapper.EnterpriseMapper;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;

/**
 * 单位信息
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/enterprise")
public class AppEnterpriseController extends AppBaseController
{
    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @GetMapping
    public AppResult info()
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        Enterprise enterprise = enterpriseMapper.selectEnterpriseById(eid);
        if (enterprise == null)
        {
            return AppResult.error("单位信息不存在");
        }
        return AppResult.ok().put("data", enterprise);
    }

    @PutMapping
    @AppPermission("business:enterprise:edit")
    public AppResult update(@RequestBody Enterprise enterprise)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        // 单位标识只取令牌，忽略请求体中的 id
        enterprise.setId(eid);
        enterpriseMapper.updateEnterprise(enterprise);
        return AppResult.ok("保存成功");
    }
}
