-- Spectra file upload domain v7.
-- This is an intentional one-shot replacement. Old file rows are not migrated.

DROP TABLE IF EXISTS spectra_core.file_upload_chunk CASCADE;
DROP TABLE IF EXISTS spectra_core.file_upload_task CASCADE;
DROP TABLE IF EXISTS spectra_core.file_info CASCADE;
DROP TABLE IF EXISTS spectra_core.file_reference CASCADE;
DROP TABLE IF EXISTS spectra_core.file_asset CASCADE;
DROP TABLE IF EXISTS spectra_core.file_type CASCADE;

CREATE TABLE spectra_core.file_type (
    id uuid NOT NULL,
    code varchar(80) NOT NULL,
    display_name varchar(120) NOT NULL,
    allowed_extensions jsonb NOT NULL,
    allowed_content_types jsonb NOT NULL,
    magic_rules jsonb NOT NULL DEFAULT '[]'::jsonb,
    max_size bigint NOT NULL,
    preview_enabled boolean NOT NULL DEFAULT false,
    download_enabled boolean NOT NULL DEFAULT true,
    upload_enabled boolean NOT NULL DEFAULT true,
    dangerous boolean NOT NULL DEFAULT false,
    enabled boolean NOT NULL DEFAULT true,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted timestamp(6) with time zone,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT file_type_pkey PRIMARY KEY (id),
    CONSTRAINT file_type_code_check CHECK (code ~ '^[A-Z][A-Z0-9_-]{1,79}$'),
    CONSTRAINT file_type_max_size_check CHECK (max_size > 0)
);

CREATE UNIQUE INDEX uk_file_type_code ON spectra_core.file_type (code) WHERE deleted IS NULL;

CREATE TABLE spectra_core.file_asset (
    id uuid NOT NULL,
    file_type_id uuid NOT NULL,
    original_name varchar(255) NOT NULL,
    content_sha256 char(64) NOT NULL,
    size bigint NOT NULL,
    content_type varchar(128) NOT NULL,
    storage_provider varchar(20) NOT NULL,
    storage_container varchar(255) NOT NULL,
    storage_key varchar(1024) NOT NULL,
    status varchar(20) NOT NULL,
    completed_at timestamp(6) with time zone,
    orphaned_at timestamp(6) with time zone,
    cleanup_attempts integer NOT NULL DEFAULT 0,
    next_cleanup_at timestamp(6) with time zone,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted timestamp(6) with time zone,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT file_asset_pkey PRIMARY KEY (id),
    CONSTRAINT file_asset_file_type_fk FOREIGN KEY (file_type_id) REFERENCES spectra_core.file_type(id),
    CONSTRAINT file_asset_size_check CHECK (size >= 0),
    CONSTRAINT file_asset_sha256_check CHECK (content_sha256 ~ '^[0-9a-fA-F]{64}$'),
    CONSTRAINT file_asset_provider_check CHECK (storage_provider IN ('LOCAL', 'S3')),
    CONSTRAINT file_asset_status_check CHECK (status IN ('READY', 'DELETING', 'DELETED')),
    CONSTRAINT file_asset_cleanup_attempts_check CHECK (cleanup_attempts >= 0)
);

CREATE UNIQUE INDEX uk_file_asset_sha256_size ON spectra_core.file_asset (content_sha256, size)
    WHERE deleted IS NULL AND status = 'READY';
CREATE INDEX idx_file_asset_cleanup ON spectra_core.file_asset (next_cleanup_at, orphaned_at)
    WHERE deleted IS NULL AND status = 'READY';

