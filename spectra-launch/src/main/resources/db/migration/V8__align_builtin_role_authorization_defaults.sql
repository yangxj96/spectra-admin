-- Built-in role baselines are managed here; ROLE_DEV_OPS retains its implicit Root wildcard.
CREATE TEMP TABLE _builtin_role_permission_baseline (
    role_code varchar(80) NOT NULL,
    permission_code varchar(120) NOT NULL,
    PRIMARY KEY (role_code, permission_code)
) ON COMMIT DROP;

INSERT INTO _builtin_role_permission_baseline (role_code, permission_code) VALUES
    ('ROLE_ADMIN_SYSTEM', 'user:create'),
    ('ROLE_ADMIN_SYSTEM', 'user:read'),
    ('ROLE_ADMIN_SYSTEM', 'user:update'),
    ('ROLE_ADMIN_SYSTEM', 'user:disable'),
    ('ROLE_ADMIN_SYSTEM', 'user:reset-password'),
    ('ROLE_ADMIN_SYSTEM', 'user:unlock'),
    ('ROLE_ADMIN_SYSTEM', 'user:assign-role'),
    ('ROLE_ADMIN_SYSTEM', 'role:assign'),
    ('ROLE_ADMIN_SYSTEM', 'role:create'),
    ('ROLE_ADMIN_SYSTEM', 'role:read'),
    ('ROLE_ADMIN_SYSTEM', 'role:update'),
    ('ROLE_ADMIN_SYSTEM', 'role:delete'),
    ('ROLE_ADMIN_SYSTEM', 'role:disable'),
    ('ROLE_ADMIN_SYSTEM', 'role:grant'),
    ('ROLE_ADMIN_SYSTEM', 'role:authority-level:update'),
    ('ROLE_ADMIN_SYSTEM', 'permission:read'),
    ('ROLE_ADMIN_SYSTEM', 'menu:create'),
    ('ROLE_ADMIN_SYSTEM', 'menu:read'),
    ('ROLE_ADMIN_SYSTEM', 'menu:update'),
    ('ROLE_ADMIN_SYSTEM', 'menu:disable'),
    ('ROLE_ADMIN_SYSTEM', 'department:create'),
    ('ROLE_ADMIN_SYSTEM', 'department:read'),
    ('ROLE_ADMIN_SYSTEM', 'department:update'),
    ('ROLE_ADMIN_SYSTEM', 'dictionary:create'),
    ('ROLE_ADMIN_SYSTEM', 'dictionary:read'),
    ('ROLE_ADMIN_SYSTEM', 'dictionary:update'),
    ('ROLE_ADMIN_SYSTEM', 'dictionary:disable'),
    ('ROLE_ADMIN_SYSTEM', 'region:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:application:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:application:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:asset:create'),
    ('ROLE_ADMIN_SYSTEM', 'oa:asset:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:asset:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:contact:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:contract:create'),
    ('ROLE_ADMIN_SYSTEM', 'oa:contract:delete'),
    ('ROLE_ADMIN_SYSTEM', 'oa:contract:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:contract:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:document:create'),
    ('ROLE_ADMIN_SYSTEM', 'oa:document:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:document:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:leave:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:leave:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:meeting:create'),
    ('ROLE_ADMIN_SYSTEM', 'oa:meeting:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:meeting:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:notice:create'),
    ('ROLE_ADMIN_SYSTEM', 'oa:notice:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:notice:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:purchase:create'),
    ('ROLE_ADMIN_SYSTEM', 'oa:purchase:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:purchase:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:reimbursement:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:reimbursement:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:report:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:application-type:create'),
    ('ROLE_ADMIN_SYSTEM', 'oa:application-type:read'),
    ('ROLE_ADMIN_SYSTEM', 'oa:application-type:update'),
    ('ROLE_ADMIN_SYSTEM', 'oa:application-type:disable'),
    ('ROLE_ADMIN_SYSTEM', 'workflow:process:create'),
    ('ROLE_ADMIN_SYSTEM', 'workflow:process:read'),
    ('ROLE_ADMIN_SYSTEM', 'workflow:process:update'),
    ('ROLE_ADMIN_SYSTEM', 'workflow:instance:read'),
    ('ROLE_ADMIN_SYSTEM', 'workflow:instance:update'),
    ('ROLE_ADMIN_SYSTEM', 'workflow:task:read'),
    ('ROLE_ADMIN_SYSTEM', 'workflow:task:update'),
    ('ROLE_AUDIT', 'audit:read'),
    ('ROLE_AUDIT', 'audit:export'),
    ('ROLE_AUDIT', 'user:read'),
    ('ROLE_AUDIT', 'role:read'),
    ('ROLE_AUDIT', 'permission:read'),
    ('ROLE_AUDIT', 'menu:read'),
    ('ROLE_AUDIT', 'department:read'),
    ('ROLE_AUDIT', 'dictionary:read'),
    ('ROLE_AUDIT', 'region:read'),
    ('ROLE_AUDIT', 'file:read'),
    ('ROLE_AUDIT', 'oa:application:read'),
    ('ROLE_AUDIT', 'oa:asset:read'),
    ('ROLE_AUDIT', 'oa:contact:read'),
    ('ROLE_AUDIT', 'oa:contract:read'),
    ('ROLE_AUDIT', 'oa:document:read'),
    ('ROLE_AUDIT', 'oa:leave:read'),
    ('ROLE_AUDIT', 'oa:meeting:read'),
    ('ROLE_AUDIT', 'oa:notice:read'),
    ('ROLE_AUDIT', 'oa:purchase:read'),
    ('ROLE_AUDIT', 'oa:reimbursement:read'),
    ('ROLE_AUDIT', 'oa:report:read'),
    ('ROLE_AUDIT', 'oa:application-type:read'),
    ('ROLE_AUDIT', 'workflow:process:read'),
    ('ROLE_AUDIT', 'workflow:instance:read'),
    ('ROLE_AUDIT', 'workflow:task:read'),
    ('ROLE_AUDIT', 'notification:provider:read'),
    ('ROLE_AUDIT', 'notification:template:read'),
    ('ROLE_USER', 'account:read'),
    ('ROLE_USER', 'account:update'),
    ('ROLE_USER', 'notification-setting:read'),
    ('ROLE_USER', 'notification-setting:update'),
    ('ROLE_USER', 'notification:read'),
    ('ROLE_USER', 'notification:update'),
    ('ROLE_USER', 'notification:delete'),
    ('ROLE_USER', 'oa:calendar:create'),
    ('ROLE_USER', 'oa:calendar:read'),
    ('ROLE_USER', 'oa:calendar:update'),
    ('ROLE_USER', 'oa:calendar:delete'),
    ('ROLE_USER', 'oa:workbench:read'),
    ('ROLE_USER', 'workflow:instance:create');

