package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.Product;
import com.ruoyi.business.domain.PurchaseOrder;
import com.ruoyi.business.domain.PurchaseOrderItem;
import com.ruoyi.business.domain.ReplenishParam;
import com.ruoyi.business.domain.ReplenishSuggestion;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.mapper.EnterpriseMapper;
import com.ruoyi.business.mapper.ProductMapper;
import com.ruoyi.business.mapper.ReplenishParamMapper;
import com.ruoyi.business.mapper.ReplenishSuggestionMapper;
import com.ruoyi.business.mapper.StockMapper;
import com.ruoyi.business.mapper.StockRecordMapper;
import com.ruoyi.business.service.IPurchaseService;
import com.ruoyi.business.service.IReplenishService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 补货计算
 *
 * G = max(SS, d × (T + L) × s)，Q = ceil(G − A − P)，并按最大库存封顶；
 * 在途量 P 参与抵扣，避免重复下单。
 *
 * @author cangxiaozhu
 */
@Service
public class ReplenishServiceImpl implements IReplenishService
{
    private static final Logger log = LoggerFactory.getLogger(ReplenishServiceImpl.class);

    /** 日均出库统计天数 */
    private static final int STAT_DAYS = 30;

    @Autowired
    private ReplenishParamMapper paramMapper;

    @Autowired
    private ReplenishSuggestionMapper suggestionMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @Autowired
    private IPurchaseService purchaseService;

    @Override
    public ReplenishParam getParam(Long enterpriseId, Long productId)
    {
        ReplenishParam param = paramMapper.selectByProduct(enterpriseId, productId);
        if (param != null)
        {
            return param;
        }
        Product product = productMapper.selectProductById(enterpriseId, productId);
        if (product == null)
        {
            throw new ServiceException("商品不存在");
        }
        ReplenishParam defaults = new ReplenishParam();
        defaults.setEnterpriseId(enterpriseId);
        defaults.setProductId(productId);
        defaults.setProductCode(product.getProductCode());
        defaults.setProductName(product.getProductName());
        defaults.setUnit(product.getUnit());
        defaults.setSafetyStock(BigDecimal.ZERO);
        defaults.setMaxStock(BigDecimal.ZERO);
        defaults.setCycleDays(7);
        defaults.setLeadDays(3);
        defaults.setSeasonCoef(BigDecimal.ONE);
        defaults.setEnableFlag("1");
        return defaults;
    }

