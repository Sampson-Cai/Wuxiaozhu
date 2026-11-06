package com.ruoyi.web.app.controller;

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
import com.ruoyi.business.domain.Supplier;
import com.ruoyi.business.mapper.SupplierMapper;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.utils.StringUtils;

/**
 * 供应商档案
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/supplier")
public class AppSupplierController extends AppBaseController
{
    @Autowired
    private SupplierMapper supplierMapper;

    @GetMapping("/list")
    public AppResult list(Supplier query)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        query.setEnterpriseId(eid);
        List<Supplier> list = supplierMapper.selectSupplierList(query);
        return pageResult(list);
    }

    @GetMapping("/{supplierId}")
    public AppResult detail(@PathVariable Long supplierId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        Supplier supplier = supplierMapper.selectSupplierById(eid, supplierId);
        return supplier == null ? AppResult.error("供应商不存在") : AppResult.ok().put("data", supplier);
    }

    @PostMapping
    @AppPermission("business:supplier:edit")
    public AppResult add(@RequestBody Supplier supplier)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        String error = validate(eid, supplier, null);
        if (error != null)
        {
            return AppResult.error(error);
        }
        supplier.setEnterpriseId(eid);
        supplier.setCreateBy(operator());
        supplier.setStatus(StringUtils.isEmpty(supplier.getStatus()) ? "0" : supplier.getStatus());
        supplierMapper.insertSupplier(supplier);
        return AppResult.ok("新增成功").put("supplierId", supplier.getSupplierId());
    }

    @PutMapping
    @AppPermission("business:supplier:edit")
    public AppResult edit(@RequestBody Supplier supplier)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (supplier.getSupplierId() == null)
        {
            return AppResult.error("供应商ID不能为空");
        }
        if (supplierMapper.selectSupplierById(eid, supplier.getSupplierId()) == null)
        {
            return AppResult.error("供应商不存在");
        }
        String error = validateUpdate(eid, supplier);
        if (error != null)
        {
            return AppResult.error(error);
        }
        supplier.setEnterpriseId(eid);
        supplier.setUpdateBy(operator());
        supplierMapper.updateSupplier(supplier);
        return AppResult.ok("修改成功");
    }

    @PutMapping("/{supplierId}/status")
    @AppPermission("business:supplier:edit")
    public AppResult changeStatus(@PathVariable Long supplierId, @RequestParam String status)
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
        Supplier supplier = new Supplier();
        supplier.setSupplierId(supplierId);
        supplier.setEnterpriseId(eid);
        supplier.setStatus(status);
        supplier.setUpdateBy(operator());
        supplierMapper.updateSupplierStatus(supplier);
        return AppResult.ok("状态已更新");
    }

    @DeleteMapping("/{supplierId}")
    @AppPermission("business:supplier:edit")
    public AppResult remove(@PathVariable Long supplierId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (supplierMapper.selectSupplierById(eid, supplierId) == null)
        {
            return AppResult.error("供应商不存在");
        }
        // 档案类单表删除的引用校验留在控制器，提示语统一指向停用接口
        if (supplierMapper.countOrdersBySupplier(eid, supplierId) > 0)
        {
            return AppResult.error("该供应商已有采购单，建议改为停用");
        }
        // 交接单未列，本次补充：商品档案的默认供应商指向它，删除会让"建议转采购"取不到供应商
        if (supplierMapper.countProductsBySupplier(eid, supplierId) > 0)
        {
            return AppResult.error("该供应商仍是部分商品的默认供应商，请先调整商品或改为停用");
        }
        if (supplierMapper.countUnpaidPayablesBySupplier(eid, supplierId) > 0)
        {
            return AppResult.error("该供应商存在未结清应付账款，请先处理或改为停用");
        }
        if (supplierMapper.deleteSupplierById(eid, supplierId, operator()) == 0)
        {
            return AppResult.error("删除失败，供应商已变化，请刷新后重试");
        }
        return AppResult.ok("删除成功");
    }

    /**
     * 编辑校验：允许只传需要修改的字段（如只改联系人），与商品编辑保持一致
     */
    private String validateUpdate(Long eid, Supplier supplier)
    {
        boolean empty = supplier.getSupplierCode() == null && supplier.getSupplierName() == null
                && supplier.getContact() == null && supplier.getPhone() == null && supplier.getEmail() == null
                && supplier.getAddress() == null && supplier.getPayTermDays() == null
                && supplier.getLeadDays() == null && supplier.getRemark() == null;
        if (empty)
        {
            return "没有需要修改的内容";
        }
        if (supplier.getSupplierCode() != null)
        {
            if (supplier.getSupplierCode().length() > 64)
            {
                return "供应商编码过长";
            }
            if (supplierMapper.countByCode(eid, supplier.getSupplierCode(), supplier.getSupplierId()) > 0)
            {
                return "供应商编码已存在";
            }
        }
        if (supplier.getSupplierName() != null && supplier.getSupplierName().length() > 100)
        {
            return "供应商名称过长";
        }
        return null;
    }

    private String validate(Long eid, Supplier supplier, Long excludeId)
    {
        if (StringUtils.isEmpty(supplier.getSupplierCode()) || StringUtils.isEmpty(supplier.getSupplierName()))
        {
            return "供应商编码和名称不能为空";
        }
        if (supplier.getSupplierCode().length() > 64 || supplier.getSupplierName().length() > 100)
        {
            return "供应商编码或名称过长";
        }
        if (supplierMapper.countByCode(eid, supplier.getSupplierCode(), excludeId) > 0)
        {
            return "供应商编码已存在";
        }
        return null;
    }
}
