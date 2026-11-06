package com.ruoyi.web.app.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.domain.StockRecord;
import com.ruoyi.business.mapper.StockMapper;
import com.ruoyi.business.mapper.StockRecordMapper;
import com.ruoyi.business.service.IStockService;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.app.dto.StockChangeRequest;

/**
 * 库存与出入库
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/stock")
public class AppStockController extends AppBaseController
{
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Autowired
    private IStockService stockService;

    @GetMapping("/list")
    public AppResult list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean lowStockOnly)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        List<Stock> list = stockMapper.selectStockList(eid, keyword, categoryId, lowStockOnly);
        list.forEach(this::fillStatus);
        return pageResult(list);
    }

    @GetMapping("/records")
    public AppResult records(@RequestParam(required = false) Long productId,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        List<StockRecord> list = stockRecordMapper.selectRecordList(eid, productId, recordType, beginTime, endTime);
        return pageResult(list);
    }

    @GetMapping("/{productId}")
    public AppResult detail(@PathVariable Long productId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        Stock stock = stockMapper.selectStockByProduct(eid, productId);
        if (stock == null)
        {
            return AppResult.error("该商品暂无库存记录");
        }
        fillStatus(stock);
        return AppResult.ok().put("data", stock);
    }

    @PostMapping("/inbound")
    @AppPermission("business:stock:edit")
    public AppResult inbound(@RequestBody StockChangeRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (request.getProductId() == null || request.getQty() == null)
        {
            return AppResult.error("商品和数量不能为空");
        }
        String bizType = StringUtils.isEmpty(request.getBizType()) ? "其他入库" : request.getBizType();
        try
        {
            stockService.inbound(eid, userId(), operator(), request.getProductId(), request.getQty(), bizType,
                    null, request.getBatchNo(), request.getRemark());
            return AppResult.ok("入库成功");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    @PostMapping("/outbound")
    @AppPermission("business:stock:edit")
    public AppResult outbound(@RequestBody StockChangeRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (request.getProductId() == null || request.getQty() == null)
        {
            return AppResult.error("商品和数量不能为空");
        }
        String bizType = StringUtils.isEmpty(request.getBizType()) ? "领用出库" : request.getBizType();
        try
        {
            stockService.outbound(eid, userId(), operator(), request.getProductId(), request.getQty(), bizType,
                    request.getRemark());
            return AppResult.ok("出库成功");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    /**
     * 冲销手工出入库流水：生成反向流水，原流水标记为已冲销
     */
    @PostMapping("/records/{recordId}/reverse")
    @AppPermission("business:stock:edit")
    public AppResult reverse(@PathVariable Long recordId, @RequestBody(required = false) Map<String, String> body)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        String remark = body == null ? null : body.get("remark");
        try
        {
            stockService.reverse(eid, userId(), operator(), recordId, remark);
            return AppResult.ok("冲销成功");
        }
        catch (ServiceException e)
        {
            return AppResult.error(e.getMessage());
        }
    }

    /**
     * 计算可用量与库存状态：0 正常 / 1 低于安全库存 / 2 缺货
     */
    private void fillStatus(Stock stock)
    {
        BigDecimal qty = stock.getQty() == null ? BigDecimal.ZERO : stock.getQty();
        BigDecimal locked = stock.getLockedQty() == null ? BigDecimal.ZERO : stock.getLockedQty();
        BigDecimal available = qty.subtract(locked);
        stock.setAvailableQty(available);
        if (qty.compareTo(BigDecimal.ZERO) <= 0)
        {
            stock.setStockStatus("2");
        }
        else if (stock.getSafetyStock() != null && available.compareTo(stock.getSafetyStock()) <= 0)
        {
            stock.setStockStatus("1");
        }
        else
        {
            stock.setStockStatus("0");
        }
    }
}
