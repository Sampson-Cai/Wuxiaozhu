import com.ruoyi.business.ai.service.impl.AiChatServiceImpl;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.mapper.DashboardMapper;
import com.ruoyi.business.mapper.StockMapper;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 直接调用已编译的 AiChatServiceImpl.snapshot()，验证库存数值口径。 */
public class SnapshotVerify {
    static void set(Object target, String name, Object value) throws Exception {
        Field f = AiChatServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @SuppressWarnings("unchecked")
    static <T> T mock(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
    }

    public static void main(String[] args) throws Exception {
        // 模拟数据库真实 6 条预警：现存 5、锁定 0、安全 5。
        // 关键：availableQty 故意设为 null，复现旧 mapper 未填充该字段的场景。
        String[] names = {
            "套筒组 1\"方驱 24-85mm", "圆柱滚子轴承 NU214E", "圆柱滚子轴承 NU216E",
            "圆柱滚子轴承 NU218E", "链轮 16B-1 35齿", "皮带轮 C型3槽 外径250mm"
        };
        List<Stock> stocks = new ArrayList<>();
        for (String name : names) {
            Stock s = new Stock();
            s.setProductName(name);
            s.setQty(new BigDecimal("5"));
            s.setLockedQty(BigDecimal.ZERO);
            s.setSafetyStock(new BigDecimal("5"));
            s.setAvailableQty(null);
            stocks.add(s);
        }

        StockMapper stockMapper = mock(StockMapper.class, (p, m, a) -> {
            if ("selectStockList".equals(m.getName())) return stocks;
            return null;
        });
        DashboardMapper dashboard = mock(DashboardMapper.class, (p, m, a) -> {
            switch (m.getName()) {
                case "countLowStock": return 6;
                case "countPendingApproval": return 0;
                case "countPayableDue": return 0;
                case "countPayableOverdue": return 0;
                case "sumUnpaidAmount": return BigDecimal.ZERO;
                default: return null;
            }
        });

        AiChatServiceImpl service = new AiChatServiceImpl();
        set(service, "stockMapper", stockMapper);
        set(service, "dashboardMapper", dashboard);

        Method snapshot = AiChatServiceImpl.class.getDeclaredMethod("snapshot", Long.class);
        snapshot.setAccessible(true);
        String text = (String) snapshot.invoke(service, 1L);
        System.out.println("=== 生成的实时业务快照 ===");
        System.out.println(text);
        System.out.println("=== 断言 ===");
        check("快照声明预警 6 个", text.contains("库存预警 6 个"));
        check("快照列出全部 6 项", text.contains("共 6 个"));
        check("每项都含链轮/轴承/套筒/皮带轮", text.contains("链轮 16B-1 35齿")
                && text.contains("圆柱滚子轴承 NU216E") && text.contains("套筒组 1\"方驱 24-85mm")
                && text.contains("皮带轮 C型3槽 外径250mm"));
        check("不再出现『可用 0』", !text.contains("可用 0"));
        check("6 项全部显示『可用 5/安全 5』", count(text, "(可用 5/安全 5)") == 6,
                "实际 " + count(text, "(可用 5/安全 5)") + " 处");
        check("声明可用 = 现存 − 锁定", text.contains("可用 = 现存 − 锁定"));
    }

    static int count(String text, String sub) {
        int n = 0, i = 0;
        while ((i = text.indexOf(sub, i)) >= 0) { n++; i += sub.length(); }
        return n;
    }

    static void check(String name, boolean ok) { check(name, ok, ""); }

    static void check(String name, boolean ok, String detail) {
        System.out.println((ok ? "PASS " : "FAIL ") + name + (detail.isEmpty() ? "" : " — " + detail));
        if (!ok) failed = true;
    }

    static boolean failed = false;
}
