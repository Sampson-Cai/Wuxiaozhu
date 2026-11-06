package com.ruoyi.web.app.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.AppPermission;
import com.ruoyi.common.core.domain.AppResult;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysRoleService;

/**
 * 角色与权限点管理
 *
 * 说明：sys_role 为部署级共享表，不按单位隔离（与若依后台一致）；
 * 角色只承载小程序权限点（business:*），不参与后台菜单授权。
 *
 * @author cangxiaozhu
 */
@RestController
@RequestMapping("/business/role")
public class AppRoleController extends AppBaseController
{
    /** 权限点前缀，只允许把业务权限点分配给角色 */
    private static final String PERM_PREFIX = "business:";

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysMenuService menuService;

    @GetMapping("/list")
    public AppResult list()
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        List<SysRole> roles = roleService.selectRoleList(new SysRole());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SysRole role : roles)
        {
            if (role.isAdmin())
            {
                continue;
            }
            rows.add(toRow(role));
        }
        return AppResult.ok().put("rows", rows);
    }

    /**
     * 角色详情：资料 + 已授权权限点
     */
    @GetMapping("/{roleId}")
    public AppResult detail(@PathVariable Long roleId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        SysRole role = roleService.selectRoleById(roleId);
        if (role == null)
        {
            return AppResult.error("角色不存在");
        }
        if (role.isAdmin())
        {
            return AppResult.error("内置超级管理员角色不允许查看");
        }
        Map<String, Object> row = toRow(role);
        row.put("remark", role.getRemark());
        Set<String> perms = menuService.selectPermsByRoleId(roleId);
        // 父级目录本身没有 perms，只能由命中权限点的 parentId 反推，否则前端回显会丢目录节点
        Set<Long> menuIds = new LinkedHashSet<>();
        List<String> permList = new ArrayList<>();
        if (perms != null)
        {
            for (SysMenu menu : businessMenus())
            {
                if (menu.getPerms() != null && perms.contains(menu.getPerms()))
                {
                    if (menu.getParentId() != null && menu.getParentId() != 0L)
                    {
                        menuIds.add(menu.getParentId());
                    }
                    menuIds.add(menu.getMenuId());
                    permList.add(menu.getPerms());
                }
            }
        }
        row.put("menuIds", new ArrayList<>(menuIds));
        row.put("permissions", permList);
        return AppResult.ok().put("data", row);
    }

    /**
     * 可分配的权限点（小程序业务权限），供角色编辑页勾选
     */
    @GetMapping("/menus")
    public AppResult menus()
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SysMenu menu : businessMenus())
        {
            Map<String, Object> row = new HashMap<>(8);
            row.put("menuId", menu.getMenuId());
            row.put("menuName", menu.getMenuName());
            row.put("parentId", menu.getParentId());
            row.put("perms", menu.getPerms());
            rows.add(row);
        }
        return AppResult.ok().put("rows", rows);
    }

    @PostMapping
    @AppPermission("business:role:edit")
    public AppResult add(@RequestBody SysRole role)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        String error = validate(role, null);
        if (error != null)
        {
            return AppResult.error(error);
        }
        role.setRoleId(null);
        role.setDataScope(StringUtils.isEmpty(role.getDataScope()) ? "1" : role.getDataScope());
        role.setStatus(StringUtils.isEmpty(role.getStatus()) ? "0" : role.getStatus());
        role.setRoleSort(StringUtils.isEmpty(role.getRoleSort()) ? "0" : role.getRoleSort());
        role.setCreateBy(operator());
        role.setMenuIds(normalizeMenuIds(role.getMenuIds()));
        roleService.insertRole(role);
        return AppResult.ok("新增成功").put("roleId", role.getRoleId());
    }

    @PutMapping
    @AppPermission("business:role:edit")
    public AppResult edit(@RequestBody SysRole role)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        if (role.getRoleId() == null)
        {
            return AppResult.error("角色ID不能为空");
        }
        SysRole exists = roleService.selectRoleById(role.getRoleId());
        if (exists == null)
        {
            return AppResult.error("角色不存在");
        }
        if (exists.isAdmin())
        {
            return AppResult.error("内置超级管理员角色不允许修改");
        }
        String error = validate(role, role.getRoleId());
        if (error != null)
        {
            return AppResult.error(error);
        }
        role.setUpdateBy(operator());
        role.setMenuIds(normalizeMenuIds(role.getMenuIds()));
        roleService.updateRole(role);
        return AppResult.ok("修改成功");
    }

    /**
     * 删除角色：内置角色、已分配用户的角色不允许删除
     */
    @DeleteMapping("/{roleId}")
    @AppPermission("business:role:edit")
    public AppResult remove(@PathVariable Long roleId)
    {
        Long eid = enterpriseId();
        if (eid == null)
        {
            return unauthorized();
        }
        SysRole role = roleService.selectRoleById(roleId);
        if (role == null)
        {
            return AppResult.error("角色不存在");
        }
        if (role.isAdmin())
        {
            return AppResult.error("内置超级管理员角色不允许删除");
        }
        if (roleService.countUserRoleByRoleId(roleId) > 0)
        {
            return AppResult.error("该角色仍有账号在使用，请先调整账号角色");
        }
        roleService.deleteRoleById(roleId);
        return AppResult.ok("删除成功");
    }

    private String validate(SysRole role, Long roleId)
    {
        if (StringUtils.isEmpty(role.getRoleName()) || StringUtils.isEmpty(role.getRoleKey()))
        {
            return "角色名称和权限字符不能为空";
        }
        if (role.getRoleName().length() > 30)
        {
            return "角色名称过长";
        }
        if (role.getRoleKey().length() > 100)
        {
            return "权限字符过长";
        }
        role.setRoleId(roleId);
        if (!roleService.checkRoleNameUnique(role))
        {
            return "角色名称已存在";
        }
        if (!roleService.checkRoleKeyUnique(role))
        {
            return "权限字符已存在";
        }
        return null;
    }

    /**
     * insertRoleMenu 会直接遍历数组，这里兜底成空数组，避免只改名称时 NPE
     */
    private Long[] normalizeMenuIds(Long[] menuIds)
    {
        return menuIds == null ? new Long[0] : menuIds;
    }

    /**
     * 小程序业务权限点：目录 2000 及其下 F 类型菜单
     */
    private List<SysMenu> businessMenus()
    {
        List<SysMenu> all = menuService.selectMenuList(new SysMenu(), userId());
        List<SysMenu> result = new ArrayList<>();
        if (all == null)
        {
            return result;
        }
        for (SysMenu menu : all)
        {
            if (menu.getPerms() != null && menu.getPerms().startsWith(PERM_PREFIX))
            {
                result.add(menu);
            }
        }
        return result;
    }

    private Map<String, Object> toRow(SysRole role)
    {
        Map<String, Object> row = new HashMap<>(8);
        row.put("roleId", role.getRoleId());
        row.put("roleName", role.getRoleName());
        row.put("roleKey", role.getRoleKey());
        row.put("roleSort", role.getRoleSort());
        row.put("status", role.getStatus());
        row.put("dataScope", role.getDataScope());
        return row;
    }
}
