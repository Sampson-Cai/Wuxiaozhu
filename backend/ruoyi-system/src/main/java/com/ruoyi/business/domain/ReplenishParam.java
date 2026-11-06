package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 补货参数
 *
 * @author cangxiaozhu
 */
public class ReplenishParam
{
    private Long paramId;

    private Long enterpriseId;

    private Long productId;

    private BigDecimal safetyStock;

    private BigDecimal maxStock;

    private Integer cycleDays;

    private Integer leadDays;

    private BigDecimal seasonCoef;

    private String enableFlag;

    private String updateBy;

    private Date updateTime;

    /** 展示用：商品信息 */
    private String productCode;

    private String productName;

    private String unit;

    public Long getParamId()
    {
        return paramId;
    }

    public void setParamId(Long paramId)
    {
        this.paramId = paramId;
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

    public BigDecimal getSafetyStock()
    {
        return safetyStock;
    }

    public void setSafetyStock(BigDecimal safetyStock)
    {
        this.safetyStock = safetyStock;
    }

    public BigDecimal getMaxStock()
    {
        return maxStock;
    }

    public void setMaxStock(BigDecimal maxStock)
    {
        this.maxStock = maxStock;
    }

    public Integer getCycleDays()
    {
        return cycleDays;
    }

    public void setCycleDays(Integer cycleDays)
    {
        this.cycleDays = cycleDays;
    }

    public Integer getLeadDays()
    {
        return leadDays;
    }

    public void setLeadDays(Integer leadDays)
    {
        this.leadDays = leadDays;
    }

    public BigDecimal getSeasonCoef()
    {
        return seasonCoef;
    }

    public void setSeasonCoef(BigDecimal seasonCoef)
    {
        this.seasonCoef = seasonCoef;
    }

    public String getEnableFlag()
    {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag)
    {
        this.enableFlag = enableFlag;
    }

    public String getUpdateBy()
    {
        return updateBy;
    }

    public void setUpdateBy(String updateBy)
    {
        this.updateBy = updateBy;
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

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }
}
