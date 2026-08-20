-- Development baseline migration.
--
-- Core migrations V1/V15/V16/V17 appended later fields to existing tables.  The
-- development contract has no historical-schema compatibility requirement, so
-- rebuild the project-owned Core tables with the same canonical order as the
-- security tables.  Relationship and singleton tables receive a UUID technical
-- primary key as well; their natural keys remain unique business constraints.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Complete the common entity contract for the Core relationship/singleton
-- tables before taking the metadata snapshot used by the rebuild below.
ALTER TABLE spectra_core.sys_user_department_membership
    ADD COLUMN IF NOT EXISTS id UUID DEFAULT uuidv7(),
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

ALTER TABLE spectra_core.sys_user_department_membership
    ALTER COLUMN id SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN version SET NOT NULL;

ALTER TABLE spectra_core.sys_department_closure
    ADD COLUMN IF NOT EXISTS id UUID DEFAULT uuidv7(),
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

ALTER TABLE spectra_core.sys_department_closure
    ALTER COLUMN id SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN version SET NOT NULL;

ALTER TABLE spectra_core.sys_organization_version
    ADD COLUMN IF NOT EXISTS id UUID DEFAULT uuidv7(),
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted TIMESTAMP(6) WITH TIME ZONE;

ALTER TABLE spectra_core.sys_organization_version
    ALTER COLUMN id SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN version SET NOT NULL;

ALTER TABLE spectra_core.sys_user_department_membership
    DROP CONSTRAINT IF EXISTS sys_user_department_membership_pkey,
    ADD CONSTRAINT pk_sys_user_department_membership PRIMARY KEY (id),
    ADD CONSTRAINT uk_sys_user_department_membership_pair UNIQUE (user_id, department_id);

ALTER TABLE spectra_core.sys_department_closure
    DROP CONSTRAINT IF EXISTS sys_department_closure_pkey,
    ADD CONSTRAINT pk_sys_department_closure PRIMARY KEY (id),
    ADD CONSTRAINT uk_sys_department_closure_pair UNIQUE (ancestor_id, descendant_id);

ALTER TABLE spectra_core.sys_organization_version
    DROP CONSTRAINT IF EXISTS sys_organization_version_pkey,
    ADD CONSTRAINT pk_sys_organization_version PRIMARY KEY (id),
    ADD CONSTRAINT uk_sys_organization_version_key UNIQUE (singleton_key);

CREATE TEMP TABLE _core_rebuild_tables (
    table_name VARCHAR(63) PRIMARY KEY
) ON COMMIT DROP;

INSERT INTO _core_rebuild_tables (table_name)
SELECT class.relname
FROM pg_class class
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
WHERE namespace.nspname = 'spectra_core'
  AND class.relkind IN ('r', 'p');

CREATE TEMP TABLE _core_rebuild_columns (
    table_name VARCHAR(63) PRIMARY KEY,
    column_list TEXT NOT NULL,
    column_definitions TEXT NOT NULL
) ON COMMIT DROP;

INSERT INTO _core_rebuild_columns (table_name, column_list, column_definitions)
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
    FROM _core_rebuild_tables rebuild
    JOIN pg_class class ON class.relname = rebuild.table_name
    JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
    JOIN pg_attribute attribute ON attribute.attrelid = class.oid
    LEFT JOIN pg_attrdef default_value
        ON default_value.adrelid = attribute.attrelid
       AND default_value.adnum = attribute.attnum
    WHERE namespace.nspname = 'spectra_core'
      AND attribute.attnum > 0
      AND NOT attribute.attisdropped
) ordered
GROUP BY ordered.table_name;

-- Capture Core constraints and all foreign keys from other project schemas
-- that point into Core.  Dropping Core tables with CASCADE would otherwise
-- remove those cross-schema guards permanently.
CREATE TEMP TABLE _core_rebuild_constraints AS
SELECT namespace.nspname AS table_schema,
       class.relname AS table_name,
       constraint_def.conname,
       constraint_def.contype,
       pg_get_constraintdef(constraint_def.oid, false) AS definition
FROM pg_constraint constraint_def
JOIN pg_class class ON class.oid = constraint_def.conrelid
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
LEFT JOIN pg_class referenced_class ON referenced_class.oid = constraint_def.confrelid
LEFT JOIN pg_namespace referenced_namespace ON referenced_namespace.oid = referenced_class.relnamespace
WHERE (namespace.nspname = 'spectra_core' OR referenced_namespace.nspname = 'spectra_core')
  AND constraint_def.contype IN ('p', 'u', 'c', 'f');

CREATE TEMP TABLE _core_rebuild_indexes AS
SELECT class.relname AS table_name,
       index_class.relname AS index_name,
       pg_get_indexdef(index_class.oid) AS definition
