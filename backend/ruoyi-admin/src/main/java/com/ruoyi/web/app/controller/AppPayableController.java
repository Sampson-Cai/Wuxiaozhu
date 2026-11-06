package com.ruoyi.web.app.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Payable;
import com.ruoyi.business.mapper.PaymentRecordMapper;
import com.ruoyi.business.service.IPayableService;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.app.dto.PaymentRequest;

/**
 * 应付账款与付款
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/payable")
public class AppPayableController extends AppBaseController
{
    @Autowired
    private IPayableService payableService;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @GetMapping("/list")
    public AppResult list(Payable query)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        query.setEnterpriseId(eid);
        List<Payable> list = payableService.list(query);
        return pageResult(list);
    }

    @GetMapping("/{payableId}")
    public AppResult detail(@PathVariable Long payableId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        try
        {
            Payable payable = payableService.detail(eid, payableId);
            return AppResult.ok().put("data", payable)
                    .put("payments", paymentRecordMapper.selectByPayable(eid, payableId));
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PostMapping("/{payableId}/payment")
    @AppPermission("business:payable:edit")
    public AppResult pay(@PathVariable Long payableId, @RequestBody PaymentRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        try
        {
            payableService.pay(eid, userId(), operator(), payableId, request.getAmount(), request.getPayMethod(),
                    request.getRemark());
            return AppResult.ok("付款登记成功");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }
}
