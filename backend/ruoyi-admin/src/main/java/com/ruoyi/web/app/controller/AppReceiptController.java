package com.ruoyi.web.app.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Receipt;
import com.ruoyi.business.mapper.ReceiptMapper;
import com.ruoyi.common.core.domain.AppResult;

/**
 * 收货单查询
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/receipt")
public class AppReceiptController extends AppBaseController
{
    @Autowired
    private ReceiptMapper receiptMapper;

    @GetMapping("/list")
    public AppResult list(@RequestParam(required = false) Long orderId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        List<Receipt> list = receiptMapper.selectReceiptList(eid, orderId);
        return pageResult(list);
    }

    @GetMapping("/{receiptId}")
    public AppResult detail(@PathVariable Long receiptId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        Receipt receipt = receiptMapper.selectReceiptById(eid, receiptId);
        if (receipt == null)
        {
            return AppResult.error("收货单不存在");
        }
        return AppResult.ok().put("data", receipt).put("items", receiptMapper.selectItems(receiptId));
    }
}
