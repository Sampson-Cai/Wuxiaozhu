package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.StockRecord;

/**
 * 出入库流水
 *
 * @author cangxiaozhu
 */
public interface StockRecordMapper
{
    List<StockRecord> selectRecordList(@Param("enterpriseId") Long enterpriseId,
            @Param("productId") Long productId, @Param("recordType") String recordType,
            @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    int insertRecord(StockRecord record);

    /** 按 ID 查流水，冲销前置校验用 */
    StockRecord selectRecordById(@Param("enterpriseId") Long enterpriseId, @Param("recordId") Long recordId);

    /** 冲销后回写标记，条件带 reverse_flag = '0' 保证幂等 */
    int markReversed(@Param("enterpriseId") Long enterpriseId, @Param("recordId") Long recordId,
            @Param("reverseRecordId") Long reverseRecordId);

    /** 近 days 天按商品汇总出库量，用于补货计算 */
    java.util.List<java.util.Map<String, Object>> selectOutboundTotals(
            @Param("enterpriseId") Long enterpriseId, @Param("days") int days);
}
