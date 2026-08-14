-- Phase 9 legacy Account model removal.
--
-- Authentication identities and password credentials now live in the target
-- spectra_security schema. This migration is intentionally idempotent and
-- does not infer or rewrite historical account rows.

DROP TABLE IF EXISTS spectra_core.sys_account;
