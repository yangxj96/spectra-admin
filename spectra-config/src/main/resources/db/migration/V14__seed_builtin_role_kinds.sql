-- 角色类型基线：DEV_OPS、SYSTEM_ADMIN、AUDITOR 各保留唯一的内置角色。
-- 当前已存在的“系统管理员”普通角色被收敛为 ROLE_ADMIN_SYSTEM，保留其授权实例和关系。
DO $$
DECLARE
    canonical_id      UUID;
    canonical_version BIGINT;
    old_code          VARCHAR(80);
    duplicate_count   INTEGER;
BEGIN
    -- Root 运维角色
    SELECT id, code
      INTO canonical_id, old_code
      FROM spectra_security.sec_role
     WHERE deleted IS NULL
       AND (code = 'ROLE_DEV_OPS' OR role_kind = 'DEV_OPS')
     ORDER BY CASE WHEN code = 'ROLE_DEV_OPS' THEN 0 ELSE 1 END, system_managed DESC, id
     LIMIT 1;

    IF canonical_id IS NULL THEN
        INSERT INTO spectra_security.sec_role
            (id, code, name, authority_level, state, role_kind, system_managed, remark, version)
        VALUES
            ('e716069e-b170-e939-3317-1fadec2d9dbf', 'ROLE_DEV_OPS', 'Root 运维管理员', 1000, 'ACTIVE',
             'DEV_OPS', true, '初始化默认 Root；高风险授权仍受审计与治理流程约束', 0);
        canonical_id := 'e716069e-b170-e939-3317-1fadec2d9dbf';
    ELSE
        SELECT COUNT(*)
          INTO duplicate_count
          FROM spectra_security.sec_role
         WHERE deleted IS NULL
           AND role_kind = 'DEV_OPS'
           AND id <> canonical_id;
        IF duplicate_count > 0 THEN
            RAISE EXCEPTION '存在多个有效 DEV_OPS 角色，无法自动收敛';
        END IF;
        UPDATE spectra_security.sec_role
           SET code = 'ROLE_DEV_OPS',
               name = 'Root 运维管理员',
               authority_level = 1000,
               state = 'ACTIVE',
               role_kind = 'DEV_OPS',
               system_managed = true,
               version = COALESCE(version, 0) + 1
         WHERE id = canonical_id;
    END IF;

    -- 系统管理员角色。优先复用已有规范编码，其次收敛当前“系统管理员”普通角色。
    canonical_id := NULL;
    old_code := NULL;
    SELECT id, code
      INTO canonical_id, old_code
      FROM spectra_security.sec_role
     WHERE deleted IS NULL
       AND (code = 'ROLE_ADMIN_SYSTEM'
            OR role_kind = 'SYSTEM_ADMIN'
            OR (role_kind = 'BUSINESS' AND name = '系统管理员'))
     ORDER BY CASE WHEN code = 'ROLE_ADMIN_SYSTEM' THEN 0
                   WHEN role_kind = 'SYSTEM_ADMIN' THEN 1
                   ELSE 2 END,
              system_managed DESC, id
     LIMIT 1;

    IF canonical_id IS NULL THEN
        INSERT INTO spectra_security.sec_role
            (id, code, name, authority_level, state, role_kind, system_managed, remark, version)
        VALUES
            ('c3a6a45c-3a62-4e7d-9a48-4e7f9f0c6e41', 'ROLE_ADMIN_SYSTEM', '系统管理员', 100, 'ACTIVE',
             'SYSTEM_ADMIN', true, '内置系统管理员角色，不可修改', 0)
        RETURNING id INTO canonical_id;
    ELSE
        IF old_code <> 'ROLE_ADMIN_SYSTEM' THEN
            UPDATE spectra_security.sec_authorization_profile_assignment
               SET role_code = 'ROLE_ADMIN_SYSTEM'
             WHERE deleted IS NULL
               AND role_code = old_code;
        END IF;
        UPDATE spectra_security.sec_role
           SET code = 'ROLE_ADMIN_SYSTEM',
               name = '系统管理员',
               authority_level = 100,
               state = 'ACTIVE',
               role_kind = 'SYSTEM_ADMIN',
               system_managed = true,
               remark = '内置系统管理员角色，不可修改',
               version = COALESCE(version, 0) + 1
         WHERE id = canonical_id;
    END IF;

    SELECT version
      INTO canonical_version
      FROM spectra_security.sec_role
     WHERE id = canonical_id;
    UPDATE spectra_security.sec_authorization_profile_assignment
       SET role_version = canonical_version
     WHERE deleted IS NULL
       AND role_code = 'ROLE_ADMIN_SYSTEM';

    SELECT COUNT(*)
      INTO duplicate_count
      FROM spectra_security.sec_role
     WHERE deleted IS NULL
       AND role_kind = 'SYSTEM_ADMIN'
       AND id <> canonical_id;
    IF duplicate_count > 0 THEN
        RAISE EXCEPTION '存在多个有效 SYSTEM_ADMIN 角色，无法自动收敛';
    END IF;

    -- 审计角色
    canonical_id := NULL;
    SELECT id
      INTO canonical_id
      FROM spectra_security.sec_role
     WHERE deleted IS NULL
       AND (code = 'ROLE_AUDIT' OR role_kind = 'AUDITOR' OR (role_kind = 'BUSINESS' AND name = '审计员'))
     ORDER BY CASE WHEN code = 'ROLE_AUDIT' THEN 0
                   WHEN role_kind = 'AUDITOR' THEN 1
                   ELSE 2 END,
              system_managed DESC, id
     LIMIT 1;

    IF canonical_id IS NULL THEN
        INSERT INTO spectra_security.sec_role
            (id, code, name, authority_level, state, role_kind, system_managed, remark, version)
        VALUES
            ('f32e6c5c-3f8f-4df8-8f0e-2c6b5c8e6a3b', 'ROLE_AUDIT', '审计员', 10, 'ACTIVE',
             'AUDITOR', true, '内置审计角色，不可修改', 0)
        RETURNING id INTO canonical_id;
    ELSE
        UPDATE spectra_security.sec_role
           SET code = 'ROLE_AUDIT',
               name = '审计员',
               authority_level = 10,
               state = 'ACTIVE',
               role_kind = 'AUDITOR',
               system_managed = true,
               remark = '内置审计角色，不可修改',
               version = COALESCE(version, 0) + 1
         WHERE id = canonical_id;
    END IF;

    SELECT COUNT(*)
      INTO duplicate_count
      FROM spectra_security.sec_role
     WHERE deleted IS NULL
       AND role_kind = 'AUDITOR'
       AND id <> canonical_id;
    IF duplicate_count > 0 THEN
        RAISE EXCEPTION '存在多个有效 AUDITOR 角色，无法自动收敛';
    END IF;

    -- 审计角色只授予审计查看和导出能力，并挂载安全审计菜单。
    INSERT INTO spectra_security.sec_role_permission
        (id, role_id, permission_id, created_at, updated_at, version)
    SELECT gen_random_uuid(), canonical_id, permission.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
      FROM spectra_security.sec_permission permission
     WHERE permission.code IN ('audit:read', 'audit:export')
       AND permission.deleted IS NULL
    ON CONFLICT (role_id, permission_id) DO UPDATE
        SET deleted = NULL,
            updated_at = CURRENT_TIMESTAMP,
            version = spectra_security.sec_role_permission.version + 1;

    INSERT INTO spectra_security.sec_role_menu
        (id, role_id, menu_id, created_at, updated_at, version)
    SELECT gen_random_uuid(), canonical_id, menu.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
      FROM spectra_core.sys_menu menu
     WHERE menu.route_name = 'SystemSecurityAudit'
       AND menu.deleted IS NULL
    ON CONFLICT (role_id, menu_id) DO UPDATE
        SET deleted = NULL,
            updated_at = CURRENT_TIMESTAMP,
            version = spectra_security.sec_role_menu.version + 1;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_sec_role_protected_kind_active
    ON spectra_security.sec_role (role_kind)
    WHERE deleted IS NULL
      AND role_kind IN ('DEV_OPS', 'SYSTEM_ADMIN', 'AUDITOR');
