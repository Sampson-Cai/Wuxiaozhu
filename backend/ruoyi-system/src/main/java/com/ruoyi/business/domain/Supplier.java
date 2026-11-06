package com.ruoyi.business.domain;

/**
 * 供应商档案
 *
 * @author cangxiaozhu
 */
public class Supplier extends BizBase
{
    private Long supplierId;

    private String supplierCode;

    private String supplierName;

    private String contact;

    private String phone;

    private String email;

    private String address;

    private Integer payTermDays;

    private Integer leadDays;

    private String status;

    private String delFlag;

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getSupplierCode()
    {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode)
    {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getContact()
    {
        return contact;
    }

    public void setContact(String contact)
    {
        this.contact = contact;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public Integer getPayTermDays()
    {
        return payTermDays;
    }

    public void setPayTermDays(Integer payTermDays)
    {
        this.payTermDays = payTermDays;
    }

    public Integer getLeadDays()
    {
        return leadDays;
    }

    public void setLeadDays(Integer leadDays)
    {
        this.leadDays = leadDays;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
