-- Phase 9 legacy DataScope removal.
--
-- The target V1 schema never creates these compatibility tables. This migration
-- removes them from installations that still carry the pre-cutover schema.
-- Historical user/role scope is not converted automatically: any required mapping
-- must be reviewed and written as an explicit Permission Boundary before this
-- migration is deployed.

DROP TABLE IF EXISTS spectra_core.sys_user_data_scope_target;
DROP TABLE IF EXISTS spectra_core.sys_user_data_scope;
DROP TABLE IF EXISTS spectra_core.sys_role_data_scope_target;
DROP TABLE IF EXISTS spectra_core.sys_role_data_scope;

ALTER TABLE IF EXISTS spectra_core.sys_role
    DROP COLUMN IF EXISTS scope;
