-- Development baseline migration.
--
-- V20 completed the security data contract, but PostgreSQL ALTER TABLE ADD
-- COLUMN appends common fields to the physical end of an existing table.  The
-- project contract is stricter: id -> business fields -> audit fields.  There
-- is no production compatibility requirement during the current 1.0.0
-- development phase, so rebuild every security table in that order while
-- preserving rows, constraints, indexes and comments.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TEMP TABLE _security_rebuild_tables (
    table_name VARCHAR(63) PRIMARY KEY
) ON COMMIT DROP;

INSERT INTO _security_rebuild_tables (table_name)
SELECT class.relname
FROM pg_class class
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
WHERE namespace.nspname = 'spectra_security'
  AND class.relkind IN ('r', 'p')
  AND class.relname <> 'sec_security_audit_event_default';

CREATE TEMP TABLE _security_rebuild_columns (
    table_name VARCHAR(63) PRIMARY KEY,
    column_list TEXT NOT NULL,
    column_definitions TEXT NOT NULL
) ON COMMIT DROP;

INSERT INTO _security_rebuild_columns (table_name, column_list, column_definitions)
SELECT ordered.table_name,
       string_agg(format('%I', ordered.column_name), ', '
                  ORDER BY ordered.column_rank, ordered.ordinal_position),
       string_agg(
               format('%I %s%s%s', ordered.column_name, ordered.data_type,
                      COALESCE(' DEFAULT ' || ordered.default_expression, ''),
                      CASE WHEN ordered.not_null THEN ' NOT NULL' ELSE '' END),
               ', ' ORDER BY ordered.column_rank, ordered.ordinal_position)
FROM (
    SELECT class.relname AS table_name,
           attribute.attname AS column_name,
           attribute.attnum AS ordinal_position,
           format_type(attribute.atttypid, attribute.atttypmod) AS data_type,
           attribute.attnotnull AS not_null,
           pg_get_expr(default_value.adbin, default_value.adrelid) AS default_expression,
           CASE attribute.attname
               WHEN 'id' THEN 0
               WHEN 'created_by' THEN 1000
               WHEN 'created_at' THEN 1001
               WHEN 'updated_by' THEN 1002
               WHEN 'updated_at' THEN 1003
               WHEN 'deleted' THEN 1004
               WHEN 'version' THEN 1005
               ELSE 500
           END AS column_rank
    FROM _security_rebuild_tables rebuild
    JOIN pg_class class ON class.relname = rebuild.table_name
    JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
    JOIN pg_attribute attribute ON attribute.attrelid = class.oid
    LEFT JOIN pg_attrdef default_value
        ON default_value.adrelid = attribute.attrelid
       AND default_value.adnum = attribute.attnum
    WHERE namespace.nspname = 'spectra_security'
      AND attribute.attnum > 0
      AND NOT attribute.attisdropped
) ordered
GROUP BY ordered.table_name;

CREATE TEMP TABLE _security_rebuild_constraints AS
SELECT class.relname AS table_name,
       constraint_def.conname,
       constraint_def.contype,
       pg_get_constraintdef(constraint_def.oid, false) AS definition
FROM _security_rebuild_tables rebuild
JOIN pg_class class ON class.relname = rebuild.table_name
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
JOIN pg_constraint constraint_def ON constraint_def.conrelid = class.oid
WHERE namespace.nspname = 'spectra_security'
  AND constraint_def.contype IN ('p', 'u', 'c', 'f');

CREATE TEMP TABLE _security_rebuild_indexes AS
SELECT class.relname AS table_name,
       index_class.relname AS index_name,
       pg_get_indexdef(index_class.oid) AS definition
FROM _security_rebuild_tables rebuild
JOIN pg_class class ON class.relname = rebuild.table_name
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
JOIN pg_index index_def ON index_def.indrelid = class.oid
JOIN pg_class index_class ON index_class.oid = index_def.indexrelid
WHERE namespace.nspname = 'spectra_security'
  AND NOT EXISTS (
      SELECT 1
      FROM pg_constraint constraint_def
      WHERE constraint_def.conindid = index_def.indexrelid
  );

