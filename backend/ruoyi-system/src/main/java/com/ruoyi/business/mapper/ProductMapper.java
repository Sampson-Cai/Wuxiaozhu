package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Product;

/**
 * 商品档案
 *
 * @author cangxiaozhu
 */
public interface ProductMapper
{
    List<Product> selectProductList(Product query);

    /**
     * 按商品编码批量查询（AI 问答时补充实时价格与库存）
     */
    List<Product> selectByCodes(@Param("enterpriseId") Long enterpriseId, @Param("codes") List<String> codes);

    Product selectProductById(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId);

    int countByCode(@Param("enterpriseId") Long enterpriseId, @Param("productCode") String productCode,
            @Param("excludeId") Long excludeId);

    int insertProduct(Product product);

    int updateProduct(Product product);

    int updateProductStatus(Product product);

    /**
     * 统计有效采购单中引用该商品的明细条数，删除前置校验用
     *
     * 引用计数统一为 count{被引用记录}By{主体}，与唯一性校验的 countByCode 区分，
     * 见 doc/开发规范.md 3.3。
     */
    int countPurchaseItemsByProduct(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId);

    int deleteProductById(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId,
            @Param("updateBy") String updateBy);
}