CREATE TABLE spectra_core.file_upload_session (
    id uuid NOT NULL,
    owner_user_id uuid NOT NULL,
    original_name varchar(255) NOT NULL,
    declared_content_type varchar(128) NOT NULL,
    size bigint NOT NULL,
    content_sha256 char(64) NOT NULL,
    chunk_size bigint NOT NULL,
    total_parts integer NOT NULL,
    storage_provider varchar(20) NOT NULL,
    transport_mode varchar(30) NOT NULL,
    storage_container varchar(255) NOT NULL,
    staging_key varchar(1024) NOT NULL,
    provider_upload_id varchar(512),
    file_asset_id uuid,
    status varchar(20) NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    last_activity_at timestamp(6) with time zone NOT NULL,
    completed_at timestamp(6) with time zone,
    verify_started_at timestamp(6) with time zone,
    verify_finished_at timestamp(6) with time zone,
    verify_processed_bytes bigint NOT NULL DEFAULT 0,
    verify_total_bytes bigint NOT NULL DEFAULT 0,
    failure_code varchar(80),
    cleanup_attempts integer NOT NULL DEFAULT 0,
    next_cleanup_at timestamp(6) with time zone,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted timestamp(6) with time zone,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT file_upload_session_pkey PRIMARY KEY (id),
    CONSTRAINT file_upload_session_asset_fk FOREIGN KEY (file_asset_id) REFERENCES spectra_core.file_asset(id),
    CONSTRAINT file_upload_session_size_check CHECK (size >= 0),
    CONSTRAINT file_upload_session_sha256_check CHECK (content_sha256 ~ '^[0-9a-fA-F]{64}$'),
    CONSTRAINT file_upload_session_chunk_size_check CHECK (chunk_size BETWEEN 5242880 AND 67108864),
    CONSTRAINT file_upload_session_parts_check CHECK (total_parts BETWEEN 1 AND 10000),
    CONSTRAINT file_upload_session_provider_check CHECK (storage_provider IN ('LOCAL', 'S3')),
    CONSTRAINT file_upload_session_transport_check CHECK (transport_mode IN ('LOCAL_PROXY', 'PRESIGNED')),
    CONSTRAINT file_upload_session_status_check CHECK (status IN ('UPLOADING', 'VERIFYING', 'READY', 'FAILED', 'CANCELED', 'EXPIRED', 'CLEANED')),
    CONSTRAINT file_upload_session_progress_check CHECK (verify_processed_bytes >= 0 AND verify_total_bytes >= 0),
    CONSTRAINT file_upload_session_cleanup_attempts_check CHECK (cleanup_attempts >= 0)
);

CREATE INDEX idx_file_upload_session_owner_resume ON spectra_core.file_upload_session
    (owner_user_id, content_sha256, size, status, expires_at) WHERE deleted IS NULL;
CREATE INDEX idx_file_upload_session_cleanup ON spectra_core.file_upload_session
    (next_cleanup_at, expires_at, last_activity_at) WHERE deleted IS NULL;

CREATE TABLE spectra_core.file_upload_part (
    id uuid NOT NULL,
    upload_session_id uuid NOT NULL,
    part_number integer NOT NULL,
    expected_size bigint NOT NULL,
    expected_sha256 char(64),
    uploaded_size bigint,
    actual_sha256 char(64),
    provider_etag varchar(512),
    status varchar(20) NOT NULL,
    upload_attempt integer NOT NULL DEFAULT 0,
    uploaded_at timestamp(6) with time zone,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted timestamp(6) with time zone,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT file_upload_part_pkey PRIMARY KEY (id),
    CONSTRAINT file_upload_part_session_fk FOREIGN KEY (upload_session_id) REFERENCES spectra_core.file_upload_session(id) ON DELETE CASCADE,
    CONSTRAINT file_upload_part_unique UNIQUE (upload_session_id, part_number),
    CONSTRAINT file_upload_part_number_check CHECK (part_number BETWEEN 1 AND 10000),
    CONSTRAINT file_upload_part_expected_size_check CHECK (expected_size >= 0),
    CONSTRAINT file_upload_part_uploaded_size_check CHECK (uploaded_size IS NULL OR uploaded_size >= 0),
    CONSTRAINT file_upload_part_expected_sha256_check CHECK (expected_sha256 IS NULL OR expected_sha256 ~ '^[0-9a-fA-F]{64}$'),
    CONSTRAINT file_upload_part_actual_sha256_check CHECK (actual_sha256 IS NULL OR actual_sha256 ~ '^[0-9a-fA-F]{64}$'),
    CONSTRAINT file_upload_part_status_check CHECK (status IN ('PENDING', 'UPLOADED', 'CONFIRMED')),
    CONSTRAINT file_upload_part_attempt_check CHECK (upload_attempt >= 0)
);

