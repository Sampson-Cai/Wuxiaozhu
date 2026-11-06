package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Supplier;

/**
 * 供应商档案
 *
 * @author cangxiaozhu
 */
public interface SupplierMapper
{
    List<Supplier> selectSupplierList(Supplier query);

    Supplier selectSupplierById(@Param("enterpriseId") Long enterpriseId, @Param("supplierId") Long supplierId);

    int countByCode(@Param("enterpriseId") Long enterpriseId, @Param("supplierCode") String supplierCode,
            @Param("excludeId") Long excludeId);

    int insertSupplier(Supplier supplier);

    int updateSupplier(Supplier supplier);

    int updateSupplierStatus(Supplier supplier);

    /**
     * 统计有效采购单数，删除前置校验用
     *
     * 引用计数统一为 count{被引用记录}By{主体}，见 doc/开发规范.md 3.3。
     */
    int countOrdersBySupplier(@Param("enterpriseId") Long enterpriseId, @Param("supplierId") Long supplierId);

    /** 统计该供应商未结清（status != '2'）的应付笔数，删除前置校验用 */
    int countUnpaidPayablesBySupplier(@Param("enterpriseId") Long enterpriseId,
            @Param("supplierId") Long supplierId);

    /** 统计把该供应商设为默认供应商的有效商品数，删除前置校验用 */
    int countProductsBySupplier(@Param("enterpriseId") Long enterpriseId, @Param("supplierId") Long supplierId);

    int deleteSupplierById(@Param("enterpriseId") Long enterpriseId, @Param("supplierId") Long supplierId,
            @Param("updateBy") String updateBy);
}
