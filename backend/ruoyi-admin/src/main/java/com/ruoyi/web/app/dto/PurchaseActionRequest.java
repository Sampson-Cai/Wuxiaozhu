package com.ruoyi.web.app.dto;

import java.util.List;
import com.ruoyi.business.domain.PurchaseOrderItem;

/**
 * 采购单动作请求（审批意见 / 取消原因 / 收货备注与分批明细）
 *
 * @author cangxiaozhu
 */
public class PurchaseActionRequest
{
    private String opinion;

    private String reason;

    private String remark;

    /** 分批收货明细：只传 itemId 与 qty，不传表示全部收货 */
    private List<PurchaseOrderItem> items;

    public List<PurchaseOrderItem> getItems()
    {
        return items;
    }

    public void setItems(List<PurchaseOrderItem> items)
    {
        this.items = items;
    }

    public String getOpinion()
    {
        return opinion;
    }

    public void setOpinion(String opinion)
    {
        this.opinion = opinion;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
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
