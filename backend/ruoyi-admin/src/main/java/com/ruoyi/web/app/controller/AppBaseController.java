package com.ruoyi.web.app.controller;

import java.util.List;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.framework.app.AppContext;

/**
 * 小程序业务控制器基类
 *
 * 统一取出当前单位、当前用户，并输出 {code:200, rows, total} 分页结构。
 *
 * @author cangxiaozhu
 */
public class AppBaseController extends BaseController
{
    protected Long enterpriseId()
    {
        return AppContext.getEnterpriseId();
    }

    protected Long userId()
    {
        return AppContext.getUserId();
    }

    protected String operator()
    {
        return AppContext.getLoginName();
    }

    protected AppResult unauthorized()
    {
        return AppResult.error(AppResult.UNAUTHORIZED, "登录状态已过期，请重新登录");
    }

    protected AppResult pageResult(List<?> list)
    {
        TableDataInfo data = getDataTable(list);
        return AppResult.ok().put("rows", data.getRows()).put("total", data.getTotal());
    }
}
