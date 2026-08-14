-- Security runtime database privileges.
--
-- V1 owns the target objects. This migration deliberately keeps the application
-- role separate from the migration/owner role: the owner can perform DDL, while
-- the application role cannot mutate or delete the append-only Security Audit.
-- Deployment must grant the login role membership in spectra_runtime and must not
-- run the application with the migration owner.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'spectra_runtime') THEN
        CREATE ROLE spectra_runtime
            NOLOGIN
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            NOINHERIT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'spectra_migrator') THEN
        CREATE ROLE spectra_migrator
            NOLOGIN
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            INHERIT;
    END IF;
END
$$;

REVOKE CREATE ON SCHEMA spectra_core, spectra_security, spectra_oa, spectra_ai,
    spectra_workflow, spectra_notification FROM PUBLIC;

GRANT USAGE ON SCHEMA spectra_core, spectra_security, spectra_oa, spectra_ai,
    spectra_workflow, spectra_notification TO spectra_runtime;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA spectra_core TO spectra_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA spectra_security TO spectra_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA spectra_oa TO spectra_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA spectra_ai TO spectra_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA spectra_workflow TO spectra_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA spectra_notification TO spectra_runtime;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA spectra_core TO spectra_runtime;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA spectra_security TO spectra_runtime;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA spectra_oa TO spectra_runtime;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA spectra_ai TO spectra_runtime;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA spectra_workflow TO spectra_runtime;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA spectra_notification TO spectra_runtime;

-- The broad table grant above is intentionally narrowed for append-only Audit.
REVOKE UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON spectra_security.security_audit_event FROM spectra_runtime;
GRANT SELECT, INSERT ON spectra_security.security_audit_event TO spectra_runtime;
REVOKE UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON spectra_security.security_audit_event_default FROM spectra_runtime;
GRANT SELECT, INSERT ON spectra_security.security_audit_event_default TO spectra_runtime;

-- New tables created by the migration owner retain the same runtime boundary.
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_core
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO spectra_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_oa
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO spectra_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_ai
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO spectra_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_workflow
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO spectra_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_notification
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO spectra_runtime;

ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_core
    GRANT USAGE, SELECT ON SEQUENCES TO spectra_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_security
    GRANT USAGE, SELECT ON SEQUENCES TO spectra_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_oa
    GRANT USAGE, SELECT ON SEQUENCES TO spectra_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_ai
    GRANT USAGE, SELECT ON SEQUENCES TO spectra_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_workflow
    GRANT USAGE, SELECT ON SEQUENCES TO spectra_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA spectra_notification
    GRANT USAGE, SELECT ON SEQUENCES TO spectra_runtime;
