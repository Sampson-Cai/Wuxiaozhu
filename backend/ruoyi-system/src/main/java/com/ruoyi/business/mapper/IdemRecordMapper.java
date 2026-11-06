package com.ruoyi.business.mapper;

import java.util.Date;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.IdemRecord;

/**
 * 幂等记录
 *
 * @author cangxiaozhu
 */
public interface IdemRecordMapper
{
    int insertRecord(IdemRecord record);

    IdemRecord selectByKey(@Param("enterpriseId") Long enterpriseId, @Param("requestId") String requestId);

    int updateStatus(@Param("idemId") Long idemId, @Param("status") String status,
            @Param("resultSummary") String resultSummary);

    int updateToProcessing(@Param("idemId") Long idemId);

    int deleteBefore(@Param("time") Date time);
}
