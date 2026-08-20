-- Normalize immutable reference seed metadata.
--
-- The all-zero UUID is the technical seed actor. The fixed timestamp makes
-- reference data independent from migration execution time. User-owned rows
-- are excluded by requiring both audit actors to be NULL.
DO $$
DECLARE
    seed_actor CONSTANT UUID := '00000000-0000-0000-0000-000000000000';
    seed_time CONSTANT TIMESTAMP(6) WITH TIME ZONE := TIMESTAMPTZ '1996-10-15 00:00:00+08:00';
BEGIN
    -- Core reference data.
    UPDATE spectra_core.sys_dict_group
    SET created_by = seed_actor, created_at = seed_time, updated_by = seed_actor, updated_at = seed_time
    WHERE created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_core.sys_dict_item
    SET created_by = seed_actor, created_at = seed_time, updated_by = seed_actor, updated_at = seed_time
    WHERE created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_core.file_type
    SET created_by = seed_actor, created_at = seed_time, updated_by = seed_actor, updated_at = seed_time
    WHERE created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_core.sys_menu
    SET created_by = seed_actor, created_at = seed_time, updated_by = seed_actor, updated_at = seed_time
    WHERE created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_core.sys_region
    SET created_by = seed_actor, created_at = seed_time, updated_by = seed_actor, updated_at = seed_time
    WHERE created_by IS NULL AND updated_by IS NULL;

    -- Fixed Core singletons use the all-zero technical identifier.
    UPDATE spectra_core.sys_organization_version
    SET id = seed_actor,
        changed_at = seed_time,
        created_by = seed_actor,
        created_at = seed_time,
        updated_by = seed_actor,
        updated_at = seed_time
    WHERE singleton_key = 'SYSTEM';

    UPDATE spectra_core.sys_system_state
    SET id = seed_actor,
        created_by = seed_actor,
        created_at = seed_time,
        updated_by = seed_actor,
        updated_at = seed_time
    WHERE state_key = 'SYSTEM' AND state = 'UNINITIALIZED';

    -- Security reference data. Runtime identities, credentials, MFA records,
    -- assignments and boundaries are intentionally not included here.
    UPDATE spectra_security.sec_permission
    SET created_by = seed_actor, created_at = seed_time, updated_by = seed_actor, updated_at = seed_time
    WHERE created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_security.sec_role
    SET created_by = seed_actor, created_at = seed_time, updated_by = seed_actor, updated_at = seed_time
    WHERE created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_security.sec_security_client
    SET created_by = seed_actor, created_at = seed_time, updated_by = seed_actor, updated_at = seed_time
    WHERE created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_security.sec_session_policy
    SET created_by = seed_actor, created_at = seed_time, updated_by = seed_actor, updated_at = seed_time
    WHERE created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_security.sec_password_policy
    SET id = seed_actor,
        created_by = seed_actor,
        created_at = seed_time,
        updated_by = seed_actor,
        updated_at = seed_time
    WHERE policy_key = 'SYSTEM' AND created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_security.sec_root_policy
    SET id = seed_actor,
        created_by = seed_actor,
        created_at = seed_time,
        updated_by = seed_actor,
        updated_at = seed_time
    WHERE policy_key = 'SYSTEM' AND created_by IS NULL AND updated_by IS NULL;

    UPDATE spectra_security.sec_security_audit_retention_policy
    SET id = seed_actor,
        created_by = seed_actor,
        created_at = seed_time,
        updated_by = seed_actor,
        updated_at = seed_time
    WHERE policy_key = 'DEFAULT' AND created_by IS NULL AND updated_by IS NULL;
END
$$;