CREATE TEMP TABLE _security_rebuild_comments AS
SELECT class.relname AS table_name,
       NULL::VARCHAR(63) AS column_name,
       description.description
FROM _security_rebuild_tables rebuild
JOIN pg_class class ON class.relname = rebuild.table_name
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
JOIN pg_description description
    ON description.classoid = 'pg_class'::regclass
   AND description.objoid = class.oid
   AND description.objsubid = 0
WHERE namespace.nspname = 'spectra_security'
UNION ALL
SELECT class.relname AS table_name,
       attribute.attname AS column_name,
       description.description
FROM _security_rebuild_tables rebuild
JOIN pg_class class ON class.relname = rebuild.table_name
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
JOIN pg_attribute attribute ON attribute.attrelid = class.oid
JOIN pg_description description
    ON description.classoid = 'pg_class'::regclass
   AND description.objoid = class.oid
   AND description.objsubid = attribute.attnum
WHERE namespace.nspname = 'spectra_security'
  AND attribute.attnum > 0
  AND NOT attribute.attisdropped;

DO $$
DECLARE
    table_record RECORD;
    columns_record RECORD;
    new_table_name TEXT;
BEGIN
    FOR table_record IN SELECT table_name FROM _security_rebuild_tables ORDER BY table_name LOOP
        SELECT column_list, column_definitions
        INTO columns_record
        FROM _security_rebuild_columns
        WHERE table_name = table_record.table_name;

        new_table_name := '__canonical_' || table_record.table_name;
        IF table_record.table_name = 'sec_security_audit_event' THEN
            EXECUTE format(
                    'CREATE TABLE spectra_security.%I (%s) PARTITION BY RANGE (occurred_at)',
                    new_table_name, columns_record.column_definitions);
            EXECUTE format(
                    'CREATE TABLE spectra_security.%I PARTITION OF spectra_security.%I DEFAULT',
                    new_table_name || '_default', new_table_name);
        ELSE
            EXECUTE format(
                    'CREATE TABLE spectra_security.%I (%s)',
                    new_table_name, columns_record.column_definitions);
        END IF;

        EXECUTE format(
                'INSERT INTO spectra_security.%I (%s) SELECT %s FROM spectra_security.%I',
                new_table_name, columns_record.column_list, columns_record.column_list,
                table_record.table_name);
    END LOOP;
END
$$;

DO $$
DECLARE
    drop_sql TEXT;
BEGIN
    SELECT 'DROP TABLE ' || string_agg(format('spectra_security.%I', table_name), ', ')
           || ' CASCADE'
    INTO drop_sql
    FROM _security_rebuild_tables;
    EXECUTE drop_sql;
END
$$;

DO $$
DECLARE
    table_record RECORD;
BEGIN
    FOR table_record IN SELECT table_name FROM _security_rebuild_tables ORDER BY table_name LOOP
        EXECUTE format(
                'ALTER TABLE spectra_security.%I RENAME TO %I',
                '__canonical_' || table_record.table_name, table_record.table_name);
        IF table_record.table_name = 'sec_security_audit_event' THEN
            EXECUTE format(
                    'ALTER TABLE spectra_security.%I RENAME TO %I',
                    '__canonical_' || table_record.table_name || '_default',
                    'sec_security_audit_event_default');
        END IF;
    END LOOP;
END
$$;

DO $$
DECLARE
    constraint_record RECORD;
BEGIN
    FOR constraint_record IN
        SELECT *
        FROM _security_rebuild_constraints
        ORDER BY CASE contype WHEN 'p' THEN 1 WHEN 'u' THEN 2 WHEN 'c' THEN 3 WHEN 'f' THEN 4 ELSE 5 END,
                 table_name,
                 conname
    LOOP
        EXECUTE format(
                'ALTER TABLE spectra_security.%I ADD CONSTRAINT %I %s',
                constraint_record.table_name, constraint_record.conname,
                constraint_record.definition);
    END LOOP;
END
$$;

DO $$
DECLARE
    index_record RECORD;
BEGIN
    FOR index_record IN SELECT * FROM _security_rebuild_indexes ORDER BY table_name, index_name LOOP
        EXECUTE index_record.definition;
    END LOOP;