CREATE INDEX idx_file_upload_part_session_status ON spectra_core.file_upload_part (upload_session_id, status, part_number)
    WHERE deleted IS NULL;

CREATE TABLE spectra_core.file_reference (
    id uuid NOT NULL,
    file_asset_id uuid NOT NULL,
    reference_type varchar(80) NOT NULL,
    reference_id uuid NOT NULL,
    purpose varchar(80) NOT NULL,
    display_name varchar(255),
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted timestamp(6) with time zone,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT file_reference_pkey PRIMARY KEY (id),
    CONSTRAINT file_reference_asset_fk FOREIGN KEY (file_asset_id) REFERENCES spectra_core.file_asset(id),
    CONSTRAINT file_reference_type_check CHECK (reference_type <> ''),
    CONSTRAINT file_reference_purpose_check CHECK (purpose <> '')
);

CREATE UNIQUE INDEX uk_file_reference_business ON spectra_core.file_reference
    (file_asset_id, reference_type, reference_id, purpose) WHERE deleted IS NULL;
CREATE INDEX idx_file_reference_asset ON spectra_core.file_reference (file_asset_id) WHERE deleted IS NULL;

COMMENT ON TABLE spectra_core.file_type IS '文件类型策略';
COMMENT ON TABLE spectra_core.file_asset IS '已完成文件资产';
COMMENT ON TABLE spectra_core.file_upload_session IS '时间受限文件上传会话';
COMMENT ON TABLE spectra_core.file_upload_part IS '文件上传分片';
COMMENT ON TABLE spectra_core.file_reference IS '文件业务引用';

COMMENT ON COLUMN spectra_core.file_asset.content_sha256 IS '服务端最终复核的 SHA-256 十六进制摘要';
COMMENT ON COLUMN spectra_core.file_upload_session.provider_upload_id IS '对象存储 Multipart Upload ID';
COMMENT ON COLUMN spectra_core.file_upload_session.last_activity_at IS '仅成功接收或确认分片时更新';
COMMENT ON COLUMN spectra_core.file_reference.file_asset_id IS 'READY 文件资产 ID';

