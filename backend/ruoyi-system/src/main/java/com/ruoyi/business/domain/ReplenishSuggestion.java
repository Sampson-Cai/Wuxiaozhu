package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 补货建议
 *
 * 状态：0 待处理 / 1 已建单 / 2 已忽略
 *
 * @author cangxiaozhu
 */
public class ReplenishSuggestion
{
    private Long suggestionId;

    private Long enterpriseId;

    private Long productId;

    private Date calcDate;

    private BigDecimal suggestQty;

    private BigDecimal targetQty;

    private BigDecimal dailyOutQty;

    private BigDecimal availableQty;

    private BigDecimal inTransitQty;

    private BigDecimal safetyStock;

    private BigDecimal maxStock;

    private BigDecimal seasonCoef;

    private String status;

    /** 生成的采购单ID */
    private Long orderId;

    private Date createTime;

    private String productCode;

    private String productName;

    private String unit;

    public Long getSuggestionId()
    {
        return suggestionId;
    }

    public void setSuggestionId(Long suggestionId)
    {
        this.suggestionId = suggestionId;
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

    public Date getCalcDate()
    {
        return calcDate;
    }

    public void setCalcDate(Date calcDate)
    {
        this.calcDate = calcDate;
    }

    public BigDecimal getSuggestQty()
    {
        return suggestQty;
    }

    public void setSuggestQty(BigDecimal suggestQty)
    {
        this.suggestQty = suggestQty;
    }

    public BigDecimal getTargetQty()
    {
        return targetQty;
    }

    public void setTargetQty(BigDecimal targetQty)
    {
        this.targetQty = targetQty;
    }

    public BigDecimal getDailyOutQty()
    {
        return dailyOutQty;
    }

    public void setDailyOutQty(BigDecimal dailyOutQty)
    {
        this.dailyOutQty = dailyOutQty;
    }

    public BigDecimal getAvailableQty()
    {
        return availableQty;
    }

    public void setAvailableQty(BigDecimal availableQty)
    {
        this.availableQty = availableQty;
    }

    public BigDecimal getInTransitQty()
    {
        return inTransitQty;
    }

    public void setInTransitQty(BigDecimal inTransitQty)
    {
        this.inTransitQty = inTransitQty;
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

    public BigDecimal getSeasonCoef()
    {
        return seasonCoef;
    }

    public void setSeasonCoef(BigDecimal seasonCoef)
    {
        this.seasonCoef = seasonCoef;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
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

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }
}
