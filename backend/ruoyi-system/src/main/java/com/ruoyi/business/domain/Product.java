package com.ruoyi.business.domain;

import java.math.BigDecimal;

/**
 * 商品档案
 *
 * @author cangxiaozhu
 */
public class Product extends BizBase
{
    private Long productId;

    private Long categoryId;

    private String productCode;

    private String productName;

    private String spec;

    private String unit;

    private BigDecimal refPrice;

    private Long supplierId;

    private String status;

    private String delFlag;

    /** 商品图片地址（/profile/... 相对路径） */
    private String imageUrl;

    /** 分类名称，列表展示用 */
    private String categoryName;

    /** 现存量，列表展示用 */
    private BigDecimal stockQty;

    /** 可用量 = 现存 − 锁定，AI 实时数据用 */
    private BigDecimal availableQty;

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getProductCode()
    {
        return productCode;
    }

    public void setProductCode(String productCode)
    {
        this.productCode = productCode;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getSpec()
    {
        return spec;
    }

    public void setSpec(String spec)
    {
        this.spec = spec;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public BigDecimal getRefPrice()
    {
        return refPrice;
    }

    public void setRefPrice(BigDecimal refPrice)
    {
        this.refPrice = refPrice;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
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

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public BigDecimal getStockQty()
    {
        return stockQty;
    }

    public void setStockQty(BigDecimal stockQty)
    {
        this.stockQty = stockQty;
    }

    public BigDecimal getAvailableQty()
    {
        return availableQty;
    }

    public void setAvailableQty(BigDecimal availableQty)
    {
        this.availableQty = availableQty;
    }
}
