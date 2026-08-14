-- Phase 9 legacy authorization runtime removal.
--
-- The target V1 schema never creates these compatibility tables. This
-- idempotent migration removes them from installations that still carry the
-- pre-cutover authorization schema after all runtime reads and writes have
-- moved to spectra_security.

DROP TABLE IF EXISTS spectra_core.sys_rel_role_authority;
DROP TABLE IF EXISTS spectra_core.sys_rel_role_menu;
DROP TABLE IF EXISTS spectra_core.sys_rel_user_role;
DROP TABLE IF EXISTS spectra_core.sys_authority;
DROP TABLE IF EXISTS spectra_core.sys_role;
