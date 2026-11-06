package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.ProductCategory;

/**
 * 商品分类
 *
 * @author cangxiaozhu
 */
public interface ProductCategoryMapper
{
    List<ProductCategory> selectCategoryList(@Param("enterpriseId") Long enterpriseId);

    ProductCategory selectCategoryById(@Param("enterpriseId") Long enterpriseId,
            @Param("categoryId") Long categoryId);

    int countProductsByCategory(@Param("enterpriseId") Long enterpriseId, @Param("categoryId") Long categoryId);

    int countChildren(@Param("enterpriseId") Long enterpriseId, @Param("categoryId") Long categoryId);

    int insertCategory(ProductCategory category);

    int updateCategory(ProductCategory category);

    int deleteCategoryById(@Param("enterpriseId") Long enterpriseId, @Param("categoryId") Long categoryId,
            @Param("updateBy") String updateBy);
}
