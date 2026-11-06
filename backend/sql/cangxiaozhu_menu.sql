-- ----------------------------------------------------------------------------
-- 物小助：菜单/权限与默认角色
--
-- 说明：小程序不使用若依后台菜单，这些记录只作为权限标识载体，
--       角色授权后即可通过 @AppPermission 校验。
-- 可重复执行。
-- ----------------------------------------------------------------------------

-- 清理旧的物小助权限数据
delete from sys_role_menu where role_id in (100, 101, 102);
delete from sys_role where role_id in (100, 101, 102);
delete from sys_menu where menu_id between 2000 and 2020;

-- 目录
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
values (2000, '物小助', 0, 0, 'business', '', 'M', '0', '1', null, 'shopping', 'admin', sysdate(), '移动端业务权限');

-- 功能权限点
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark) values
(2001, '商品维护', 2000, 1, '', '', 'F', '0', '1', 'business:product:edit',   '#', 'admin', sysdate(), ''),
(2002, '供应商维护', 2000, 2, '', '', 'F', '0', '1', 'business:supplier:edit',  '#', 'admin', sysdate(), ''),
(2003, '分类维护', 2000, 3, '', '', 'F', '0', '1', 'business:category:edit',  '#', 'admin', sysdate(), ''),
(2004, '出入库操作', 2000, 4, '', '', 'F', '0', '1', 'business:stock:edit',     '#', 'admin', sysdate(), ''),
(2005, '采购单维护', 2000, 5, '', '', 'F', '0', '1', 'business:purchase:edit',  '#', 'admin', sysdate(), ''),
(2006, '采购审批', 2000, 6, '', '', 'F', '0', '1', 'business:purchase:approve', '#', 'admin', sysdate(), ''),
(2007, '应付与付款', 2000, 7, '', '', 'F', '0', '1', 'business:payable:edit',   '#', 'admin', sysdate(), ''),
(2008, '补货参数', 2000, 8, '', '', 'F', '0', '1', 'business:replenish:edit', '#', 'admin', sysdate(), ''),
(2009, '单位信息', 2000, 9, '', '', 'F', '0', '1', 'business:enterprise:edit','#', 'admin', sysdate(), ''),
(2010, '数据导出', 2000, 10, '', '', 'F', '0', '1', 'business:export',        '#', 'admin', sysdate(), ''),
(2011, '账号与角色', 2000, 11, '', '', 'F', '0', '1', 'business:user:edit',     '#', 'admin', sysdate(), '新增账号、改资料、重置密码、停用、分配角色'),
(2012, '角色权限', 2000, 12, '', '', 'F', '0', '1', 'business:role:edit',     '#', 'admin', sysdate(), '角色增删改与权限点分配'),
(2013, '操作日志', 2000, 13, '', '', 'F', '0', '1', 'business:log:view',      '#', 'admin', sysdate(), '按人/模块/时间/结果查询操作日志'),
(2014, '知识库维护', 2000, 14, '', '', 'F', '0', '1', 'business:kb:edit',       '#', 'admin', sysdate(), '导入/重建/删除知识库文档，供 AI 检索');

-- 默认角色：库管 / 采购 / 审批
insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark) values
(100, '库管员', 'keeper',   3, '1', '0', '0', 'admin', sysdate(), '出入库、收货、库存查看'),
(101, '采购员', 'buyer',    2, '1', '0', '0', 'admin', sysdate(), '采购单、供应商、应付'),
(102, '审批员', 'approver', 4, '1', '0', '0', 'admin', sysdate(), '采购单审批');

-- 角色权限
insert into sys_role_menu (role_id, menu_id) values
(100, 2000), (100, 2004), (100, 2008),
(101, 2000), (101, 2001), (101, 2002), (101, 2005), (101, 2007), (101, 2010),
(102, 2000), (102, 2006);