DO $$
DECLARE
    missing_permissions text;
BEGIN
    SELECT string_agg(baseline.permission_code, ', ' ORDER BY baseline.permission_code)
    INTO missing_permissions
    FROM _builtin_role_permission_baseline baseline
    LEFT JOIN spectra_security.sec_permission permission
        ON permission.code = baseline.permission_code
       AND permission.deleted IS NULL
       AND permission.state = 'ACTIVE'
    WHERE permission.id IS NULL;

    IF missing_permissions IS NOT NULL THEN
        RAISE EXCEPTION 'Built-in role migration refers to missing active permissions: %', missing_permissions;
    END IF;
END $$;

CREATE TEMP TABLE _builtin_role_grantable_baseline ON COMMIT DROP AS
SELECT baseline.role_code, baseline.permission_code
FROM _builtin_role_permission_baseline baseline
WHERE baseline.role_code = 'ROLE_ADMIN_SYSTEM'
  AND baseline.permission_code NOT IN (
      'user:assign-role',
      'role:assign',
      'role:create',
      'role:update',
      'role:delete',
      'role:disable',
      'role:grant',
      'role:authority-level:update'
  );

-- Auditing is globally visible only through the dedicated high-risk visibility policy.
UPDATE spectra_security.sec_permission
SET allowed_scope_modes = 'NONE,RULES',
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE code IN ('audit:read', 'audit:export')
  AND deleted IS NULL
  AND allowed_scope_modes <> 'NONE,RULES';

