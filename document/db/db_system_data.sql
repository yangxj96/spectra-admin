-- 用户
INSERT INTO db_auth.t_user (id, name, email, avatar, state, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1934276682383138817, '平台管理员', 'yangxj96@gmail.com', NULL, 0, NULL, '2025-06-15 23:47:52.864290', NULL, '2025-06-24 11:37:14.216352',
        NULL);
INSERT INTO db_auth.t_user (id, name, email, avatar, state, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1937354420709711873, '超级管理员', 'sysadmin@1.com', NULL, 0, NULL, '2025-06-24 11:37:42.866695', NULL, '2025-06-24 11:41:34.424012', NULL);
INSERT INTO db_auth.t_user (id, name, email, avatar, state, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1937706586813169665, '测试用户', 'ceshi@1.com', NULL, 0, NULL, '2025-06-25 10:57:05.810342', NULL, '2025-06-25 10:57:05.811358', NULL);

-- 账号
INSERT INTO db_auth.t_account (id, username, password, user_id, type, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1927290201865945090, 'yangxj96@gmail.com', '$2a$10$ALzuYNgOSYLlJg/XsxUY7O4BKeqECHf5J7bY8eGPaQK.3VSlkFTaO', 1934276682383138817, 0, NULL,
        '2025-06-24 11:37:42.957695', NULL, '2025-06-24 11:37:14.243354', NULL);
INSERT INTO db_auth.t_account (id, username, password, user_id, type, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1937354421099782146, 'sysadmin@1.com', '$2a$10$zQSrfeQHvHw022UFUOoJwe5oHdOAWcaZr8d2owbbCwAgWqOSjVFVa', 1937354420709711873, 0, NULL,
        '2025-06-24 11:37:42.957695', NULL, '2025-06-24 11:37:42.957695', NULL);
INSERT INTO db_auth.t_account (id, username, password, user_id, type, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1937706587203239938, 'ceshi@1.com', '$2a$10$UhexxdMYdvPFokOuBO2va.hG4mxzjvXRGufSmnLtRYJrirY8Bw4km', 1937706586813169665, 0, NULL,
        '2025-06-25 10:57:05.907345', NULL, '2025-06-25 10:57:05.911344', NULL);

-- 用户到角色
INSERT INTO db_auth.t_user_role_map (id, user_id, role_id, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1934292480493473793, 1934276682383138817, 1932682189593350146, NULL, '2025-06-16 00:50:39.420317', NULL, '2025-06-16 00:50:39.420317', NULL);
INSERT INTO db_auth.t_user_role_map (id, user_id, role_id, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1937354421099782147, 1937354420709711873, 1932685785802162178, NULL, '2025-06-24 11:37:42.867752', NULL, '2025-06-24 11:37:42.867752', NULL);
INSERT INTO db_auth.t_user_role_map (id, user_id, role_id, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1937706587291320321, 1937706586813169665, 1932687324356775938, NULL, '2025-06-25 10:57:05.812216', NULL, '2025-06-25 10:57:05.812216', NULL);

-- 角色
INSERT INTO db_auth.t_role (id, name, code, state, scope, remark, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1932682189593350146, '运维管理员', 'DEV_ADMIN', TRUE, 2, '运维人员使用,全局范围,拥有所有权限', NULL, '2025-06-11 14:11:56.208812', NULL,
        '2025-06-11 14:33:59.593709', NULL);
INSERT INTO db_auth.t_role (id, name, code, state, scope, remark, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1932687324356775938, '小组长', 'GROUP_LEADER', FALSE, 1, '测试禁用状态', NULL, '2025-06-11 14:32:20.385948', NULL,
        '2025-06-12 17:15:18.034439', NULL);
INSERT INTO db_auth.t_role (id, name, code, state, scope, remark, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1932685785802162178, '系统管理员', 'SYS_ADMIN', TRUE, 0, '系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容', NULL,
        '2025-06-11 14:26:13.572692', NULL, '2025-06-11 14:26:13.572692', NULL);
-- 菜单
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620816441347, 1929928379667386370, 'icon-module', '列表示例', 'table', '/Example/Table/index', NULL, 1, 1927290201865945090,
        '2025-06-05 11:37:45.581760', 1927290201865945090, '2025-06-11 09:47:10.002749', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620816441348, 1929928379667386370, 'icon-module', '表单示例', 'form', '/Example/Form/index', NULL, 2, 1927290201865945090,
        '2025-06-05 11:37:45.581760', 1927290201865945090, '2025-06-11 09:47:13.831965', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620816441346, 1929928379667386370, 'icon-module', '图表示例', 'echarts', '/Example/Echarts/index', NULL, 3, 1927290201865945090,
        '2025-06-05 11:37:45.581760', 1927290201865945090, '2025-06-11 09:47:18.286463', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526785, 1929928379575111682, 'icon-module', '访问控制', 'RBAC', '/System/RBAC/index', NULL, 2, 1927290201865945090,
        '2025-06-05 11:37:45.581760', 1927290201865945090, '2025-06-12 10:03:38.885015', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526789, 1929928379575111682, 'icon-module', '菜单管理', 'menu', '/System/Menu/index', NULL, 3, 1927290201865945090,
        '2025-06-05 11:37:45.581760', 1927290201865945090, '2025-06-12 10:03:42.708315', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526787, 1929928379575111682, 'icon-module', '字典管理', 'dict', '/System/Dict/index', NULL, 4, 1927290201865945090,
        '2025-06-05 11:37:45.581760', 1927290201865945090, '2025-06-12 10:03:45.588571', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526790, 1929928379575111682, 'icon-module', '文件存储', 'storage', '/System/Storage/index', NULL, 5, 1927290201865945090,
        '2025-06-05 11:37:45.581760', 1927290201865945090, '2025-06-12 10:03:48.846378', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620753526788, 1929928379575111682, 'icon-module', '定时任务', 'task', '/System/Task/index', NULL, 6, 1927290201865945090,
        '2025-06-05 11:37:45.581760', 1927290201865945090, '2025-06-12 10:03:52.308930', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1932983846772363266, 1929928379575111682, 'icon-module', '部门管理', 'dept', '/System/Dept/index', NULL, 1, 1927290201865945090,
        '2025-06-12 10:10:36.840451', 1927290201865945090, '2025-06-12 10:10:36.840451', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929929620715778049, 1929928379575111682, 'icon-module', '用户管理', 'user', '/System/User/index', NULL, 0, 1927290201865945090,
        '2025-06-05 11:37:45.581760', 1927290201865945090, '2025-06-13 15:32:51.681846', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929928379667386370, NULL, 'icon-setting', '组件示例', '/example', 'layout', 'layout', 1, 1927290201865945090, '2025-06-05 11:37:45.581760',
        1927290201865945090, '2025-06-05 11:37:45.582763', NULL);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted)
VALUES (1929928379575111682, NULL, 'icon-setting', '系统管理', '/system', 'layout', 'layout', 0, 1927290201865945090, '2025-06-05 11:37:45.581760',
        1927290201865945090, '2025-06-05 11:37:45.582763', NULL);
