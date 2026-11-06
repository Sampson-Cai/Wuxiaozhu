package com.ruoyi.framework.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.CacheUtils;
import com.ruoyi.system.service.ISysMenuService;

/**
 * 小程序权限查询
 *
 * 复用若依菜单权限（sys_menu.perms），按用户缓存 5 分钟；
 * 管理员固定返回 *:*:*，避免每次请求查库。
 *
 * @author cangxiaozhu
 */
@Service
public class AppPermissionService
{
    private static final String CACHE_NAME = "sys-cache";

    private static final String CACHE_PREFIX = "app:perm:";

    @Autowired
    private ISysMenuService menuService;

    @SuppressWarnings("unchecked")
    public Set<String> getPermissions(Long userId)
    {
        if (userId == null)
        {
            return Collections.emptySet();
        }
        String key = CACHE_PREFIX + userId;
        Object cached = CacheUtils.get(CACHE_NAME, key);
        if (cached instanceof Set)
        {
            return (Set<String>) cached;
        }
        Set<String> permissions = new HashSet<>();
        if (Long.valueOf(1L).equals(userId))
        {
            permissions.add("*:*:*");
        }
        else
        {
            Set<String> perms = menuService.selectPermsByUserId(userId);
            if (perms != null)
            {
                permissions.addAll(perms);
            }
        }
        CacheUtils.put(CACHE_NAME, key, permissions);
        return permissions;
    }

    public boolean hasPermission(Long userId, String permission)
    {
        Set<String> permissions = getPermissions(userId);
        return permissions.contains("*:*:*") || permissions.contains(permission);
    }

    /**
     * 权限变更后调用，下次请求重新加载
     */
    public void clearCache(Long userId)
    {
        CacheUtils.remove(CACHE_NAME, CACHE_PREFIX + userId);
    }
}