UPDATE spectra_core.sys_menu
SET name = CASE route_name
        WHEN 'OASupply' THEN '办公用品'
        WHEN 'OALeave' THEN '请假申请'
        WHEN 'OAApplicationTypes' THEN '申请类型'
    END,
    pid = 'd72fa359-102a-4b17-8d21-6d78b78cff34',
    icon = 'icon-module',
    menu_type = 'MENU',
    deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE route_name IN ('OASupply', 'OALeave', 'OAApplicationTypes')
  AND deleted IS NOT NULL;

INSERT INTO spectra_core.sys_menu (
    id, name, pid, icon, menu_type, route_name, sort,
    created_by, created_at, updated_by, updated_at, deleted, version
)
SELECT uuidv7(), new_menu.name, 'd72fa359-102a-4b17-8d21-6d78b78cff34', 'icon-module', 'MENU',
       new_menu.route_name, new_menu.sort,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0
FROM (VALUES
    ('办公用品', 'OASupply', 40),
    ('请假申请', 'OALeave', 50),
    ('申请类型', 'OAApplicationTypes', 60)
) AS new_menu(name, route_name, sort)
WHERE NOT EXISTS (
    SELECT 1
    FROM spectra_core.sys_menu existing
    WHERE existing.route_name = new_menu.route_name
      AND existing.deleted IS NULL
);

DO $$
DECLARE
    missing_menus text;
BEGIN
    WITH expected(route_name) AS (
        VALUES ('OASupply'), ('OALeave'), ('OAApplicationTypes')
    )
    SELECT string_agg(expected.route_name, ', ' ORDER BY expected.route_name)
    INTO missing_menus
    FROM expected
    WHERE NOT EXISTS (
        SELECT 1 FROM spectra_core.sys_menu menu
        WHERE menu.route_name = expected.route_name
          AND menu.menu_type = 'MENU'
          AND menu.deleted IS NULL
    );

    IF missing_menus IS NOT NULL THEN
        RAISE EXCEPTION 'Built-in role migration could not establish active page menus: %', missing_menus;
    END IF;
END $$;

CREATE TEMP TABLE _builtin_role_menu_baseline (
    role_code varchar(80) NOT NULL,
    route_name varchar(120) NOT NULL,
    PRIMARY KEY (role_code, route_name)
) ON COMMIT DROP;

