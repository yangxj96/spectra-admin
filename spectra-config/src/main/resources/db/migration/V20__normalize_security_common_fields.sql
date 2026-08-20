-- Normalize every deployed spectra_security table to the project database contract.
-- This migration preserves all existing rows and business columns. Relationship
-- and singleton tables receive a UUID technical primary key while their existing
-- business keys remain protected by unique constraints.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    table_name TEXT;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'sec_permission',
        'sec_role',
        'sec_role_permission',
        'sec_role_grantable_permission',
        'sec_role_assignment',
        'sec_authorization_scope',
        'sec_assignment_permission_boundary',
        'sec_assignment_grant_boundary',
        'sec_scope_rule',
        'sec_authentication_identity',
        'sec_password_credential',
        'sec_security_client',
        'sec_authentication_method',
        'sec_client_auth_method',
        'sec_session_policy',
        'sec_password_policy',
        'sec_mfa_enrollment',
        'sec_totp_credential',
        'sec_recovery_code',
        'sec_root_policy',
        'sec_security_audit_event',
        'sec_security_audit_retention_policy',
        'sec_security_audit_archive_manifest',
        'sec_security_change_outbox',
        'sec_role_menu'
    ] LOOP
        EXECUTE format('ALTER TABLE spectra_security.%I ADD COLUMN IF NOT EXISTS created_by UUID', table_name);
        EXECUTE format(
            'ALTER TABLE spectra_security.%I ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP',
            table_name);
        EXECUTE format('ALTER TABLE spectra_security.%I ADD COLUMN IF NOT EXISTS updated_by UUID', table_name);
        EXECUTE format(
            'ALTER TABLE spectra_security.%I ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP',
            table_name);
        EXECUTE format(
            'ALTER TABLE spectra_security.%I ADD COLUMN IF NOT EXISTS deleted TIMESTAMP(6) WITH TIME ZONE',
            table_name);
        EXECUTE format(
            'ALTER TABLE spectra_security.%I ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0',
            table_name);
    END LOOP;
END
$$;

-- Technical UUID identifiers missing from relationship, singleton and archive tables.
ALTER TABLE spectra_security.sec_role_permission
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_role_grantable_permission
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_assignment_permission_boundary
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_assignment_grant_boundary
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_password_credential
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_client_auth_method
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_session_policy
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_password_policy
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_totp_credential
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_root_policy
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_security_audit_event
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_security_audit_retention_policy
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_security_audit_archive_manifest
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE spectra_security.sec_role_menu
    ADD COLUMN IF NOT EXISTS id UUID NOT NULL DEFAULT gen_random_uuid();

-- Existing UUID identifiers must also work for direct SQL seeds and operational inserts.
DO $$
DECLARE
    table_name TEXT;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'sec_permission',
        'sec_role',
        'sec_role_permission',
        'sec_role_grantable_permission',
        'sec_role_assignment',
        'sec_authorization_scope',
        'sec_assignment_permission_boundary',
        'sec_assignment_grant_boundary',
        'sec_scope_rule',
        'sec_authentication_identity',
        'sec_password_credential',
        'sec_security_client',
        'sec_authentication_method',
        'sec_client_auth_method',
        'sec_session_policy',
        'sec_password_policy',
        'sec_mfa_enrollment',
        'sec_totp_credential',
        'sec_recovery_code',
        'sec_root_policy',
        'sec_security_audit_event',
        'sec_security_audit_retention_policy',
        'sec_security_audit_archive_manifest',
        'sec_security_change_outbox',
        'sec_role_menu'
    ] LOOP
        EXECUTE format('ALTER TABLE spectra_security.%I ALTER COLUMN id SET DEFAULT gen_random_uuid()', table_name);
    END LOOP;
END
$$;

-- Preserve the audit event timestamp and actor as the common audit values.
DROP TRIGGER IF EXISTS trg_sec_security_audit_event_immutable
    ON spectra_security.sec_security_audit_event;
UPDATE spectra_security.sec_security_audit_event
SET created_by = operator_id,
    created_at = occurred_at,
    updated_by = operator_id,
    updated_at = occurred_at,
    version = 0;

