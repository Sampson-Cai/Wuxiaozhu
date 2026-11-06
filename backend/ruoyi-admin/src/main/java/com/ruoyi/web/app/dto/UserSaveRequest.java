package com.ruoyi.web.app.dto;

import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 账号新增/修改请求
 *
 * 不直接用 SysUser 接收：其 getPassword 标了 @JsonIgnore，Jackson 会连 setter 一起忽略，
 * 导致 JSON 里的 password 绑不进来。这里显式声明字段，避免改公共实体。
 *
 * @author cangxiaozhu
 */
public class UserSaveRequest
{
    private String loginName;

    private String userName;

    private String password;

    private String phonenumber;

    private String email;

    private String sex;

    private String remark;

    private Long deptId;

    private Long[] roleIds;

    /**
     * 转成实体，只带允许前端提交的字段
     */
    public SysUser toUser()
    {
        SysUser user = new SysUser();
        user.setLoginName(loginName);
        user.setUserName(userName);
        user.setPassword(password);
        user.setPhonenumber(phonenumber);
        user.setEmail(email);
        user.setSex(sex);
        user.setRemark(remark);
        user.setDeptId(deptId);
        user.setRoleIds(roleIds);
        return user;
    }

    public String getLoginName()
    {
        return loginName;
    }

    public void setLoginName(String loginName)
    {
        this.loginName = loginName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long[] getRoleIds()
    {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds)
    {
        this.roleIds = roleIds;
    }
}
