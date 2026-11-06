package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.Product;
import com.ruoyi.business.mapper.ProductMapper;

/**
 * 商品智能检索
 *
 * 做法：把自然语言问题拆成"概念"，每个概念带一组近义词；
 * 商品只要命中任一近义词，就算命中该概念，再按字段权重（名称 3、规格/编码 2、分类/备注 1）打分。
 *
 * 例："找到一台电脑的配套螺丝"
 *   概念1 = 电脑（近义：计算机、笔记本、PC、台式机）
 *   概念2 = 螺丝（近义：螺钉、螺栓、紧固件、螺帽）
 *   "笔记本专用螺丝 M2×5" 命中两个概念 → 相关度 1.00
 *
 * 这是词法 + 同义词层，可解释、零外部依赖；
 * 真正的语义关系（配套、兼容、可替代）需接入 Embedding 向量检索，见 doc/接口说明.md。
 *
 * @author cangxiaozhu
 */
@Service
public class ProductSearchService
{
    /** 近义词表：命中左词时，扩展出右词作为同一概念的别名 */
    private static final Map<String, List<String>> SYNONYMS = new HashMap<>();

    /** 口语中无检索价值的词 */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "吗", "呢", "一个", "一台", "一批", "一些", "帮我", "帮忙", "我想", "我要",
            "找到", "查找", "查询", "搜索", "有没有", "什么", "哪些", "哪个", "多少", "库存",
            "需要", "采购", "买", "找", "看看", "一下", "配套", "相关", "用于", "用来"));

    static
    {
        SYNONYMS.put("电脑", Arrays.asList("计算机", "笔记本", "PC", "台式机"));
        SYNONYMS.put("笔记本", Arrays.asList("电脑", "便携", "Laptop"));
        SYNONYMS.put("螺丝", Arrays.asList("螺钉", "螺栓", "紧固件", "螺帽"));
        SYNONYMS.put("螺钉", Arrays.asList("螺丝", "螺栓"));
        SYNONYMS.put("纸", Arrays.asList("复印纸", "纸张", "A4"));
        SYNONYMS.put("打印", Arrays.asList("打印机", "墨盒", "硒鼓", "打印纸"));
        SYNONYMS.put("笔", Arrays.asList("签字笔", "中性笔", "圆珠笔", "铅笔"));
        SYNONYMS.put("办公", Arrays.asList("办公用品", "文具"));
        SYNONYMS.put("线", Arrays.asList("数据线", "电源线", "网线"));
    }

    /** 长词补充二元片段，避免用户输入整句商品名时匹配不到 */
    private static final int GRAM_SIZE = 2;

    private static final int GRAM_TRIGGER_LENGTH = 4;

    @Autowired
    private ProductMapper productMapper;

    public List<Map<String, Object>> search(Long enterpriseId, String question, int limit)
    {
        List<Concept> concepts = buildConcepts(question);
        if (concepts.isEmpty())
        {
            return new ArrayList<>();
        }

        Product query = new Product();
        query.setEnterpriseId(enterpriseId);
        query.setStatus("0");
        List<Product> products = productMapper.selectProductList(query);

        List<Map<String, Object>> scored = new ArrayList<>();
        for (Product product : products)
        {
            MatchResult result = score(product, concepts);
            if (result.score <= 0)
            {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("productId", product.getProductId());
            row.put("productCode", product.getProductCode());
            row.put("productName", product.getProductName());
            row.put("spec", product.getSpec());
            row.put("unit", product.getUnit());
            row.put("categoryName", product.getCategoryName());
            row.put("stockQty", product.getStockQty());
            row.put("score", Math.round(result.score * 100) / 100.0);
            row.put("matchedTerms", result.matched);
            scored.add(row);
        }

        scored.sort(Comparator.comparingDouble((Map<String, Object> m) -> (Double) m.get("score")).reversed());
        return scored.size() > limit ? new ArrayList<>(scored.subList(0, limit)) : scored;
    }

    /**
     * 拆概念：原句词 + 近义词别名（长词再补二元片段）
     */
    private List<Concept> buildConcepts(String question)
    {
        if (question == null)
        {
            return new ArrayList<>();
        }
        String cleaned = question.trim();
        for (String stop : STOP_WORDS)
        {
            cleaned = cleaned.replace(stop, " ");
        }

        List<Concept> concepts = new ArrayList<>();
        for (String token : cleaned.split("[\\s,，。.;；:：!！?？]+"))
        {
            if (token.isEmpty())
            {
                continue;
            }
            Set<String> variants = new LinkedHashSet<>();
            variants.add(token);
            for (Map.Entry<String, List<String>> entry : SYNONYMS.entrySet())
            {
                if (token.contains(entry.getKey()))
                {
                    variants.addAll(entry.getValue());
                }
            }
            if (token.length() > GRAM_TRIGGER_LENGTH)
            {
                for (int i = 0; i + GRAM_SIZE <= token.length(); i++)
                {
                    variants.add(token.substring(i, i + GRAM_SIZE));
                }
            }
            concepts.add(new Concept(token, new ArrayList<>(variants)));
        }
        return concepts;
    }

    /**
     * 每个概念取最高命中权重，最后按概念数归一化
     */
    private MatchResult score(Product product, List<Concept> concepts)
    {
        String name = lower(product.getProductName());
        String spec = lower(product.getSpec());
        String code = lower(product.getProductCode());
        String other = lower(product.getCategoryName()) + lower(product.getRemark());

        double total = 0;
        List<String> matched = new ArrayList<>();
        for (Concept concept : concepts)
        {
            double best = 0;
            String bestTerm = null;
            for (String variant : concept.variants)
            {
                String term = lower(variant);
                double hit = 0;
                if (name.contains(term))
                {
                    hit = 3;
                }
                else if (spec.contains(term) || code.contains(term))
                {
                    hit = 2;
                }
                else if (other.contains(term))
                {
                    hit = 1;
                }
                if (hit > best)
                {
                    best = hit;
                    bestTerm = variant;
                }
            }
            if (best > 0)
            {
                total += best;
                matched.add(bestTerm);
            }
        }
        double score = total / (concepts.size() * 3.0);
        return new MatchResult(score, matched);
    }

    private String lower(String value)
    {
        return value == null ? "" : value.toLowerCase();
    }

    private static class Concept
    {
        private final String term;

        private final List<String> variants;

        Concept(String term, List<String> variants)
        {
            this.term = term;
            this.variants = variants;
        }
    }

    private static class MatchResult
    {
        private final double score;

        private final List<String> matched;

        MatchResult(double score, List<String> matched)
        {
            this.score = score;
            this.matched = matched;
        }
    }
}
