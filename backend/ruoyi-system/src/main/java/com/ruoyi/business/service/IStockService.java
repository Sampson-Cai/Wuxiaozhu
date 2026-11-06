package com.ruoyi.business.service;

import java.math.BigDecimal;

/**
 * 库存业务
 *
 * @author cangxiaozhu
 */
public interface IStockService
{
    /**
     * 入库：更新库存并写流水
     */
    void inbound(Long enterpriseId, Long operatorId, String operatorName, Long productId, BigDecimal qty,
            String bizType, String sourceNo, String batchNo, String remark);

    /**
     * 出库：校验可用库存后扣减并写流水
     */
    void outbound(Long enterpriseId, Long operatorId, String operatorName, Long productId, BigDecimal qty,
            String bizType, String remark);

    /**
     * 冲销手工出入库流水：生成反向流水并回写冲销标记
     *
     * 仅允许冲销无来源单号（source_no 为空）的手工流水；采购入库等单据来源流水需走原单据流程。
     */
    void reverse(Long enterpriseId, Long operatorId, String operatorName, Long recordId, String remark);
}
