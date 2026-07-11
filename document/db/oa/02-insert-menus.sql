-- ============================================
-- OA 模块 — 菜单数据初始化
-- 生成时间: 2026-07-11
-- 说明: 先执行 01-create-tables.sql 建表，再执行本文件插入菜单
-- 菜单层级: OA办公(一级) → 9个子模块(二级)
-- ============================================

-- 使用 CTE 生成父菜单 UUID，确保父子关系正确
WITH oa_menu AS (
    INSERT INTO spectra_core.sys_menu (
        id, pid, icon, name, path, component, layout, sort, hide, metadata,
        created_by, created_at, updated_by, updated_at, deleted, version
    ) VALUES (
        gen_random_uuid(),
        NULL,
        'icon-setting',
        'OA办公',
        '/oa',
        '',
        'default',
        4,
        false,
        '{}'::jsonb,
        NULL, NOW(), NULL, NOW(), NULL, 0
    )
    RETURNING id
)
INSERT INTO spectra_core.sys_menu (
    id, pid, icon, name, path, component, layout, sort, hide, metadata,
    created_by, created_at, updated_by, updated_at, deleted, version
)
SELECT
    gen_random_uuid(), (SELECT id FROM oa_menu), v.icon, v.name, v.path, v.component, v.layout, v.sort, false, '{}'::jsonb,
    NULL, NOW(), NULL, NOW(), NULL, 0
FROM (
    VALUES
        ('icon-module', '资产管理', 'asset',      'OA/Asset/index',      NULL, 1),
        ('icon-module', '考勤管理', 'attendance', 'OA/Attendance/index', NULL, 2),
        ('icon-module', '日历管理', 'calendar',   'OA/Calendar/index',   NULL, 3),
        ('icon-module', '通讯录',   'contact',    'OA/Contact/index',    NULL, 4),
        ('icon-module', '合同管理', 'contract',   'OA/Contract/index',   NULL, 5),
        ('icon-module', '文档管理', 'document',   'OA/Document/index',   NULL, 6),
        ('icon-module', '会议管理', 'meeting',    'OA/Meeting/index',    NULL, 7),
        ('icon-module', '公告通知', 'notice',     'OA/Notice/index',     NULL, 8),
        ('icon-module', '报表管理', 'report',     'OA/Report/index',     NULL, 9)
) AS v(icon, name, path, component, layout, sort);