INSERT INTO _builtin_role_menu_baseline (role_code, route_name) VALUES
    ('ROLE_ADMIN_SYSTEM', 'Dashboard'),
    ('ROLE_ADMIN_SYSTEM', 'SystemUser'),
    ('ROLE_ADMIN_SYSTEM', 'SystemRoleManagement'),
    ('ROLE_ADMIN_SYSTEM', 'SystemDept'),
    ('ROLE_ADMIN_SYSTEM', 'SystemDict'),
    ('ROLE_ADMIN_SYSTEM', 'SystemMenu'),
    ('ROLE_ADMIN_SYSTEM', 'SystemWorkflow'),
    ('ROLE_ADMIN_SYSTEM', 'SystemRegion'),
    ('ROLE_ADMIN_SYSTEM', 'OAAsset'),
    ('ROLE_ADMIN_SYSTEM', 'OASupply'),
    ('ROLE_ADMIN_SYSTEM', 'OALeave'),
    ('ROLE_ADMIN_SYSTEM', 'OAApplicationTypes'),
    ('ROLE_ADMIN_SYSTEM', 'OAContact'),
    ('ROLE_ADMIN_SYSTEM', 'OAContract'),
    ('ROLE_ADMIN_SYSTEM', 'OADocument'),
    ('ROLE_ADMIN_SYSTEM', 'OAMeeting'),
    ('ROLE_ADMIN_SYSTEM', 'OANotice'),
    ('ROLE_ADMIN_SYSTEM', 'OAPurchase'),
    ('ROLE_ADMIN_SYSTEM', 'OAReimbursement'),
    ('ROLE_ADMIN_SYSTEM', 'OAReport'),
    ('ROLE_ADMIN_SYSTEM', 'OAApproval'),
    ('ROLE_ADMIN_SYSTEM', 'OAApprovalReimbursement'),
    ('ROLE_ADMIN_SYSTEM', 'OAApprovalPurchase'),
    ('ROLE_ADMIN_SYSTEM', 'OAApprovalLeave'),
    ('ROLE_AUDIT', 'Dashboard'),
    ('ROLE_AUDIT', 'SystemUser'),
    ('ROLE_AUDIT', 'SystemRoleManagement'),
    ('ROLE_AUDIT', 'SystemDept'),
    ('ROLE_AUDIT', 'SystemDict'),
    ('ROLE_AUDIT', 'SystemMenu'),
    ('ROLE_AUDIT', 'SystemWorkflow'),
    ('ROLE_AUDIT', 'SystemRegion'),
    ('ROLE_AUDIT', 'DevopsAuditLog'),
    ('ROLE_AUDIT', 'DevopsNotificationTemplate'),
    ('ROLE_AUDIT', 'DevopsNotificationProvider'),
    ('ROLE_AUDIT', 'OAAsset'),
    ('ROLE_AUDIT', 'OASupply'),
    ('ROLE_AUDIT', 'OALeave'),
    ('ROLE_AUDIT', 'OAApplicationTypes'),
    ('ROLE_AUDIT', 'OAContact'),
    ('ROLE_AUDIT', 'OAContract'),
    ('ROLE_AUDIT', 'OADocument'),
    ('ROLE_AUDIT', 'OAMeeting'),
    ('ROLE_AUDIT', 'OANotice'),
    ('ROLE_AUDIT', 'OAPurchase'),
    ('ROLE_AUDIT', 'OAReimbursement'),
    ('ROLE_AUDIT', 'OAReport'),
    ('ROLE_AUDIT', 'OAApproval'),
    ('ROLE_AUDIT', 'OAApprovalReimbursement'),
    ('ROLE_AUDIT', 'OAApprovalPurchase'),
    ('ROLE_AUDIT', 'OAApprovalLeave'),
    ('ROLE_USER', 'Dashboard'),
    ('ROLE_USER', 'OACalendar');

DO $$
DECLARE
    missing_menus text;
BEGIN
    SELECT string_agg(baseline.role_code || ':' || baseline.route_name, ', ' ORDER BY baseline.role_code, baseline.route_name)
    INTO missing_menus
    FROM _builtin_role_menu_baseline baseline
    WHERE NOT EXISTS (
        SELECT 1 FROM spectra_core.sys_menu menu
        WHERE menu.route_name = baseline.route_name
          AND menu.menu_type = 'MENU'
          AND menu.deleted IS NULL
    );

    IF missing_menus IS NOT NULL THEN
        RAISE EXCEPTION 'Built-in role migration refers to missing active route menus: %', missing_menus;
    END IF;
END $$;

CREATE TEMP TABLE _builtin_default_assignment_scope (
    role_code varchar(80) NOT NULL,
    permission_code varchar(120) NOT NULL,
    scope_mode varchar(8) NOT NULL,
    PRIMARY KEY (role_code, permission_code)
) ON COMMIT DROP;

