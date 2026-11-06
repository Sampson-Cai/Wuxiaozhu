package com.ruoyi.web.app.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Product;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.mapper.ProductCategoryMapper;
import com.ruoyi.business.mapper.ReplenishParamMapper;
import com.ruoyi.business.mapper.StockMapper;
import com.ruoyi.business.mapper.ProductMapper;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.utils.StringUtils;

/**
 * 商品档案
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/product")
public class AppProductController extends AppBaseController
{
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductCategoryMapper categoryMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private ReplenishParamMapper replenishParamMapper;

    @GetMapping("/list")
    public AppResult list(Product query)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        query.setEnterpriseId(eid);
        List<Product> list = productMapper.selectProductList(query);
        return pageResult(list);
    }

    @GetMapping("/{productId}")
    public AppResult detail(@PathVariable Long productId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        Product product = productMapper.selectProductById(eid, productId);
        if (product == null)
        {
            return AppResult.error("商品不存在");
        }
        return AppResult.ok().put("data", product)
                .put("stock", stockMapper.selectStockByProduct(eid, productId))
                .put("replenishParam", replenishParamMapper.selectByProduct(eid, productId));
    }

    @PostMapping
    @AppPermission("business:product:edit")
    public AppResult add(@RequestBody Product product)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        String error = validate(eid, product, null);
        if (error != null)
        {
            return AppResult.error(error);
        }
        product.setEnterpriseId(eid);
        product.setCreateBy(operator());
        product.setStatus(StringUtils.isEmpty(product.getStatus()) ? "0" : product.getStatus());
        productMapper.insertProduct(product);
        return AppResult.ok("新增成功").put("productId", product.getProductId());
    }

    @PutMapping
    @AppPermission("business:product:edit")
    public AppResult edit(@RequestBody Product product)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (product.getProductId() == null)
        {
            return AppResult.error("商品ID不能为空");
        }
        if (productMapper.selectProductById(eid, product.getProductId()) == null)
        {
            return AppResult.error("商品不存在");
        }
        String error = validateUpdate(eid, product);
        if (error != null)
        {
            return AppResult.error(error);
        }
        product.setEnterpriseId(eid);
        product.setUpdateBy(operator());
        productMapper.updateProduct(product);
        return AppResult.ok("修改成功");
    }

    @PutMapping("/{productId}/status")
    @AppPermission("business:product:edit")
    public AppResult changeStatus(@PathVariable Long productId, @RequestParam String status)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (!"0".equals(status) && !"1".equals(status))
        {
            return AppResult.error("状态值不合法");
        }
        Product product = new Product();
        product.setProductId(productId);
        product.setEnterpriseId(eid);
        product.setStatus(status);
        product.setUpdateBy(operator());
        productMapper.updateProductStatus(product);
        return AppResult.ok("状态已更新");
    }

    @DeleteMapping("/{productId}")
    @AppPermission("business:product:edit")
    public AppResult remove(@PathVariable Long productId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (productMapper.selectProductById(eid, productId) == null)
        {
            return AppResult.error("商品不存在");
        }
        // 档案类单表删除的引用校验留在控制器（沿用 AppCategoryController.remove 写法），
        // 被引用时统一提示改为停用，指向 PUT /business/product/{id}/status
        if (productMapper.countPurchaseItemsByProduct(eid, productId) > 0)
        {
            return AppResult.error("该商品已被采购单引用，建议改为停用");
        }
        Stock stock = stockMapper.selectStockByProduct(eid, productId);
        if (stock != null && stock.getQty() != null && stock.getQty().compareTo(BigDecimal.ZERO) > 0)
        {
            return AppResult.error("该商品仍有库存，请先出库或改为停用");
        }
        if (productMapper.deleteProductById(eid, productId, operator()) == 0)
        {
            return AppResult.error("删除失败，商品已变化，请刷新后重试");
        }
        return AppResult.ok("删除成功");
    }

    /**
     * 编辑校验：允许只传需要修改的字段（如只改默认供应商）
     */
    private String validateUpdate(Long eid, Product product)
    {
        boolean empty = product.getProductCode() == null && product.getProductName() == null
                && product.getCategoryId() == null && product.getSupplierId() == null
                && product.getRefPrice() == null && product.getSpec() == null && product.getUnit() == null
                && product.getRemark() == null;
        if (empty)
        {
            return "没有需要修改的内容";
        }
        if (product.getProductCode() != null)
        {
            if (product.getProductCode().length() > 64)
            {
                return "商品编码过长";
            }
            if (productMapper.countByCode(eid, product.getProductCode(), product.getProductId()) > 0)
            {
                return "商品编码已存在";
            }
        }
        if (product.getProductName() != null && product.getProductName().length() > 100)
        {
            return "商品名称过长";
        }
        if (product.getCategoryId() != null
                && categoryMapper.selectCategoryById(eid, product.getCategoryId()) == null)
        {
            return "所选分类不存在";
        }
        return null;
    }

    private String validate(Long eid, Product product, Long excludeId)
    {
        if (StringUtils.isEmpty(product.getProductCode()) || StringUtils.isEmpty(product.getProductName()))
        {
            return "商品编码和商品名称不能为空";
        }
        if (product.getProductCode().length() > 64 || product.getProductName().length() > 100)
        {
            return "商品编码或名称过长";
        }
        if (productMapper.countByCode(eid, product.getProductCode(), excludeId) > 0)
        {
            return "商品编码已存在";
        }
        if (product.getCategoryId() != null
                && categoryMapper.selectCategoryById(eid, product.getCategoryId()) == null)
        {
            return "所选分类不存在";
        }
        return null;
    }
}
