package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 出入库流水
 *
 * @author cangxiaozhu
 */
public class StockRecord
{
    private Long recordId;

    private Long enterpriseId;

    private Long productId;

    private String recordType;

    private String bizType;

    private BigDecimal qty;

    private BigDecimal beforeQty;

    private BigDecimal afterQty;

    private String sourceNo;

    private String batchNo;

    private Long operatorId;

    private String operatorName;

    private String remark;

    /** 冲销标记（0 未冲销 1 已冲销） */
    private String reverseFlag;

    /** 冲销生成的流水ID */
    private Long reverseRecordId;

    private Date createTime;

    /** 商品名称，列表展示用 */
    private String productName;

    /** 商品编码，列表展示用 */
    private String productCode;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
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

    public String getRecordType()
    {
        return recordType;
    }

    public void setRecordType(String recordType)
    {
        this.recordType = recordType;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public BigDecimal getQty()
    {
        return qty;
    }

    public void setQty(BigDecimal qty)
    {
        this.qty = qty;
    }

    public BigDecimal getBeforeQty()
    {
        return beforeQty;
    }

    public void setBeforeQty(BigDecimal beforeQty)
    {
        this.beforeQty = beforeQty;
    }

    public BigDecimal getAfterQty()
    {
        return afterQty;
    }

    public void setAfterQty(BigDecimal afterQty)
    {
        this.afterQty = afterQty;
    }

    public String getSourceNo()
    {
        return sourceNo;
    }

    public void setSourceNo(String sourceNo)
    {
        this.sourceNo = sourceNo;
    }

    public String getBatchNo()
    {
        return batchNo;
    }

    public void setBatchNo(String batchNo)
    {
        this.batchNo = batchNo;
    }

    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    public String getOperatorName()
    {
        return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getReverseFlag()
    {
        return reverseFlag;
    }

    public void setReverseFlag(String reverseFlag)
    {
        this.reverseFlag = reverseFlag;
    }

    public Long getReverseRecordId()
    {
        return reverseRecordId;
    }

    public void setReverseRecordId(Long reverseRecordId)
    {
        this.reverseRecordId = reverseRecordId;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getProductCode()
    {
        return productCode;
    }

    public void setProductCode(String productCode)
    {
        this.productCode = productCode;
    }
}
