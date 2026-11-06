package com.ruoyi.web.app.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.shiro.service.SysPasswordService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.web.app.dto.UserSaveRequest;

/**
 * 本单位账号管理（查询、新增、改资料、重置密码、停用、分配角色）
 *
 * 只返回必要字段，不返回口令与盐值；单位标识由服务端注入，不接受前端传入。
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business")
public class AppUserController extends AppBaseController
{
    /** 口令最小长度 */
    private static final int PASSWORD_MIN_LENGTH = 6;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private SysPasswordService passwordService;

    @GetMapping("/user/list")
    public AppResult userList(@RequestParam(required = false) String keyword)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        startPage();
        // 单位条件与关键字都下推到 SQL，避免「先分页后过滤」导致 total 跨单位、页内条数变少
        SysUser query = new SysUser();
        query.setEnterpriseId(eid);
        if (StringUtils.isNotEmpty(keyword))
        {
            query.getParams().put("keyword", keyword);
        }
        List<SysUser> users = userService.selectUserList(query);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SysUser user : users)
        {
            rows.add(toRow(user));
        }
        return pageResult(rows);
    }

    /**
     * 账号详情：资料 + 已分配角色
     */
    @GetMapping("/user/{userId}")
    public AppResult userDetail(@PathVariable Long userId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        SysUser target = userService.selectUserById(userId);
        String error = checkTarget(eid, target);
        if (error != null)
        {
            return AppResult.error(error);
        }
        Map<String, Object> row = toRow(target);
        row.put("email", target.getEmail());
        row.put("sex", target.getSex());
        row.put("remark", target.getRemark());
        row.put("deptId", target.getDeptId());
        List<Long> roleIds = new ArrayList<>();
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        if (roles != null)
        {
            for (SysRole role : roles)
            {
                if (role.isFlag())
                {
                    roleIds.add(role.getRoleId());
                }
            }
        }
        row.put("roleIds", roleIds);
        return AppResult.ok().put("data", row);
    }

    /**
     * 新增账号：小程序端建号，口令加密方式与后台一致，否则新账号登录不了
     */
    @PostMapping("/user")
    @AppPermission("business:user:edit")
    public AppResult add(@RequestBody UserSaveRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        SysUser user = request.toUser();
        if (StringUtils.isEmpty(user.getLoginName()) || StringUtils.isEmpty(user.getUserName()))
        {
            return AppResult.error("登录账号和姓名不能为空");
        }
        if (user.getLoginName().length() < 2 || user.getLoginName().length() > 20)
        {
            return AppResult.error("登录账号长度需在 2 到 20 个字符之间");
        }
        if (user.getUserName().length() > 30)
        {
            return AppResult.error("姓名过长");
        }
        if (StringUtils.isEmpty(user.getPassword()) || user.getPassword().length() < PASSWORD_MIN_LENGTH)
        {
            return AppResult.error("初始密码不能少于 " + PASSWORD_MIN_LENGTH + " 位");
        }
        if (!userService.checkLoginNameUnique(user))
        {
            return AppResult.error("登录账号已存在");
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return AppResult.error("手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return AppResult.error("邮箱账号已存在");
        }

        user.setUserId(null);
        user.setEnterpriseId(eid);
        user.setSalt(ShiroUtils.randomSalt());
        user.setPassword(passwordService.encryptPassword(user.getLoginName(), user.getPassword(), user.getSalt()));
        user.setPwdUpdateDate(DateUtils.getNowDate());
        user.setStatus("0");
        user.setCreateBy(operator());
        userService.insertUser(user);
        return AppResult.ok("新增成功").put("userId", user.getUserId());
    }

    /**
     * 修改账号资料（姓名、手机、邮箱、性别、备注），不含角色与口令
     */
    @PutMapping("/user/{userId}")
    @AppPermission("business:user:edit")
    public AppResult edit(@PathVariable Long userId, @RequestBody UserSaveRequest request)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        SysUser user = request.toUser();
        SysUser target = userService.selectUserById(userId);
        String error = checkTarget(eid, target);
        if (error != null)
        {
            return AppResult.error(error);
        }
        if (userId == 1L && !userId.equals(userId()))
        {
            return AppResult.error("不能修改超级管理员资料");
        }
        user.setUserId(userId);
        // 登录账号、单位、口令一律不允许通过该接口修改
        user.setLoginName(null);
        user.setPassword(null);
        user.setSalt(null);
        user.setEnterpriseId(null);
        user.setUpdateBy(operator());
        if (user.getDeptId() == null)
        {
            // updateUser 的 deptId 判断为 != 0，传 null 会被写成空，这里回填原值
            user.setDeptId(target.getDeptId());
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return AppResult.error("手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return AppResult.error("邮箱账号已存在");
        }
        userService.updateUserInfo(user);
        return AppResult.ok("修改成功");
    }

    /**
     * 修改本人密码：校验原密码，任何登录用户都可调用
     */
    @PutMapping("/user/password")
    public AppResult changeOwnPassword(@RequestBody Map<String, String> body)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        SysUser current = userService.selectUserById(userId());
        if (current == null)
        {
            return AppResult.error("账号不存在");
        }
        String oldPassword = body == null ? null : body.get("oldPassword");
        String newPassword = body == null ? null : body.get("newPassword");
        if (StringUtils.isEmpty(oldPassword))
        {
            return AppResult.error("请输入原密码");
        }
        if (StringUtils.isEmpty(newPassword) || newPassword.length() < PASSWORD_MIN_LENGTH)
        {
            return AppResult.error("新密码不能少于 " + PASSWORD_MIN_LENGTH + " 位");
        }
        if (!passwordService.matches(current, oldPassword))
        {
            return AppResult.error("原密码不正确");
        }
        if (oldPassword.equals(newPassword))
        {
            return AppResult.error("新密码不能与原密码相同");
        }
        SysUser update = new SysUser();
        update.setUserId(current.getUserId());
        update.setSalt(ShiroUtils.randomSalt());
        update.setPassword(passwordService.encryptPassword(current.getLoginName(), newPassword, update.getSalt()));
        userService.resetUserPwd(update);
        return AppResult.ok("密码已修改");
    }

    /**
     * 重置密码
     */
    @PutMapping("/user/{userId}/password")
    @AppPermission("business:user:edit")
    public AppResult resetPassword(@PathVariable Long userId, @RequestBody Map<String, String> body)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        SysUser target = userService.selectUserById(userId);
        String error = checkTarget(eid, target);
        if (error != null)
        {
            return AppResult.error(error);
        }
        if (userId == 1L && !userId.equals(userId()))
        {
            return AppResult.error("不能重置超级管理员密码");
        }
        String password = body == null ? null : body.get("password");
        if (StringUtils.isEmpty(password) || password.length() < PASSWORD_MIN_LENGTH)
        {
            return AppResult.error("新密码不能少于 " + PASSWORD_MIN_LENGTH + " 位");
        }
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setSalt(ShiroUtils.randomSalt());
        update.setPassword(passwordService.encryptPassword(target.getLoginName(), password, update.getSalt()));
        userService.resetUserPwd(update);
        return AppResult.ok("密码已重置");
    }

    /**
     * 启用/停用账号
     */
    @PutMapping("/user/{userId}/status")
    @AppPermission("business:user:edit")
    public AppResult changeStatus(@PathVariable Long userId,
            @RequestParam String status)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (!"0".equals(status) && !"1".equals(status))
        {
            return AppResult.error("状态值不合法");
        }
        if (userId == 1L)
        {
            return AppResult.error("不能停用超级管理员");
        }
        SysUser target = userService.selectUserById(userId);
        String error = checkTarget(eid, target);
        if (error != null)
        {
            return AppResult.error(error);
        }
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setStatus(status);
        update.setUpdateBy(operator());
        userService.changeStatus(update);
        return AppResult.ok("状态已更新");
    }

    /**
     * 分配角色
     */
    @PutMapping("/user/{userId}/roles")
    @AppPermission("business:user:edit")
    public AppResult assignRoles(@PathVariable Long userId, @RequestBody Map<String, Object> body)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (userId == 1L)
        {
            return AppResult.error("不能修改超级管理员角色");
        }
        SysUser target = userService.selectUserById(userId);
        String error = checkTarget(eid, target);
        if (error != null)
        {
            return AppResult.error(error);
        }
        Object raw = body == null ? null : body.get("roleIds");
        if (!(raw instanceof List))
        {
            return AppResult.error("roleIds 必须是数组");
        }
        List<?> list = (List<?>) raw;
        Long[] roleIds = new Long[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            roleIds[i] = Long.valueOf(String.valueOf(list.get(i)));
        }
        userService.insertUserAuth(userId, roleIds);
        return AppResult.ok("角色已更新");
    }

    /**
     * 校验目标账号存在且属于当前单位
     */
    private String checkTarget(Long enterpriseId, SysUser target)
    {
        if (target == null)
        {
            return "用户不存在";
        }
        if (target.getEnterpriseId() != null && !enterpriseId.equals(target.getEnterpriseId()))
        {
            return "用户不存在或不属于当前单位";
        }
        return null;
    }

    private Map<String, Object> toRow(SysUser user)
    {
        Map<String, Object> row = new HashMap<>(8);
        row.put("userId", user.getUserId());
        row.put("loginName", user.getLoginName());
        row.put("userName", user.getUserName());
        row.put("deptName", user.getDept() == null ? "" : user.getDept().getDeptName());
        row.put("phonenumber", user.getPhonenumber());
        row.put("status", user.getStatus());
        return row;
    }
}
