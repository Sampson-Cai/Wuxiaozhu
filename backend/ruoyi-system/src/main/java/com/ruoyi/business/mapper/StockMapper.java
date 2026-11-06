package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Stock;

/**
 * 库存
 *
 * @author cangxiaozhu
 */
public interface StockMapper
{
    List<Stock> selectStockList(@Param("enterpriseId") Long enterpriseId, @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId, @Param("lowStockOnly") Boolean lowStockOnly);

    Stock selectStockByProduct(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId);

    /** 行锁查询，出入库时使用 */
    Stock selectStockForUpdate(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId);

    int insertStock(Stock stock);

    int addStockQty(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId,
            @Param("qty") BigDecimal qty);

    int subtractStockQty(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId,
            @Param("qty") BigDecimal qty);

    /** 采购审批通过后累加在途量 */
    int addInTransit(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId,
            @Param("qty") BigDecimal qty);

    /** 收货入库时冲减在途量 */
    int subtractInTransit(@Param("enterpriseId") Long enterpriseId, @Param("productId") Long productId,
            @Param("qty") BigDecimal qty);
}
