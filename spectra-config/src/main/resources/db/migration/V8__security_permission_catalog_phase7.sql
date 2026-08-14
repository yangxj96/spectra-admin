-- Phase 7 Permission Catalog additions for controller and operations migration.
-- New rows are target permissions; legacy ROLE_* and uppercase codes are not seeded.
INSERT INTO spectra_security.permission (
    id,
    code,
    name,
    resource_code,
    action_code,
    allowed_scope_modes,
    state,
    system_managed
)
VALUES
        (md5('region:create')::uuid, 'region:create', '行政区划创建', 'region', 'create', 'NONE', 'ACTIVE', TRUE),
        (md5('region:update')::uuid, 'region:update', '行政区划维护', 'region', 'update', 'NONE', 'ACTIVE', TRUE),
        (md5('region:disable')::uuid, 'region:disable', '行政区划停用', 'region', 'disable', 'NONE', 'ACTIVE', TRUE),
        (md5('security:config:read')::uuid, 'security:config:read', '系统配置查看', 'security:config', 'read', 'NONE', 'ACTIVE', TRUE),
        (md5('security:config:update')::uuid, 'security:config:update', '系统配置维护', 'security:config', 'update', 'NONE', 'ACTIVE', TRUE),
        (md5('security:crypto:manage')::uuid, 'security:crypto:manage', '加解密密钥治理', 'security:crypto', 'manage', 'NONE', 'ACTIVE', TRUE),
        (md5('permission:manage')::uuid, 'permission:manage', '权限目录维护', 'permission', 'manage', 'NONE', 'ACTIVE', TRUE),
        (md5('system:monitor:read')::uuid, 'system:monitor:read', '系统运行状态查看', 'system:monitor', 'read', 'NONE', 'ACTIVE', TRUE),
        (md5('file:admin:read')::uuid, 'file:admin:read', '文件资产管理查看', 'file:admin', 'read', 'NONE', 'ACTIVE', TRUE),
        (md5('file:admin:delete')::uuid, 'file:admin:delete', '文件资产管理删除', 'file:admin', 'delete', 'NONE', 'ACTIVE', TRUE),
        (md5('notification:admin:read')::uuid, 'notification:admin:read', '通知运维记录查看', 'notification:admin', 'read', 'NONE', 'ACTIVE', TRUE),
        (md5('notification:admin:retry')::uuid, 'notification:admin:retry', '通知任务重试', 'notification:admin', 'retry', 'NONE', 'ACTIVE', TRUE),
        (md5('notification:admin:cancel')::uuid, 'notification:admin:cancel', '通知任务取消', 'notification:admin', 'cancel', 'NONE', 'ACTIVE', TRUE)
ON CONFLICT (code) DO NOTHING;

-- Target navigation seed: System Management -> Security Operations -> Security Context.
-- role_menu remains a separate relation and is populated by role provisioning, not by Permission rows.
INSERT INTO spectra_core.sys_menu (id,name,pid,icon,menu_type,route_name,sort,created_by,created_at,updated_by,updated_at,deleted,version)
VALUES ('019fdba9-f00a-7716-918c-0ca1ae929b62','安全运维','019bdfc5-b32c-74e9-90ac-0540954c4e4a','icon-security','DIRECTORY',NULL,8,NULL,CURRENT_TIMESTAMP,NULL,CURRENT_TIMESTAMP,NULL,0)
ON CONFLICT (id) DO NOTHING;
INSERT INTO spectra_core.sys_menu (id,name,pid,icon,menu_type,route_name,sort,created_by,created_at,updated_by,updated_at,deleted,version)
VALUES ('019fdba9-f00a-7716-918c-0ca1ae929b63','安全上下文','019fdba9-f00a-7716-918c-0ca1ae929b62','icon-security','MENU','SystemSecurityContext',0,NULL,CURRENT_TIMESTAMP,NULL,CURRENT_TIMESTAMP,NULL,0)
ON CONFLICT (id) DO NOTHING;
