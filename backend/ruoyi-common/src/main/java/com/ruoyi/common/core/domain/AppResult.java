package com.ruoyi.common.core.domain;

import java.util.HashMap;

/**
 * 小程序 / 前后端分离接口统一响应体
 *
 * 与 AjaxResult 的区别：成功码为 200，与前端 request.js 的约定一致
 * （code === 200 视为成功，401 触发重新登录，500 为业务异常）。
 *
 * @author cangxiaozhu
 */
public class AppResult extends HashMap<String, Object>
{
    private static final long serialVersionUID = 1L;

    /** 成功 */
    public static final int SUCCESS = 200;

    /** 未登录 / token 失效 */
    public static final int UNAUTHORIZED = 401;

    /** 无权限 */
    public static final int FORBIDDEN = 403;

    /** 参数或业务校验失败 */
    public static final int FAIL = 500;

    /** 状态码 */
    public static final String CODE_TAG = "code";

    /** 返回内容 */
    public static final String MSG_TAG = "msg";

    /** 数据对象 */
    public static final String DATA_TAG = "data";

    public AppResult()
    {
        super.put(CODE_TAG, SUCCESS);
        super.put(MSG_TAG, "操作成功");
    }

    public AppResult(int code, String msg)
    {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    public static AppResult ok()
    {
        return new AppResult(SUCCESS, "操作成功");
    }

    public static AppResult ok(String msg)
    {
        return new AppResult(SUCCESS, msg);
    }

    public static AppResult ok(Object data)
    {
        AppResult result = new AppResult(SUCCESS, "操作成功");
        result.put(DATA_TAG, data);
        return result;
    }

    public static AppResult error(int code, String msg)
    {
        return new AppResult(code, msg);
    }

    public static AppResult error(String msg)
    {
        return new AppResult(FAIL, msg);
    }

    @Override
    public AppResult put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }
}