-- Complete the common-column nullability contract after existing rows are filled.
DO $$
DECLARE
    table_name TEXT;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'sec_permission',
        'sec_role',
        'sec_role_permission',
        'sec_role_grantable_permission',
        'sec_role_assignment',
        'sec_authorization_scope',
        'sec_assignment_permission_boundary',
        'sec_assignment_grant_boundary',
        'sec_scope_rule',
        'sec_authentication_identity',
        'sec_password_credential',
        'sec_security_client',
        'sec_authentication_method',
        'sec_client_auth_method',
        'sec_session_policy',
        'sec_password_policy',
        'sec_mfa_enrollment',
        'sec_totp_credential',
        'sec_recovery_code',
        'sec_root_policy',
        'sec_security_audit_event',
        'sec_security_audit_retention_policy',
        'sec_security_audit_archive_manifest',
        'sec_security_change_outbox',
        'sec_role_menu'
    ] LOOP
        EXECUTE format(
            'UPDATE spectra_security.%I SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP), updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP), version = COALESCE(version, 0)',
            table_name);
        EXECUTE format('ALTER TABLE spectra_security.%I ALTER COLUMN created_at SET NOT NULL', table_name);
        EXECUTE format('ALTER TABLE spectra_security.%I ALTER COLUMN updated_at SET NOT NULL', table_name);
        EXECUTE format('ALTER TABLE spectra_security.%I ALTER COLUMN version SET NOT NULL', table_name);
    END LOOP;
END
$$;

CREATE TRIGGER trg_sec_security_audit_event_immutable
    BEFORE UPDATE OR DELETE ON spectra_security.sec_security_audit_event
    FOR EACH ROW EXECUTE FUNCTION spectra_security.sec_reject_audit_mutation();

-- Relationship rows use a technical UUID primary key and retain their natural uniqueness.
ALTER TABLE spectra_security.sec_role_permission
    DROP CONSTRAINT IF EXISTS pk_sec_role_permission;
ALTER TABLE spectra_security.sec_role_permission
    DROP CONSTRAINT IF EXISTS uk_sec_role_permission_role_permission;
ALTER TABLE spectra_security.sec_role_permission
    ADD CONSTRAINT pk_sec_role_permission PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_role_permission_role_permission UNIQUE (role_id, permission_id);

ALTER TABLE spectra_security.sec_role_grantable_permission
    DROP CONSTRAINT IF EXISTS pk_sec_role_grantable_permission;
ALTER TABLE spectra_security.sec_role_grantable_permission
    DROP CONSTRAINT IF EXISTS uk_sec_role_grantable_permission_role_permission;
ALTER TABLE spectra_security.sec_role_grantable_permission
    ADD CONSTRAINT pk_sec_role_grantable_permission PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_role_grantable_permission_role_permission UNIQUE (role_id, permission_id);

ALTER TABLE spectra_security.sec_assignment_permission_boundary
    DROP CONSTRAINT IF EXISTS pk_sec_assignment_permission_boundary;
ALTER TABLE spectra_security.sec_assignment_permission_boundary
    DROP CONSTRAINT IF EXISTS uk_sec_assignment_permission_boundary_assignment_permission;
ALTER TABLE spectra_security.sec_assignment_permission_boundary
    ADD CONSTRAINT pk_sec_assignment_permission_boundary PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_assignment_permission_boundary_assignment_permission
        UNIQUE (assignment_id, permission_id);

ALTER TABLE spectra_security.sec_assignment_grant_boundary
    DROP CONSTRAINT IF EXISTS pk_sec_assignment_grant_boundary;
ALTER TABLE spectra_security.sec_assignment_grant_boundary
    DROP CONSTRAINT IF EXISTS uk_sec_assignment_grant_boundary_assignment_permission;
ALTER TABLE spectra_security.sec_assignment_grant_boundary
    ADD CONSTRAINT pk_sec_assignment_grant_boundary PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_assignment_grant_boundary_assignment_permission
        UNIQUE (assignment_id, permission_id);

ALTER TABLE spectra_security.sec_role_menu
    DROP CONSTRAINT IF EXISTS pk_sec_role_menu;
ALTER TABLE spectra_security.sec_role_menu
    DROP CONSTRAINT IF EXISTS uk_sec_role_menu_role_menu;
ALTER TABLE spectra_security.sec_role_menu
    ADD CONSTRAINT pk_sec_role_menu PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_role_menu_role_menu UNIQUE (role_id, menu_id);

ALTER TABLE spectra_security.sec_client_auth_method
    DROP CONSTRAINT IF EXISTS pk_sec_client_auth_method;
