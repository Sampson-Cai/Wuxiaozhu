package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存
 *
 * @author cangxiaozhu
 */
public class Stock
{
    private Long stockId;

    private Long enterpriseId;

    private Long productId;

    private BigDecimal qty;

    private BigDecimal lockedQty;

    private BigDecimal inTransitQty;

    private Date updateTime;

    /** 商品编码 */
    private String productCode;

    /** 商品名称 */
    private String productName;

    /** 规格 */
    private String spec;

    /** 单位 */
    private String unit;

    /** 安全库存，来自补货参数 */
    private BigDecimal safetyStock;

    /** 可用库存 = 现存 - 锁定 */
    private BigDecimal availableQty;

    /** 库存状态：0 正常 1 低于安全库存 2 缺货 */
    private String stockStatus;

    public Long getStockId()
    {
        return stockId;
    }

    public void setStockId(Long stockId)
    {
        this.stockId = stockId;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public BigDecimal getQty()
    {
        return qty;
    }

    public void setQty(BigDecimal qty)
    {
        this.qty = qty;
    }

    public BigDecimal getLockedQty()
    {
        return lockedQty;
    }

    public void setLockedQty(BigDecimal lockedQty)
    {
        this.lockedQty = lockedQty;
    }

    public BigDecimal getInTransitQty()
    {
        return inTransitQty;
    }

    public void setInTransitQty(BigDecimal inTransitQty)
    {
        this.inTransitQty = inTransitQty;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
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

    public BigDecimal getSafetyStock()
    {
        return safetyStock;
    }

    public void setSafetyStock(BigDecimal safetyStock)
    {
        this.safetyStock = safetyStock;
    }

    public BigDecimal getAvailableQty()
    {
        return availableQty;
    }

    public void setAvailableQty(BigDecimal availableQty)
    {
        this.availableQty = availableQty;
    }

    public String getStockStatus()
    {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus)
    {
        this.stockStatus = stockStatus;
    }
}