ALTER TABLE spectra_oa.oa_application_attachment DROP CONSTRAINT IF EXISTS uk_oa_application_attachment;
ALTER TABLE spectra_oa.oa_application_attachment DROP CONSTRAINT IF EXISTS oa_application_attachment_file_asset_fk;
ALTER TABLE spectra_oa.oa_application_attachment DROP COLUMN IF EXISTS file_id;
ALTER TABLE spectra_oa.oa_application_attachment ADD COLUMN IF NOT EXISTS file_asset_id uuid;
ALTER TABLE spectra_oa.oa_application_attachment
    ADD CONSTRAINT oa_application_attachment_file_asset_fk FOREIGN KEY (file_asset_id) REFERENCES spectra_core.file_asset(id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_oa_application_attachment_asset
    ON spectra_oa.oa_application_attachment (application_id, file_asset_id) WHERE deleted IS NULL;
COMMENT ON COLUMN spectra_oa.oa_application_attachment.file_asset_id IS '文件资产 ID';

ALTER TABLE spectra_oa.oa_contract_version DROP COLUMN IF EXISTS file_id;
ALTER TABLE spectra_oa.oa_contract_version DROP CONSTRAINT IF EXISTS oa_contract_version_file_asset_fk;
ALTER TABLE spectra_oa.oa_contract_version ADD COLUMN IF NOT EXISTS file_asset_id uuid;
ALTER TABLE spectra_oa.oa_contract_version
    ADD CONSTRAINT oa_contract_version_file_asset_fk FOREIGN KEY (file_asset_id) REFERENCES spectra_core.file_asset(id);
CREATE INDEX IF NOT EXISTS idx_oa_contract_version_file_asset ON spectra_oa.oa_contract_version (file_asset_id)
    WHERE deleted IS NULL;
COMMENT ON COLUMN spectra_oa.oa_contract_version.file_asset_id IS '文件资产 ID';

ALTER TABLE spectra_oa.oa_document_version DROP COLUMN IF EXISTS file_id;
ALTER TABLE spectra_oa.oa_document_version DROP CONSTRAINT IF EXISTS oa_document_version_file_asset_fk;
ALTER TABLE spectra_oa.oa_document_version ADD COLUMN IF NOT EXISTS file_asset_id uuid;
ALTER TABLE spectra_oa.oa_document_version
    ADD CONSTRAINT oa_document_version_file_asset_fk FOREIGN KEY (file_asset_id) REFERENCES spectra_core.file_asset(id);
CREATE INDEX IF NOT EXISTS idx_oa_document_version_file_asset ON spectra_oa.oa_document_version (file_asset_id)
    WHERE deleted IS NULL;
COMMENT ON COLUMN spectra_oa.oa_document_version.file_asset_id IS '文件资产 ID';

INSERT INTO spectra_core.file_type
    (id, code, display_name, allowed_extensions, allowed_content_types, magic_rules, max_size,
     preview_enabled, download_enabled, upload_enabled, dangerous, enabled)
VALUES
    (gen_random_uuid(), 'JPEG', 'JPEG 图片', '[".jpg", ".jpeg"]'::jsonb, '["image/jpeg"]'::jsonb,
     '[{"bytes":"FFD8FFE0","offset":0},{"bytes":"FFD8FFE1","offset":0},{"bytes":"FFD8FFDB","offset":0}]'::jsonb,
     20971520, true, true, true, false, true),
    (gen_random_uuid(), 'PNG', 'PNG 图片', '[".png"]'::jsonb, '["image/png"]'::jsonb,
     '[{"bytes":"89504E470D0A1A0A","offset":0}]'::jsonb, 20971520, true, true, true, false, true),
    (gen_random_uuid(), 'GIF', 'GIF 图片', '[".gif"]'::jsonb, '["image/gif"]'::jsonb,
     '[{"bytes":"474946383761","offset":0},{"bytes":"474946383961","offset":0}]'::jsonb,
     10485760, true, true, true, false, true),
    (gen_random_uuid(), 'PDF', 'PDF 文档', '[".pdf"]'::jsonb, '["application/pdf"]'::jsonb,
     '[{"bytes":"25504446","offset":0}]'::jsonb, 104857600, true, true, true, false, true),
    (gen_random_uuid(), 'ZIP', 'ZIP 压缩包', '[".zip"]'::jsonb, '["application/zip", "application/x-zip-compressed"]'::jsonb,
     '[{"bytes":"504B0304","offset":0}]'::jsonb, 209715200, false, true, true, false, true),
    (gen_random_uuid(), 'DOCX', 'Word 文档', '[".docx"]'::jsonb,
     '["application/vnd.openxmlformats-officedocument.wordprocessingml.document"]'::jsonb,
     '[{"bytes":"504B0304","offset":0}]'::jsonb, 104857600, true, true, true, false, true),
    (gen_random_uuid(), 'XLSX', 'Excel 文档', '[".xlsx"]'::jsonb,
     '["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"]'::jsonb,
     '[{"bytes":"504B0304","offset":0}]'::jsonb, 104857600, true, true, true, false, true),
    (gen_random_uuid(), 'HTML', 'HTML 文档', '[".html", ".htm"]'::jsonb, '["text/html"]'::jsonb,
     '[]'::jsonb, 10485760, false, true, false, true, true)
;