INSERT INTO _builtin_default_assignment_scope (role_code, permission_code, scope_mode)
SELECT role_code, permission_code, 'SELF'
FROM _builtin_role_permission_baseline
WHERE role_code = 'ROLE_USER'
UNION ALL
SELECT baseline.role_code, baseline.permission_code, 'NONE'
FROM _builtin_role_permission_baseline baseline
JOIN spectra_security.sec_permission permission
    ON permission.code = baseline.permission_code
   AND permission.deleted IS NULL
WHERE baseline.role_code IN ('ROLE_ADMIN_SYSTEM', 'ROLE_AUDIT')
  AND EXISTS (
      SELECT 1
      FROM unnest(string_to_array(permission.allowed_scope_modes, ',')) allowed_mode
      WHERE upper(btrim(allowed_mode)) = 'NONE'
  );

CREATE TEMP TABLE _builtin_default_assignment_grant_scope (
    role_code varchar(80) NOT NULL,
    permission_code varchar(120) NOT NULL,
    scope_mode varchar(8) NOT NULL,
    PRIMARY KEY (role_code, permission_code)
) ON COMMIT DROP;

INSERT INTO _builtin_default_assignment_grant_scope (role_code, permission_code, scope_mode)
SELECT baseline.role_code, baseline.permission_code, 'NONE'
FROM _builtin_role_grantable_baseline baseline
JOIN spectra_security.sec_permission permission
    ON permission.code = baseline.permission_code
   AND permission.deleted IS NULL
WHERE EXISTS (
    SELECT 1
    FROM unnest(string_to_array(permission.allowed_scope_modes, ',')) allowed_mode
    WHERE upper(btrim(allowed_mode)) = 'NONE'
);

CREATE TEMP TABLE _builtin_stale_assignment_boundaries ON COMMIT DROP AS
SELECT DISTINCT
       boundary.id AS boundary_id,
       role.code AS role_code,
       assignment.id AS assignment_id
FROM spectra_security.sec_assignment_permission_boundary boundary
JOIN spectra_security.sec_role_assignment assignment
    ON assignment.id = boundary.assignment_id
   AND assignment.deleted IS NULL
JOIN spectra_security.sec_role role
    ON role.id = assignment.role_id
   AND role.deleted IS NULL
JOIN spectra_security.sec_permission permission
    ON permission.id = boundary.permission_id
JOIN spectra_security.sec_authorization_scope scope
    ON scope.id = boundary.scope_id
   AND scope.deleted IS NULL
WHERE boundary.deleted IS NULL
  AND role.code IN ('ROLE_ADMIN_SYSTEM', 'ROLE_AUDIT', 'ROLE_USER')
  AND (
      NOT EXISTS (
          SELECT 1 FROM _builtin_role_permission_baseline expected
          WHERE expected.role_code = role.code
            AND expected.permission_code = permission.code
      )
      OR EXISTS (
          SELECT 1 FROM _builtin_default_assignment_scope expected
          WHERE expected.role_code = role.code
            AND expected.permission_code = permission.code
            AND expected.scope_mode <> scope.scope_mode
      )
      OR (
          NOT EXISTS (
              SELECT 1 FROM _builtin_default_assignment_scope expected
              WHERE expected.role_code = role.code
                AND expected.permission_code = permission.code
          )
          AND (
              scope.scope_mode = 'ALL'
              OR NOT EXISTS (
                  SELECT 1
                  FROM unnest(string_to_array(permission.allowed_scope_modes, ',')) allowed_mode
                  WHERE upper(btrim(allowed_mode)) = scope.scope_mode
              )
              OR (scope.scope_mode = 'RULES' AND NOT EXISTS (
                  SELECT 1 FROM spectra_security.sec_scope_rule rule
                  WHERE rule.scope_id = scope.id
                    AND rule.deleted IS NULL
                    AND rule.rule_type = 'DEPARTMENT'
                    AND rule.department_id IS NOT NULL
              ))
          )
      )
  );