ALTER TABLE spectra_security.sec_client_auth_method
    DROP CONSTRAINT IF EXISTS uk_sec_client_auth_method_client_method;
ALTER TABLE spectra_security.sec_client_auth_method
    ADD CONSTRAINT pk_sec_client_auth_method PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_client_auth_method_client_method UNIQUE (client_id, authentication_method_id);

-- Single-row/business-key tables use a technical UUID primary key and retain their domain key.
ALTER TABLE spectra_security.sec_password_credential
    DROP CONSTRAINT IF EXISTS pk_sec_password_credential;
ALTER TABLE spectra_security.sec_password_credential
    DROP CONSTRAINT IF EXISTS uk_sec_password_credential_user;
ALTER TABLE spectra_security.sec_password_credential
    ADD CONSTRAINT pk_sec_password_credential PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_password_credential_user UNIQUE (user_id);

ALTER TABLE spectra_security.sec_session_policy
    DROP CONSTRAINT IF EXISTS pk_sec_session_policy;
ALTER TABLE spectra_security.sec_session_policy
    DROP CONSTRAINT IF EXISTS uk_sec_session_policy_client;
ALTER TABLE spectra_security.sec_session_policy
    ADD CONSTRAINT pk_sec_session_policy PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_session_policy_client UNIQUE (client_id);

ALTER TABLE spectra_security.sec_password_policy
    DROP CONSTRAINT IF EXISTS pk_sec_password_policy;
ALTER TABLE spectra_security.sec_password_policy
    DROP CONSTRAINT IF EXISTS uk_sec_password_policy_key;
ALTER TABLE spectra_security.sec_password_policy
    ADD CONSTRAINT pk_sec_password_policy PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_password_policy_key UNIQUE (policy_key);

ALTER TABLE spectra_security.sec_totp_credential
    DROP CONSTRAINT IF EXISTS pk_sec_totp_credential;
ALTER TABLE spectra_security.sec_totp_credential
    DROP CONSTRAINT IF EXISTS uk_sec_totp_credential_enrollment;
ALTER TABLE spectra_security.sec_totp_credential
    ADD CONSTRAINT pk_sec_totp_credential PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_totp_credential_enrollment UNIQUE (enrollment_id);

ALTER TABLE spectra_security.sec_root_policy
    DROP CONSTRAINT IF EXISTS pk_sec_root_policy;
ALTER TABLE spectra_security.sec_root_policy
    DROP CONSTRAINT IF EXISTS uk_sec_root_policy_key;
ALTER TABLE spectra_security.sec_root_policy
    ADD CONSTRAINT pk_sec_root_policy PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_root_policy_key UNIQUE (policy_key);

ALTER TABLE spectra_security.sec_security_audit_retention_policy
    DROP CONSTRAINT IF EXISTS pk_sec_security_audit_retention_policy;
ALTER TABLE spectra_security.sec_security_audit_retention_policy
    DROP CONSTRAINT IF EXISTS uk_sec_security_audit_retention_policy_key;
ALTER TABLE spectra_security.sec_security_audit_retention_policy
    ADD CONSTRAINT pk_sec_security_audit_retention_policy PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_security_audit_retention_policy_key UNIQUE (policy_key);

ALTER TABLE spectra_security.sec_security_audit_archive_manifest
    DROP CONSTRAINT IF EXISTS pk_sec_security_audit_archive_manifest;
ALTER TABLE spectra_security.sec_security_audit_archive_manifest
    DROP CONSTRAINT IF EXISTS uk_sec_security_audit_archive_manifest_id;
ALTER TABLE spectra_security.sec_security_audit_archive_manifest
    ADD CONSTRAINT pk_sec_security_audit_archive_manifest PRIMARY KEY (id),
    ADD CONSTRAINT uk_sec_security_audit_archive_manifest_id UNIQUE (manifest_id);

-- The audit event remains partitioned and append-only. PostgreSQL requires the
-- partition key in a parent primary/unique key, so event_id/occurred_at stays
-- as the immutable business key; id is the common technical identifier.
CREATE UNIQUE INDEX IF NOT EXISTS uk_sec_security_audit_event_id
    ON spectra_security.sec_security_audit_event (id, occurred_at);

COMMENT ON COLUMN spectra_security.sec_security_audit_event.id IS '技术主键；分区表唯一约束必须包含 occurred_at';
COMMENT ON COLUMN spectra_security.sec_security_audit_event.deleted IS '审计事实不可软删除，固定保持 NULL';
