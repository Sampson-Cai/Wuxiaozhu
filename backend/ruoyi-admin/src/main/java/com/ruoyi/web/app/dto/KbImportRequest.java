package com.ruoyi.web.app.dto;

/**
 * 知识库导入请求（服务器本地文件）
 *
 * @author cangxiaozhu
 */
public class KbImportRequest
{
    /** 服务器上的文件绝对路径，支持 .md / .txt / .csv */
    private String path;

    /** 文档分类，如 制度 / 物料手册 */
    private String docType;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getDocType()
    {
        return docType;
    }

    public void setDocType(String docType)
    {
        this.docType = docType;
    }
}