CREATE TEMP TABLE _builtin_stale_assignment_grant_boundaries ON COMMIT DROP AS
SELECT DISTINCT
       boundary.id AS boundary_id,
       role.code AS role_code,
       assignment.id AS assignment_id
FROM spectra_security.sec_assignment_grant_boundary boundary
JOIN spectra_security.sec_role_assignment assignment
    ON assignment.id = boundary.assignment_id
   AND assignment.deleted IS NULL
JOIN spectra_security.sec_role role
    ON role.id = assignment.role_id
   AND role.deleted IS NULL
JOIN spectra_security.sec_permission permission
    ON permission.id = boundary.permission_id
JOIN spectra_security.sec_authorization_scope scope
    ON scope.id = boundary.scope_id
   AND scope.deleted IS NULL
WHERE boundary.deleted IS NULL
  AND role.code IN ('ROLE_ADMIN_SYSTEM', 'ROLE_AUDIT', 'ROLE_USER')
  AND (
      NOT EXISTS (
          SELECT 1 FROM _builtin_role_grantable_baseline expected
          WHERE expected.role_code = role.code
            AND expected.permission_code = permission.code
      )
      OR EXISTS (
          SELECT 1 FROM _builtin_default_assignment_grant_scope expected
          WHERE expected.role_code = role.code
            AND expected.permission_code = permission.code
            AND expected.scope_mode <> scope.scope_mode
      )
      OR (
          NOT EXISTS (
              SELECT 1 FROM _builtin_default_assignment_grant_scope expected
              WHERE expected.role_code = role.code
                AND expected.permission_code = permission.code
          )
          AND (
              scope.scope_mode = 'ALL'
              OR NOT EXISTS (
                  SELECT 1
                  FROM unnest(string_to_array(permission.allowed_scope_modes, ',')) allowed_mode
                  WHERE upper(btrim(allowed_mode)) = scope.scope_mode
              )
              OR (scope.scope_mode = 'RULES' AND NOT EXISTS (
                  SELECT 1 FROM spectra_security.sec_scope_rule rule
                  WHERE rule.scope_id = scope.id
                    AND rule.deleted IS NULL
                    AND rule.rule_type = 'DEPARTMENT'
                    AND rule.department_id IS NOT NULL
              ))
          )
      )
  );

DO $$
DECLARE
    affected_assignments jsonb;
BEGIN
    SELECT jsonb_agg(jsonb_build_object('roleCode', changed.role_code, 'assignmentId', changed.assignment_id))
    INTO affected_assignments
    FROM (
        SELECT DISTINCT role_code, assignment_id
        FROM _builtin_stale_assignment_boundaries
        ORDER BY role_code, assignment_id
    ) changed;

    IF affected_assignments IS NOT NULL THEN
        RAISE NOTICE 'Built-in role migration will revoke stale access boundaries: %', affected_assignments;
    END IF;
END $$;

DO $$
DECLARE
    affected_assignments jsonb;
BEGIN
    SELECT jsonb_agg(jsonb_build_object('roleCode', changed.role_code, 'assignmentId', changed.assignment_id))
    INTO affected_assignments
    FROM (
        SELECT DISTINCT role_code, assignment_id
        FROM _builtin_stale_assignment_grant_boundaries
        ORDER BY role_code, assignment_id
    ) changed;

    IF affected_assignments IS NOT NULL THEN
        RAISE NOTICE 'Built-in role migration will revoke stale grant boundaries: %', affected_assignments;
    END IF;
END $$;

UPDATE spectra_security.sec_assignment_permission_boundary boundary
SET deleted = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
FROM _builtin_stale_assignment_boundaries stale
WHERE boundary.id = stale.boundary_id
  AND boundary.deleted IS NULL;

UPDATE spectra_security.sec_assignment_grant_boundary grant_boundary
SET deleted = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
FROM _builtin_stale_assignment_grant_boundaries stale
WHERE grant_boundary.id = stale.boundary_id
  AND grant_boundary.deleted IS NULL;

