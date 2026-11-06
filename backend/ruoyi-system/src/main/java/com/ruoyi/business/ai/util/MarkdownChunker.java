package com.ruoyi.business.ai.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Markdown 切片器
 *
 * 目标：按标题切段、按长度再切块，尽量不破坏表格。
 * 表格被切断时，后续块会重复表头，保证每块都能独立看懂。
 *
 * @author cangxiaozhu
 */
public final class MarkdownChunker
{
    private MarkdownChunker()
    {
    }

    /**
     * 切片
     *
     * @param markdown 原文
     * @param maxChars 单块字符上限
     * @param overlap 相邻块重叠字符（按行回退，近似值）
     */
    public static List<String> split(String markdown, int maxChars, int overlap)
    {
        List<String> chunks = new ArrayList<>();
        if (markdown == null || markdown.trim().isEmpty())
        {
            return chunks;
        }
        int limit = maxChars <= 0 ? 700 : maxChars;
        int back = Math.max(0, Math.min(overlap, limit / 3));

        String text = markdown.replace("\r\n", "\n").replace("\r", "\n");
        List<String> sections = splitSections(text);
        for (String section : sections)
        {
            String body = section.trim();
            if (body.isEmpty())
            {
                continue;
            }
            if (body.length() <= limit)
            {
                chunks.add(body);
                continue;
            }
            chunks.addAll(splitLongSection(body, limit, back));
        }
        return chunks;
    }

    /**
     * 按标题行切成若干段（标题行归属到下一段）
     */
    private static List<String> splitSections(String text)
    {
        List<String> sections = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String[] lines = text.split("\n", -1);
        for (String line : lines)
        {
            boolean heading = line.startsWith("#");
            if (heading && current.length() > 0)
            {
                sections.add(current.toString());
                current.setLength(0);
            }
            current.append(line).append('\n');
        }
        if (current.length() > 0)
        {
            sections.add(current.toString());
        }
        return sections;
    }

    /**
     * 超长段落按行累积切块，表格块补表头
     */
    private static List<String> splitLongSection(String section, int limit, int overlap)
    {
        List<String> chunks = new ArrayList<>();
        String[] lines = section.split("\n", -1);
        StringBuilder buffer = new StringBuilder();
        String tableHeader = null;

        for (String line : lines)
        {
            // 记录表头（表头 + 分隔行），后续块复用
            if (line.startsWith("|") && tableHeader == null)
            {
                tableHeader = line;
            }
            else if (line.startsWith("|") && tableHeader != null && tableHeader.indexOf('\n') < 0
                    && line.matches("^\\|[\\s:\\-|]+\\|$"))
            {
                tableHeader = tableHeader + "\n" + line;
            }

            if (buffer.length() + line.length() + 1 > limit && buffer.length() > 0)
            {
                chunks.add(buffer.toString().trim());
                String carry = carryOver(buffer.toString(), overlap);
                buffer.setLength(0);
                // 续块落在表格中间时补表头
                if (line.startsWith("|") && tableHeader != null && !carry.startsWith("|"))
                {
                    buffer.append(tableHeader).append('\n');
                }
                if (!carry.isEmpty())
                {
                    buffer.append(carry).append('\n');
                }
            }

            if (line.length() > limit)
            {
                // 单行超长（如整行表格），硬切
                int start = 0;
                while (start < line.length())
                {
                    int end = Math.min(line.length(), start + limit);
                    chunks.add(line.substring(start, end));
                    start = end;
                }
                continue;
            }
            buffer.append(line).append('\n');
        }
        if (buffer.length() > 0)
        {
            String last = buffer.toString().trim();
            if (!last.isEmpty())
            {
                chunks.add(last);
            }
        }
        return chunks;
    }

    /**
     * 取末尾若干字符作为重叠上下文，尽量从整行开始
     */
    private static String carryOver(String text, int overlap)
    {
        if (overlap <= 0 || text.isEmpty())
        {
            return "";
        }
        int start = Math.max(0, text.length() - overlap);
        int newline = text.indexOf('\n', start);
        if (newline >= 0 && newline + 1 < text.length())
        {
            start = newline + 1;
        }
        return text.substring(start).trim();
    }
}