FROM _core_rebuild_tables rebuild
JOIN pg_class class ON class.relname = rebuild.table_name
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
JOIN pg_index index_def ON index_def.indrelid = class.oid
JOIN pg_class index_class ON index_class.oid = index_def.indexrelid
WHERE namespace.nspname = 'spectra_core'
  AND NOT EXISTS (
      SELECT 1
      FROM pg_constraint constraint_def
      WHERE constraint_def.conindid = index_def.indexrelid
  );

CREATE TEMP TABLE _core_rebuild_comments AS
SELECT class.relname AS table_name,
       NULL::VARCHAR(63) AS column_name,
       description.description
FROM _core_rebuild_tables rebuild
JOIN pg_class class ON class.relname = rebuild.table_name
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
JOIN pg_description description
    ON description.classoid = 'pg_class'::regclass
   AND description.objoid = class.oid
   AND description.objsubid = 0
WHERE namespace.nspname = 'spectra_core'
UNION ALL
SELECT class.relname AS table_name,
       attribute.attname AS column_name,
       description.description
FROM _core_rebuild_tables rebuild
JOIN pg_class class ON class.relname = rebuild.table_name
JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
JOIN pg_attribute attribute ON attribute.attrelid = class.oid
JOIN pg_description description
    ON description.classoid = 'pg_class'::regclass
   AND description.objoid = class.oid
   AND description.objsubid = attribute.attnum
WHERE namespace.nspname = 'spectra_core'
  AND attribute.attnum > 0
  AND NOT attribute.attisdropped;

DO $$
DECLARE
    table_record RECORD;
    columns_record RECORD;
    new_table_name TEXT;
BEGIN
    FOR table_record IN SELECT table_name FROM _core_rebuild_tables ORDER BY table_name LOOP
        SELECT column_list, column_definitions
        INTO columns_record
        FROM _core_rebuild_columns
        WHERE table_name = table_record.table_name;

        new_table_name := '__canonical_' || table_record.table_name;
        EXECUTE format(
                'CREATE TABLE spectra_core.%I (%s)',
                new_table_name, columns_record.column_definitions);
        EXECUTE format(
                'INSERT INTO spectra_core.%I (%s) SELECT %s FROM spectra_core.%I',
                new_table_name, columns_record.column_list, columns_record.column_list,
                table_record.table_name);
    END LOOP;
END
$$;

DO $$
DECLARE
    drop_sql TEXT;
BEGIN
    SELECT 'DROP TABLE ' || string_agg(format('spectra_core.%I', table_name), ', ')
           || ' CASCADE'
    INTO drop_sql
    FROM _core_rebuild_tables;
    EXECUTE drop_sql;
END
$$;

DO $$
DECLARE
    table_record RECORD;
BEGIN
    FOR table_record IN SELECT table_name FROM _core_rebuild_tables ORDER BY table_name LOOP
        EXECUTE format(
                'ALTER TABLE spectra_core.%I RENAME TO %I',
                '__canonical_' || table_record.table_name, table_record.table_name);
    END LOOP;
END
$$;

DO $$
DECLARE
    constraint_record RECORD;
BEGIN
    FOR constraint_record IN
        SELECT *
        FROM _core_rebuild_constraints
        ORDER BY CASE contype WHEN 'p' THEN 1 WHEN 'u' THEN 2 WHEN 'c' THEN 3 WHEN 'f' THEN 4 ELSE 5 END,
                 table_schema, table_name, conname
    LOOP
        EXECUTE format(
                'ALTER TABLE %I.%I ADD CONSTRAINT %I %s',
                constraint_record.table_schema, constraint_record.table_name,
                constraint_record.conname, constraint_record.definition);
    END LOOP;
END
$$;

DO $$
DECLARE
    index_record RECORD;
BEGIN
    FOR index_record IN SELECT * FROM _core_rebuild_indexes ORDER BY table_name, index_name LOOP
        EXECUTE index_record.definition;
    END LOOP;
END
$$;

DO $$
DECLARE
    comment_record RECORD;
BEGIN
    FOR comment_record IN SELECT * FROM _core_rebuild_comments LOOP
        IF comment_record.column_name IS NULL THEN
            EXECUTE format(
                    'COMMENT ON TABLE spectra_core.%I IS %L',
                    comment_record.table_name, comment_record.description);
        ELSE
            EXECUTE format(
                    'COMMENT ON COLUMN spectra_core.%I.%I IS %L',
                    comment_record.table_name, comment_record.column_name,
                    comment_record.description);
        END IF;
    END LOOP;
END
$$;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA spectra_core TO spectra_runtime;