UPDATE spectra_security.sec_role_grantable_permission grantable
SET deleted = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP,
    version = grantable.version + 1
FROM spectra_security.sec_role role, spectra_security.sec_permission permission
WHERE grantable.role_id = role.id
  AND permission.id = grantable.permission_id
  AND role.code IN ('ROLE_ADMIN_SYSTEM', 'ROLE_AUDIT', 'ROLE_USER')
  AND role.deleted IS NULL
  AND grantable.deleted IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM _builtin_role_grantable_baseline baseline
      WHERE baseline.role_code = role.code
        AND baseline.permission_code = permission.code
  );

UPDATE spectra_security.sec_role_permission role_permission
SET deleted = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP,
    version = role_permission.version + 1
FROM spectra_security.sec_role role, spectra_security.sec_permission permission
WHERE role_permission.role_id = role.id
  AND permission.id = role_permission.permission_id
  AND role.code IN ('ROLE_ADMIN_SYSTEM', 'ROLE_AUDIT', 'ROLE_USER')
  AND role.deleted IS NULL
  AND role_permission.deleted IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM _builtin_role_permission_baseline baseline
      WHERE baseline.role_code = role.code
        AND baseline.permission_code = permission.code
  );

INSERT INTO spectra_security.sec_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM _builtin_role_permission_baseline baseline
JOIN spectra_security.sec_role role
    ON role.code = baseline.role_code
   AND role.deleted IS NULL
JOIN spectra_security.sec_permission permission
    ON permission.code = baseline.permission_code
   AND permission.deleted IS NULL
   AND permission.state = 'ACTIVE'
