-- 默认账号数据
INSERT INTO db_system.t_account (id, username, password, user_id, type, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1927290201865945090, 'sysadmin', '$2a$10$ALzuYNgOSYLlJg/XsxUY7O4BKeqECHf5J7bY8eGPaQK.3VSlkFTaO', 0, 0, NULL, NULL, NULL, NULL, NULL);
-- 菜单初始化
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929928379575111682, 0, 'icon-setting', '系统管理', '/system', 'layout', 'layout', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929928379667386370, 0, 'icon-setting', '组件示例', '/example', 'layout', 'layout', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526785, 1929928379575111682, 'icon-setting-role', '角色管理', 'role', '/System/Role/index', NULL, 0, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526787, 1929928379575111682, 'icon-setting-role', '字典管理', 'dict', '/System/Dict/index', NULL, 0, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620816441347, 1929928379667386370, 'icon-setting-role', '列表示例', 'table', '/Example/Table/index', NULL, 0, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526789, 1929928379575111682, 'icon-setting-role', '菜单管理', 'menu', '/System/Menu/index', NULL, 0, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526786, 1929928379575111682, 'icon-setting-role', '权限管理', 'power', '/System/Power/index', NULL, 0, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526788, 1929928379575111682, 'icon-setting-role', '定时任务', 'task', '/System/Task/index', NULL, 0, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620816441348, 1929928379667386370, 'icon-setting-role', '表单示例', 'form', '/Example/Form/index', NULL, 0, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620816441346, 1929928379667386370, 'icon-setting-role', '图表示例', 'echarts', '/Example/Echarts/index', NULL, 0, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526790, 1929928379575111682, 'icon-setting-role', '文件存储', 'storage', '/System/Storage/index', NULL, 0, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620715778049, 1929928379575111682, 'icon-setting-role', '用户管理', 'user', '/System/User/index', NULL, 0, NULL, NULL, NULL, NULL,
        NULL);

-- 角色初始化
INSERT INTO db_system.t_role (id, name, state, scope, remark, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1932685785802162178, '系统管理员', TRUE, 0, '系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容', 1927290201865945090,
        '2025-06-11 14:26:13.572692', 1927290201865945090, '2025-06-11 14:26:13.572692', NULL);
INSERT INTO db_system.t_role (id, name, state, scope, remark, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1932682189593350146, '运维管理员', TRUE, 2, '运维人员使用,全局范围,拥有所有权限', 1927290201865945090, '2025-06-11 14:11:56.208812',
        1927290201865945090, '2025-06-11 14:33:59.593709', NULL);
INSERT INTO db_system.t_role (id, name, state, scope, remark, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1932687324356775938, '小组长', FALSE, 1, '测试禁用状态', 1927290201865945090, '2025-06-11 14:32:20.385948', 1927290201865945090,
        '2025-06-12 17:15:18.034439', NULL);