    @Override
    public void saveParam(Long enterpriseId, String operator, Long productId, ReplenishParam param)
    {
        Product product = productMapper.selectProductById(enterpriseId, productId);
        if (product == null)
        {
            throw new ServiceException("商品不存在");
        }
        param.setEnterpriseId(enterpriseId);
        param.setProductId(productId);
        param.setSafetyStock(nvl(param.getSafetyStock()));
        param.setMaxStock(nvl(param.getMaxStock()));
        param.setCycleDays(param.getCycleDays() == null || param.getCycleDays() <= 0 ? 7 : param.getCycleDays());
        param.setLeadDays(param.getLeadDays() == null || param.getLeadDays() < 0 ? 3 : param.getLeadDays());
        BigDecimal season = param.getSeasonCoef() == null ? BigDecimal.ONE : param.getSeasonCoef();
        if (season.compareTo(new BigDecimal("0.80")) < 0 || season.compareTo(new BigDecimal("1.50")) > 0)
        {
            throw new ServiceException("季节系数需在 0.80 - 1.50 之间");
        }
        param.setSeasonCoef(season);
        param.setEnableFlag(StringUtils.isEmpty(param.getEnableFlag()) ? "1" : param.getEnableFlag());
        param.setUpdateBy(operator);
        paramMapper.upsert(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int calc(Long enterpriseId)
    {
        List<ReplenishParam> params = paramMapper.selectEnabled(enterpriseId);
        if (params.isEmpty())
        {
            return 0;
        }
        Map<Long, Stock> stockMap = new HashMap<>();
        for (Stock stock : stockMapper.selectStockList(enterpriseId, null, null, null))
        {
            stockMap.put(stock.getProductId(), stock);
        }
        Map<Long, BigDecimal> outboundMap = new HashMap<>();
        for (Map<String, Object> row : stockRecordMapper.selectOutboundTotals(enterpriseId, STAT_DAYS))
        {
            Object pid = row.get("productId");
            Object total = row.get("totalQty");
            if (pid != null && total != null)
            {
                outboundMap.put(Long.valueOf(pid.toString()), new BigDecimal(total.toString()));
            }
        }

        Date calcDate = truncate(new Date());
        int count = 0;
        for (ReplenishParam param : params)
        {
            Stock stock = stockMap.get(param.getProductId());
            BigDecimal qty = stock == null || stock.getQty() == null ? BigDecimal.ZERO : stock.getQty();
            BigDecimal locked = stock == null || stock.getLockedQty() == null ? BigDecimal.ZERO
                    : stock.getLockedQty();
            BigDecimal inTransit = stock == null || stock.getInTransitQty() == null ? BigDecimal.ZERO
                    : stock.getInTransitQty();
            BigDecimal available = qty.subtract(locked);

            BigDecimal totalOut = outboundMap.getOrDefault(param.getProductId(), BigDecimal.ZERO);
            BigDecimal daily = totalOut.divide(new BigDecimal(STAT_DAYS), 3, RoundingMode.HALF_UP);

            BigDecimal safety = nvl(param.getSafetyStock());
            BigDecimal max = nvl(param.getMaxStock());
            BigDecimal season = param.getSeasonCoef() == null ? BigDecimal.ONE : param.getSeasonCoef();
            int cycle = param.getCycleDays() == null ? 7 : param.getCycleDays();
            int lead = param.getLeadDays() == null ? 3 : param.getLeadDays();

            BigDecimal demand = daily.multiply(new BigDecimal(cycle + lead)).multiply(season);
            BigDecimal target = demand.max(safety);
            BigDecimal suggest = target.subtract(available).subtract(inTransit)
                    .setScale(0, RoundingMode.CEILING);
            if (max.compareTo(BigDecimal.ZERO) > 0)
            {
                BigDecimal cap = max.subtract(available).subtract(inTransit);
                if (suggest.compareTo(cap) > 0)
                {
                    suggest = cap;
                }
            }

            if (suggest.compareTo(BigDecimal.ZERO) <= 0)
            {
                suggestionMapper.deleteByProductDate(enterpriseId, param.getProductId(), calcDate);
                continue;
            }

            ReplenishSuggestion suggestion = new ReplenishSuggestion();
            suggestion.setEnterpriseId(enterpriseId);
            suggestion.setProductId(param.getProductId());
            suggestion.setCalcDate(calcDate);
            suggestion.setSuggestQty(suggest);
            suggestion.setTargetQty(target.setScale(3, RoundingMode.HALF_UP));
            suggestion.setDailyOutQty(daily);
            suggestion.setAvailableQty(available);
            suggestion.setInTransitQty(inTransit);
            suggestion.setSafetyStock(safety);
            suggestion.setMaxStock(max);
            suggestion.setSeasonCoef(season);
            suggestionMapper.upsert(suggestion);
            count++;
        }
        return count;
    }

    @Override
    public int calcAll()
    {
        int total = 0;
        for (Long enterpriseId : enterpriseMapper.selectEnabledIds())
        {
            try
            {
                total += calc(enterpriseId);
            }
            catch (Exception e)
            {
                log.error("补货计算失败, enterpriseId={}", enterpriseId, e);
            }
        }
        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long toPurchaseDraft(Long enterpriseId, String operator, Long suggestionId)
    {
        ReplenishSuggestion suggestion = suggestionMapper.selectById(enterpriseId, suggestionId);
        if (suggestion == null)
        {
            throw new ServiceException("补货建议不存在");
        }
        if (!"0".equals(suggestion.getStatus()))
        {
            throw new ServiceException("该建议已处理，不能重复转单");
        }
        Product product = productMapper.selectProductById(enterpriseId, suggestion.getProductId());
        if (product == null)
        {
            throw new ServiceException("商品不存在或已删除");
        }
        if (product.getSupplierId() == null)
        {
            throw new ServiceException("商品未设置默认供应商，请先维护商品档案");
        }

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProductId(product.getProductId());
        item.setQty(suggestion.getSuggestQty());
        item.setPrice(product.getRefPrice());

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(product.getSupplierId());
        order.setRemark("补货建议自动生成（建议ID " + suggestionId + "）");
        order.setItems(Collections.singletonList(item));

        PurchaseOrder created = purchaseService.create(enterpriseId, operator, order);
        suggestionMapper.updateStatus(enterpriseId, suggestionId, "1", created.getOrderId());
        return created.getOrderId();
    }

    @Override
    public void ignore(Long enterpriseId, Long suggestionId)
    {
        ReplenishSuggestion suggestion = suggestionMapper.selectById(enterpriseId, suggestionId);
        if (suggestion == null)
        {
            throw new ServiceException("补货建议不存在");
        }
        if (!"0".equals(suggestion.getStatus()))
        {
            throw new ServiceException("该建议已处理");
        }
        suggestionMapper.updateStatus(enterpriseId, suggestionId, "2", null);
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Date truncate(Date date)
    {
        return java.sql.Date.valueOf(new java.sql.Date(date.getTime()).toLocalDate());
    }
}