ON CONFLICT (role_id, permission_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_security.sec_role_permission.version + 1;

INSERT INTO spectra_security.sec_role_grantable_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM _builtin_role_grantable_baseline baseline
JOIN spectra_security.sec_role role
    ON role.code = baseline.role_code
   AND role.deleted IS NULL
JOIN spectra_security.sec_permission permission
    ON permission.code = baseline.permission_code
   AND permission.deleted IS NULL
   AND permission.state = 'ACTIVE'
ON CONFLICT (role_id, permission_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_security.sec_role_grantable_permission.version + 1;

UPDATE spectra_security.sec_role_menu role_menu
SET deleted = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP,
    version = role_menu.version + 1
FROM spectra_security.sec_role role
WHERE role_menu.role_id = role.id
  AND role.code IN ('ROLE_ADMIN_SYSTEM', 'ROLE_AUDIT', 'ROLE_USER')
  AND role.deleted IS NULL
  AND role_menu.deleted IS NULL;

INSERT INTO spectra_security.sec_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM _builtin_role_menu_baseline baseline
JOIN spectra_security.sec_role role
    ON role.code = baseline.role_code
   AND role.deleted IS NULL
JOIN spectra_core.sys_menu menu
    ON menu.route_name = baseline.route_name
   AND menu.menu_type = 'MENU'
   AND menu.deleted IS NULL
ON CONFLICT (role_id, menu_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_security.sec_role_menu.version + 1;

CREATE TEMP TABLE _builtin_missing_default_assignment_scope ON COMMIT DROP AS
SELECT uuidv7() AS scope_id,
       assignment.id AS assignment_id,
       permission.id AS permission_id,
       permission.resource_code,
       expected.scope_mode,
       role.code AS role_code
FROM _builtin_default_assignment_scope expected
JOIN spectra_security.sec_role role
    ON role.code = expected.role_code
   AND role.deleted IS NULL
JOIN spectra_security.sec_role_assignment assignment
    ON assignment.role_id = role.id
   AND assignment.state = 'ACTIVE'
   AND assignment.deleted IS NULL
JOIN spectra_security.sec_permission permission
    ON permission.code = expected.permission_code
   AND permission.deleted IS NULL
   AND permission.state = 'ACTIVE'
LEFT JOIN spectra_security.sec_assignment_permission_boundary boundary
    ON boundary.assignment_id = assignment.id
   AND boundary.permission_id = permission.id
   AND boundary.deleted IS NULL
WHERE boundary.id IS NULL;

INSERT INTO spectra_security.sec_authorization_scope (
    id, scope_mode, resource_code, created_by, created_at, updated_by, updated_at, deleted, version
)
SELECT scope_id, scope_mode, resource_code,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0
FROM _builtin_missing_default_assignment_scope;

CREATE TEMP TABLE _builtin_missing_default_assignment_grant_scope ON COMMIT DROP AS
SELECT uuidv7() AS scope_id,
       assignment.id AS assignment_id,
       permission.id AS permission_id,
       permission.resource_code,
       expected.scope_mode,
       role.code AS role_code
FROM _builtin_default_assignment_grant_scope expected
JOIN spectra_security.sec_role role
    ON role.code = expected.role_code
   AND role.deleted IS NULL
JOIN spectra_security.sec_role_assignment assignment
    ON assignment.role_id = role.id
   AND assignment.state = 'ACTIVE'
   AND assignment.deleted IS NULL
JOIN spectra_security.sec_permission permission
    ON permission.code = expected.permission_code
   AND permission.deleted IS NULL
   AND permission.state = 'ACTIVE'
LEFT JOIN spectra_security.sec_assignment_grant_boundary boundary
    ON boundary.assignment_id = assignment.id
   AND boundary.permission_id = permission.id
   AND boundary.deleted IS NULL
WHERE boundary.id IS NULL;

INSERT INTO spectra_security.sec_authorization_scope (
    id, scope_mode, resource_code, created_by, created_at, updated_by, updated_at, deleted, version
)
SELECT scope_id, scope_mode, resource_code,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0
FROM _builtin_missing_default_assignment_grant_scope;

INSERT INTO spectra_security.sec_assignment_grant_boundary (
    id, assignment_id, permission_id, scope_id,
    created_by, created_at, updated_by, updated_at, deleted, version
)
SELECT uuidv7(), assignment_id, permission_id, scope_id,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0
FROM _builtin_missing_default_assignment_grant_scope;

INSERT INTO spectra_security.sec_assignment_permission_boundary (
    id, assignment_id, permission_id, scope_id,
    created_by, created_at, updated_by, updated_at, deleted, version
)
SELECT uuidv7(), assignment_id, permission_id, scope_id,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0
FROM _builtin_missing_default_assignment_scope;

DO $$
DECLARE
    defaulted_assignments jsonb;
BEGIN
    SELECT jsonb_agg(jsonb_build_object(
        'roleCode', changed.role_code,
        'assignmentId', changed.assignment_id,
        'permissionCount', changed.permission_count
    ))
    INTO defaulted_assignments
    FROM (
        SELECT role_code, assignment_id, count(*) AS permission_count
        FROM _builtin_missing_default_assignment_scope
        GROUP BY role_code, assignment_id
        ORDER BY role_code, assignment_id
    ) changed;

    IF defaulted_assignments IS NOT NULL THEN
        RAISE NOTICE 'Built-in role migration added safe default NONE/SELF boundaries: %', defaulted_assignments;
    END IF;
END $$;

DO $$
DECLARE
    defaulted_grant_assignments jsonb;
BEGIN
    SELECT jsonb_agg(jsonb_build_object(
        'roleCode', changed.role_code,
        'assignmentId', changed.assignment_id,
        'permissionCount', changed.permission_count
    ))
    INTO defaulted_grant_assignments
    FROM (
        SELECT role_code, assignment_id, count(*) AS permission_count
        FROM _builtin_missing_default_assignment_grant_scope
        GROUP BY role_code, assignment_id
        ORDER BY role_code, assignment_id
    ) changed;

    IF defaulted_grant_assignments IS NOT NULL THEN
        RAISE NOTICE 'Built-in role migration added safe default grantable NONE boundaries: %', defaulted_grant_assignments;
    END IF;
END $$;