END
$$;

DO $$
DECLARE
    comment_record RECORD;
BEGIN
    FOR comment_record IN SELECT * FROM _security_rebuild_comments LOOP
        IF comment_record.column_name IS NULL THEN
            EXECUTE format(
                    'COMMENT ON TABLE spectra_security.%I IS %L',
                    comment_record.table_name, comment_record.description);
        ELSE
            EXECUTE format(
                    'COMMENT ON COLUMN spectra_security.%I.%I IS %L',
                    comment_record.table_name, comment_record.column_name,
                    comment_record.description);
        END IF;
    END LOOP;
END
$$;

-- The old table trigger is removed with the partitioned parent. Recreate the
-- immutable audit guard after the parent has been rebuilt.
CREATE OR REPLACE FUNCTION spectra_security.sec_reject_audit_mutation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'security_audit_event is append-only';
END;
$$;

CREATE TRIGGER trg_sec_security_audit_event_immutable
    BEFORE UPDATE OR DELETE ON spectra_security.sec_security_audit_event
    FOR EACH ROW EXECUTE FUNCTION spectra_security.sec_reject_audit_mutation();

REVOKE UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON spectra_security.sec_security_audit_event FROM PUBLIC;
GRANT SELECT, INSERT ON spectra_security.sec_security_audit_event TO spectra_runtime;
REVOKE UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON spectra_security.sec_security_audit_event_default FROM PUBLIC;
GRANT SELECT, INSERT ON spectra_security.sec_security_audit_event_default TO spectra_runtime;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA spectra_security TO spectra_runtime;
REVOKE UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON spectra_security.sec_security_audit_event FROM spectra_runtime;
REVOKE UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON spectra_security.sec_security_audit_event_default FROM spectra_runtime;
GRANT SELECT, INSERT ON spectra_security.sec_security_audit_event TO spectra_runtime;
GRANT SELECT, INSERT ON spectra_security.sec_security_audit_event_default TO spectra_runtime;

-- The system state is the only Core singleton introduced by initialization.
CREATE TABLE spectra_core.sys_system_state (
    id                UUID NOT NULL DEFAULT uuidv7(),
    state_key         VARCHAR(32) NOT NULL,
    state             VARCHAR(32) NOT NULL DEFAULT 'UNINITIALIZED',
    initialization_id UUID,
    initialized_at    TIMESTAMP(6) WITH TIME ZONE,
    initialized_by    UUID,
    created_by        UUID,
    created_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by        UUID,
    updated_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           TIMESTAMP(6) WITH TIME ZONE,
    version           BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_system_state PRIMARY KEY (id),
    CONSTRAINT uk_sys_system_state_key UNIQUE (state_key),
    CONSTRAINT ck_sys_system_state_key CHECK (state_key = 'SYSTEM'),
    CONSTRAINT ck_sys_system_state_value CHECK (state IN ('UNINITIALIZED', 'INITIALIZING', 'INITIALIZED'))
);

COMMENT ON TABLE spectra_core.sys_system_state IS '系统初始化单例状态表';
COMMENT ON COLUMN spectra_core.sys_system_state.id IS '技术主键';
COMMENT ON COLUMN spectra_core.sys_system_state.state_key IS '单例键，固定为 SYSTEM';
COMMENT ON COLUMN spectra_core.sys_system_state.state IS '初始化状态';
COMMENT ON COLUMN spectra_core.sys_system_state.initialization_id IS '当前初始化挑战 ID';
COMMENT ON COLUMN spectra_core.sys_system_state.initialized_at IS '初始化完成时间';
COMMENT ON COLUMN spectra_core.sys_system_state.initialized_by IS '初始化创建的 DEV_OPS 用户 ID';

INSERT INTO spectra_core.sys_system_state (
    id, state_key, state, version
)
VALUES (
    md5('seed:system-state:SYSTEM')::uuid, 'SYSTEM', 'UNINITIALIZED', 0
)
ON CONFLICT (state_key) DO NOTHING;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA spectra_core TO spectra_runtime;
