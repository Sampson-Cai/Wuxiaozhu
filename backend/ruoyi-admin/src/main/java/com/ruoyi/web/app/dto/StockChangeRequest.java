package com.ruoyi.web.app.dto;

import java.math.BigDecimal;

/**
 * 出入库请求
 *
 * @author cangxiaozhu
 */
public class StockChangeRequest
{
    private Long productId;

    private BigDecimal qty;

    /** 业务类型：采购入库/其他入库/领用出库等 */
    private String bizType;

    private String batchNo;

    private String remark;

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

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public String getBatchNo()
    {
        return batchNo;
    }

    public void setBatchNo(String batchNo)
    {
        this.batchNo = batchNo;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
