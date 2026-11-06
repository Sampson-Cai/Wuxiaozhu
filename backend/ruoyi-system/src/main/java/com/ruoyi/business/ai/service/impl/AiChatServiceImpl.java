package com.ruoyi.business.ai.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.ruoyi.business.ai.AiProperties;
import com.ruoyi.business.ai.AiStreamListener;
import com.ruoyi.business.ai.ChatClient;
import com.ruoyi.business.ai.domain.AiChatMessage;
import com.ruoyi.business.ai.domain.AiChatSession;
import com.ruoyi.business.ai.domain.KbHit;
import com.ruoyi.business.ai.mapper.AiChatMapper;
import com.ruoyi.business.ai.service.IAiChatService;
import com.ruoyi.business.ai.service.IKbService;
import com.ruoyi.business.domain.Product;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.mapper.DashboardMapper;
import com.ruoyi.business.mapper.ProductMapper;
import com.ruoyi.business.mapper.StockMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * AI 对话服务：检索知识库 + 注入业务快照 + 调用对话模型
 *
 * @author cangxiaozhu
 */
@Service
public class AiChatServiceImpl implements IAiChatService
{
    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    /** 物料编码，如 BOLT-HX-001 / NUT-HX-004 */
    private static final Pattern SKU_PATTERN = Pattern.compile("[A-Z]{2,8}(-[A-Z0-9]{1,8}){1,4}");

    @Autowired
    private AiProperties properties;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private IKbService kbService;

    @Autowired
    private AiChatMapper chatMapper;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public boolean enabled()
    {
        return properties.isEnabled();
    }

    @Override
    public Map<String, Object> ask(Long enterpriseId, Long userId, String operator, Long sessionId, String question)
    {
        if (!properties.isEnabled())
        {
            throw new ServiceException("AI 助手未启用");
        }
        String text = question == null ? "" : question.trim();
        if (text.isEmpty())
        {
            throw new ServiceException("问题不能为空");
        }
        if (text.length() > 500)
        {
            text = text.substring(0, 500);
        }

        AiChatSession session = sessionId == null ? null : chatMapper.selectSession(enterpriseId, sessionId);
        boolean fresh = session == null;
        if (fresh)
        {
            session = new AiChatSession();
            session.setEnterpriseId(enterpriseId);
            session.setUserId(userId);
            session.setTitle(text.length() > 40 ? text.substring(0, 40) : text);
            chatMapper.insertSession(session);
        }

        List<AiChatMessage> history = chatMapper.selectMessages(session.getSessionId(),
                Math.max(0, properties.getRag().getHistoryMessages()));

        List<KbHit> hits = kbService.search(enterpriseId, text, properties.getRag().getTopK(),
                properties.getRag().getMinScore());

        List<Map<String, String>> messages = ChatClient.newMessages();
        messages.add(ChatClient.message("system", systemPrompt(enterpriseId, hits, liveProducts(enterpriseId, hits))));
        for (AiChatMessage item : history)
        {
            if ("user".equals(item.getRole()) || "assistant".equals(item.getRole()))
            {
                messages.add(ChatClient.message(item.getRole(), item.getContent()));
            }
        }
        messages.add(ChatClient.message("user", text));

        long start = System.currentTimeMillis();
        ChatClient.ChatReply reply = chatClient.chat(messages);
        long cost = System.currentTimeMillis() - start;

        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setEnterpriseId(enterpriseId);
        userMessage.setSessionId(session.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(text);
        userMessage.setModel(reply.getModel());
        userMessage.setTokenUsage(0);
        userMessage.setCostMs(0);
        chatMapper.insertMessage(userMessage);

        List<Map<String, Object>> sources = sources(hits);
        AiChatMessage answerMessage = new AiChatMessage();
        answerMessage.setEnterpriseId(enterpriseId);
        answerMessage.setSessionId(session.getSessionId());
        answerMessage.setRole("assistant");
        answerMessage.setContent(reply.getContent());
        answerMessage.setSources(sources.isEmpty() ? null : JSON.toJSONString(sources));
        answerMessage.setModel(reply.getModel());
        answerMessage.setTokenUsage(reply.getTokens());
        answerMessage.setCostMs((int) cost);
        chatMapper.insertMessage(answerMessage);

        chatMapper.touchSession(session.getSessionId(), fresh ? session.getTitle() : null);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getSessionId());
        result.put("answer", reply.getContent());
        result.put("sources", sources);
        result.put("model", reply.getModel());
        result.put("tokens", reply.getTokens());
        result.put("costMs", cost);
        result.put("messageId", answerMessage.getMessageId());
        log.info("AI 回答完成：session={}，命中 {} 条资料，{}ms", session.getSessionId(), hits.size(), cost);
        return result;
    }

