package com.ruoyi.web.app.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Payable;
import com.ruoyi.business.domain.PurchaseOrder;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.mapper.PayableMapper;
import com.ruoyi.business.mapper.PurchaseOrderMapper;
import com.ruoyi.business.mapper.StockMapper;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.utils.ServletUtils;

/**
 * Excel 导出（采购单 / 库存 / 应付）
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/export")
public class AppExportController extends AppBaseController
{
    private static final String STATUS_TEXT[] = { "草稿", "待审批", "已通过", "已驳回", "已完成", "已取消" };

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private PayableMapper payableMapper;

    @GetMapping("/purchase")
    @AppPermission("business:export")
    public void purchase(HttpServletResponse response) throws IOException
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            ServletUtils.renderString(response,
                    com.alibaba.fastjson.JSON.toJSONString(AppResult.error(AppResult.UNAUTHORIZED, "登录状态已过期")));
            return;
        }
        PurchaseOrder query = new PurchaseOrder();
        query.setEnterpriseId(eid);
        List<PurchaseOrder> list = purchaseOrderMapper.selectOrderList(query);

        try (XSSFWorkbook workbook = new XSSFWorkbook())
        {
            Sheet sheet = workbook.createSheet("采购单");
            String[] headers = { "采购单号", "供应商", "总金额", "状态", "创建人", "创建时间", "审批人", "审批时间" };
            writeHeader(workbook, sheet, headers);
            int rowIndex = 1;
            for (PurchaseOrder order : list)
            {
                Row row = sheet.createRow(rowIndex++);
                write(row, 0, order.getOrderNo());
                write(row, 1, order.getSupplierName());
                write(row, 2, order.getTotalAmount());
                write(row, 3, statusText(order.getStatus()));
                write(row, 4, order.getCreateBy());
                write(row, 5, format(order.getCreateTime()));
                write(row, 6, order.getApproveBy());
                write(row, 7, format(order.getApproveTime()));
            }
            autoSize(sheet, headers.length);
            download(workbook, response, "采购单");
        }
    }

    @GetMapping("/stock")
    @AppPermission("business:export")
    public void stock(HttpServletResponse response) throws IOException
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            ServletUtils.renderString(response,
                    com.alibaba.fastjson.JSON.toJSONString(AppResult.error(AppResult.UNAUTHORIZED, "登录状态已过期")));
            return;
        }
        List<Stock> list = stockMapper.selectStockList(eid, null, null, null);
        try (XSSFWorkbook workbook = new XSSFWorkbook())
        {
            Sheet sheet = workbook.createSheet("库存");
            String[] headers = { "商品编码", "商品名称", "规格", "单位", "现存", "锁定", "可用", "在途" };
            writeHeader(workbook, sheet, headers);
            int rowIndex = 1;
            for (Stock stock : list)
            {
                Row row = sheet.createRow(rowIndex++);
                write(row, 0, stock.getProductCode());
                write(row, 1, stock.getProductName());
                write(row, 2, stock.getSpec());
                write(row, 3, stock.getUnit());
                write(row, 4, stock.getQty());
                write(row, 5, stock.getLockedQty());
                BigDecimal available = stock.getQty() == null ? BigDecimal.ZERO : stock.getQty();
                if (stock.getLockedQty() != null)
                {
                    available = available.subtract(stock.getLockedQty());
                }
                write(row, 6, available);
                write(row, 7, stock.getInTransitQty());
            }
            autoSize(sheet, headers.length);
            download(workbook, response, "库存");
        }
    }

    @GetMapping("/payable")
    @AppPermission("business:export")
    public void payable(HttpServletResponse response) throws IOException
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            ServletUtils.renderString(response,
                    com.alibaba.fastjson.JSON.toJSONString(AppResult.error(AppResult.UNAUTHORIZED, "登录状态已过期")));
            return;
        }
        Payable query = new Payable();
        query.setEnterpriseId(eid);
        List<Payable> list = payableMapper.selectPayableList(query);

        try (XSSFWorkbook workbook = new XSSFWorkbook())
        {
            Sheet sheet = workbook.createSheet("应付账款");
            String[] headers = { "应付单号", "采购单ID", "供应商ID", "应付金额", "已付金额", "到期日", "状态" };
            writeHeader(workbook, sheet, headers);
            int rowIndex = 1;
            for (Payable payable : list)
            {
                Row row = sheet.createRow(rowIndex++);
                write(row, 0, payable.getPayableNo());
                write(row, 1, payable.getOrderId());
                write(row, 2, payable.getSupplierId());
                write(row, 3, payable.getTotalAmount());
                write(row, 4, payable.getPaidAmount());
                write(row, 5, format(payable.getDueDate()));
                write(row, 6, "0".equals(payable.getStatus()) ? "未付款" : ("1".equals(payable.getStatus()) ? "部分付款" : "已结清"));
            }
            autoSize(sheet, headers.length);
            download(workbook, response, "应付账款");
        }
    }

    private void writeHeader(XSSFWorkbook workbook, Sheet sheet, String[] headers)
    {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++)
        {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void write(Row row, int index, Object value)
    {
        Cell cell = row.createCell(index);
        if (value == null)
        {
            cell.setCellValue("");
        }
        else if (value instanceof Number)
        {
            cell.setCellValue(((Number) value).doubleValue());
        }
        else
        {
            cell.setCellValue(value.toString());
        }
    }

    private void autoSize(Sheet sheet, int columns)
    {
        for (int i = 0; i < columns; i++)
        {
            sheet.setColumnWidth(i, 18 * 256);
        }
    }

    private String statusText(String status)
    {
        try
        {
            return STATUS_TEXT[Integer.parseInt(status)];
        }
        catch (Exception e)
        {
            return status;
        }
    }

    private String format(Date date)
    {
        return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    private void download(XSSFWorkbook workbook, HttpServletResponse response, String name) throws IOException
    {
        String fileName = URLEncoder.encode(name + "_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xlsx",
                StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        workbook.write(response.getOutputStream());
    }
}
