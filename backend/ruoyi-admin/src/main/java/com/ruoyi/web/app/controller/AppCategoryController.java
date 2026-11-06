package com.ruoyi.web.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.ProductCategory;
import com.ruoyi.business.mapper.ProductCategoryMapper;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.utils.StringUtils;

/**
 * 商品分类
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/category")
public class AppCategoryController extends AppBaseController
{
    @Autowired
    private ProductCategoryMapper categoryMapper;

    @GetMapping("/list")
    public AppResult list()
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        return AppResult.ok().put("rows", categoryMapper.selectCategoryList(eid));
    }

    /**
     * 分类详情
     */
    @GetMapping("/{categoryId}")
    public AppResult detail(@PathVariable Long categoryId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        ProductCategory category = categoryMapper.selectCategoryById(eid, categoryId);
        return category == null ? AppResult.error("分类不存在") : AppResult.ok().put("data", category);
    }

    @PostMapping
    @AppPermission("business:category:edit")
    public AppResult add(@RequestBody ProductCategory category)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (StringUtils.isEmpty(category.getName()))
        {
            return AppResult.error("分类名称不能为空");
        }
        category.setEnterpriseId(eid);
        category.setParentId(category.getParentId() == null ? 0L : category.getParentId());
        category.setSort(category.getSort() == null ? 0 : category.getSort());
        category.setStatus(StringUtils.isEmpty(category.getStatus()) ? "0" : category.getStatus());
        category.setCreateBy(operator());
        categoryMapper.insertCategory(category);
        return AppResult.ok("新增成功").put("categoryId", category.getCategoryId());
    }

    @PutMapping
    @AppPermission("business:category:edit")
    public AppResult edit(@RequestBody ProductCategory category)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (category.getCategoryId() == null)
        {
            return AppResult.error("分类ID不能为空");
        }
        if (categoryMapper.selectCategoryById(eid, category.getCategoryId()) == null)
        {
            return AppResult.error("分类不存在");
        }
        if (category.getCategoryId().equals(category.getParentId()))
        {
            return AppResult.error("上级分类不能是自己");
        }
        category.setEnterpriseId(eid);
        category.setUpdateBy(operator());
        categoryMapper.updateCategory(category);
        return AppResult.ok("修改成功");
    }

    @DeleteMapping("/{categoryId}")
    @AppPermission("business:category:edit")
    public AppResult remove(@PathVariable Long categoryId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (categoryMapper.selectCategoryById(eid, categoryId) == null)
        {
            return AppResult.error("分类不存在");
        }
        if (categoryMapper.countChildren(eid, categoryId) > 0)
        {
            return AppResult.error("存在子分类，不能删除");
        }
        if (categoryMapper.countProductsByCategory(eid, categoryId) > 0)
        {
            return AppResult.error("分类下存在商品，不能删除");
        }
        if (categoryMapper.deleteCategoryById(eid, categoryId, operator()) == 0)
        {
            return AppResult.error("删除失败，分类已变化，请刷新后重试");
        }
        return AppResult.ok("删除成功");
    }
}