    @Override
    public void streamAsk(Long enterpriseId, Long userId, String operator, Long sessionId, String question,
            AiStreamListener listener)
    {
        if (!properties.isEnabled())
        {
            listener.onError("AI 助手未启用");
            return;
        }
        String text = question == null ? "" : question.trim();
        if (text.isEmpty())
        {
            listener.onError("问题不能为空");
            return;
        }
        if (text.length() > 500)
        {
            text = text.substring(0, 500);
        }

        AiChatSession session = sessionId == null ? null : chatMapper.selectSession(enterpriseId, sessionId);
        boolean fresh = session == null;
        if (fresh)
        {
            session = new AiChatSession();
            session.setEnterpriseId(enterpriseId);
            session.setUserId(userId);
            session.setTitle(text.length() > 40 ? text.substring(0, 40) : text);
            chatMapper.insertSession(session);
        }

        // 先取历史，再落本次提问，避免同一条问句进上下文两次
        List<AiChatMessage> history = chatMapper.selectMessages(session.getSessionId(),
                Math.max(0, properties.getRag().getHistoryMessages()));

        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setEnterpriseId(enterpriseId);
        userMessage.setSessionId(session.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(text);
        userMessage.setModel(properties.getChat().getModel());
        userMessage.setTokenUsage(0);
        userMessage.setCostMs(0);
        chatMapper.insertMessage(userMessage);

        // 先回会话，前端立刻能显示气泡，检索慢也不会白屏
        listener.onMeta(session.getSessionId());

        List<KbHit> hits = kbService.search(enterpriseId, text, properties.getRag().getTopK(),
                properties.getRag().getMinScore());
        listener.onSources(sources(hits));

        List<Map<String, String>> messages = ChatClient.newMessages();
        messages.add(ChatClient.message("system", systemPrompt(enterpriseId, hits, liveProducts(enterpriseId, hits))));
        for (AiChatMessage item : history)
        {
            if ("user".equals(item.getRole()) || "assistant".equals(item.getRole()))
            {
                messages.add(ChatClient.message(item.getRole(), item.getContent()));
            }
        }
        messages.add(ChatClient.message("user", text));

        try
        {
            ChatClient.ChatReply reply = chatClient.streamChat(messages, listener::onDelta);

            AiChatMessage answerMessage = new AiChatMessage();
            answerMessage.setEnterpriseId(enterpriseId);
            answerMessage.setSessionId(session.getSessionId());
            answerMessage.setRole("assistant");
            answerMessage.setContent(reply.getContent());
            List<Map<String, Object>> used = sources(hits);
            answerMessage.setSources(used.isEmpty() ? null : JSON.toJSONString(used));
            answerMessage.setModel(reply.getModel());
            answerMessage.setTokenUsage(reply.getTokens());
            answerMessage.setCostMs((int) reply.getCostMs());
            chatMapper.insertMessage(answerMessage);
            chatMapper.touchSession(session.getSessionId(), fresh ? session.getTitle() : null);

            listener.onDone(reply.getContent(), reply.getTokens(), reply.getCostMs());
        }
        catch (RuntimeException e)
        {
            log.warn("流式问答失败：{}", e.getMessage());
            listener.onError(e.getMessage());
        }
    }

    @Override
    public List<AiChatSession> sessions(Long enterpriseId, Long userId, int limit)
    {
        return chatMapper.selectSessions(enterpriseId, userId, limit <= 0 ? 20 : limit);
    }

    @Override
    public List<AiChatMessage> messages(Long sessionId, int limit)
    {
        return chatMapper.selectMessages(sessionId, limit <= 0 ? 50 : limit);
    }

    /**
     * 系统提示词：角色 + 业务快照 + 检索到的资料
     */
    private String systemPrompt(Long enterpriseId, List<KbHit> hits, String productBlock)
    {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是「物小助」智能库管助手，服务中小规模单位的补货、采购、库存与应付管理。\n");
        prompt.append("回答规则：\n");
        prompt.append("1. 规格、型号、材质、参考单价等静态信息取自【知识库资料】，引用时在句末标注（来源：文档标题）。\n");
        prompt.append("2. 当前库存数量、可用量、安全库存、在途量、金额、单据状态一律以【实时业务快照】和【相关商品实时数据】为准；知识库表格里出现的数量是导入时的历史值，不能当作当前库存。\n");
        prompt.append("3. 可用 = 现存 − 锁定；快照中的「可用」是系统已算好的准确值，禁止根据安全库存或知识库数字自行推算、补零或改写。\n");
        prompt.append("4. 快照没有给出的数据不要编造，直接说明系统里查不到。\n");
        prompt.append("5. 用简体中文，先给结论再列要点，最多 6 行；不要使用 Markdown 表格、标题符号和代码块。\n");
        prompt.append("6. 给操作建议时写成可执行步骤，不要声称系统会自动执行。\n\n");
        prompt.append("【实时业务快照】\n").append(snapshot(enterpriseId)).append('\n');
        if (productBlock != null && !productBlock.isEmpty())
        {
            prompt.append('\n').append(productBlock);
        }

        if (hits != null && !hits.isEmpty())
        {
            prompt.append("\n【知识库资料】\n");
            int used = 0;
            int max = Math.max(500, properties.getRag().getMaxContextChars());
            for (KbHit hit : hits)
            {
                String block = "〔" + safe(hit.getDocTitle()) + "〕\n" + hit.getContent() + "\n\n";
                if (used + block.length() > max)
                {
                    break;
                }
                prompt.append(block);
                used += block.length();
            }
        }
        return prompt.toString();
    }

    /**
     * 从召回片段里识别物料编码，回查实时单价与现存，避免模型拿过期数字或直接说查不到
     */
    private String liveProducts(Long enterpriseId, List<KbHit> hits)
    {
        if (hits == null || hits.isEmpty())
        {
            return "";
        }
        Set<String> codes = new LinkedHashSet<>();
        for (KbHit hit : hits)
        {
            if (hit.getContent() == null)
            {
                continue;
            }
            Matcher matcher = SKU_PATTERN.matcher(hit.getContent());
            while (matcher.find() && codes.size() < 30)
            {
                String code = matcher.group();
                codes.add(code);
                codes.add("M-" + code);
            }
        }
        if (codes.isEmpty())
        {
            return "";
        }
        List<Product> products;
        try
        {
            products = productMapper.selectByCodes(enterpriseId, new ArrayList<>(codes));
        }
        catch (RuntimeException e)
        {
            log.warn("回查物料实时数据失败：{}", e.getMessage());
            return "";
        }
        if (products == null || products.isEmpty())
        {
            return "";
        }
        StringBuilder text = new StringBuilder("【相关商品实时数据】\n");
        // 只带最相关的 5 条，提示词越长首字越慢
        int limit = Math.min(5, products.size());
        for (int i = 0; i < limit; i++)
        {
            Product product = products.get(i);
            text.append("- ").append(product.getProductCode()).append("｜").append(product.getProductName());
            if (product.getSpec() != null && !product.getSpec().isEmpty())
            {
                text.append("｜规格 ").append(product.getSpec());
            }
            text.append("｜单价 ¥").append(product.getRefPrice() == null ? "-" : product.getRefPrice().toPlainString());
            BigDecimal available = product.getAvailableQty() == null ? product.getStockQty() : product.getAvailableQty();
            text.append("｜可用 ").append(available == null ? "0" : available.stripTrailingZeros().toPlainString());
            text.append(product.getUnit() == null ? "" : product.getUnit());
            text.append('\n');
        }
        return text.toString();
    }

    /**
     * 实时业务快照，控制在一屏以内
     */
    private String snapshot(Long enterpriseId)
    {
        StringBuilder text = new StringBuilder();
        int lowStock = dashboardMapper.countLowStock(enterpriseId);
        int pending = dashboardMapper.countPendingApproval(enterpriseId);
        int due = dashboardMapper.countPayableDue(enterpriseId, 7);
        int overdue = dashboardMapper.countPayableOverdue(enterpriseId);
        BigDecimal unpaid = dashboardMapper.sumUnpaidAmount(enterpriseId);
        text.append("库存预警 ").append(lowStock).append(" 个；待审批采购单 ").append(pending).append(" 单；");
        text.append("7 天内到期应付 ").append(due).append(" 笔；逾期应付 ").append(overdue).append(" 笔；");
        text.append("未结清应付合计 ¥").append(unpaid == null ? "0.00" : unpaid.toPlainString()).append("。\n");

        try
        {
            List<Stock> stocks = stockMapper.selectStockList(enterpriseId, null, null, Boolean.TRUE);
            if (stocks != null && !stocks.isEmpty())
            {
                text.append("低库存明细（共 ").append(stocks.size()).append(" 个，可用 = 现存 − 锁定）：");
                int limit = Math.min(10, stocks.size());
                for (int i = 0; i < limit; i++)
                {
                    Stock stock = stocks.get(i);
                    if (i > 0)
                    {
                        text.append("、");
                    }
                    BigDecimal qty = stock.getQty() == null ? BigDecimal.ZERO : stock.getQty();
                    BigDecimal locked = stock.getLockedQty() == null ? BigDecimal.ZERO : stock.getLockedQty();
                    text.append(stock.getProductName()).append("(可用 ").append(plain(qty.subtract(locked)))
                            .append("/安全 ").append(plain(stock.getSafetyStock())).append(")");
                }
                if (stocks.size() > limit)
                {
                    text.append("……");
                }
                text.append("。");
            }
        }
        catch (RuntimeException e)
        {
            log.warn("构建低库存快照失败：{}", e.getMessage());
        }
        return text.toString();
    }

    private List<Map<String, Object>> sources(List<KbHit> hits)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        if (hits == null)
        {
            return list;
        }
        // 同一文档命中多段时只保留一条，取最高分
        Map<Long, Map<String, Object>> merged = new LinkedHashMap<>();
        for (KbHit hit : hits)
        {
            Long documentId = hit.getDocumentId();
            if (merged.containsKey(documentId))
            {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("documentId", documentId);
            item.put("title", hit.getDocTitle());
            item.put("score", Math.round(hit.getScore() * 1000) / 1000D);
            String content = hit.getContent() == null ? "" : hit.getContent().replaceAll("\\s+", " ").trim();
            item.put("snippet", content.length() > 120 ? content.substring(0, 120) + "…" : content);
            merged.put(documentId, item);
        }
        list.addAll(merged.values());
        return list;
    }

    private String plain(BigDecimal value)
    {
        if (value == null)
        {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private String safe(String value)
    {
        return value == null ? "未命名文档" : value;
    }
}
