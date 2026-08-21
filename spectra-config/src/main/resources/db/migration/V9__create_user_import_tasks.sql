-- 用户批量导入采用 Preview/Apply 两阶段；原始行只在任务有效期内暂存。

CREATE TABLE spectra_core.sys_user_import_task (
    id                   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    operator_id          UUID NOT NULL REFERENCES spectra_core.sys_user (id),
    idempotency_key      VARCHAR(120) NOT NULL,
    file_name            VARCHAR(255) NOT NULL,
    file_hash            VARCHAR(128) NOT NULL,
    skip_existing        BOOLEAN NOT NULL DEFAULT FALSE,
    status               VARCHAR(20) NOT NULL,
    request_hash         VARCHAR(64) NOT NULL,
    profile_version_hash VARCHAR(64) NOT NULL,
    preview_token_hash   VARCHAR(64),
    preview_expires_at   TIMESTAMP(6) WITH TIME ZONE,
    expires_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    total_rows           INTEGER NOT NULL DEFAULT 0,
    valid_rows           INTEGER NOT NULL DEFAULT 0,
    error_rows           INTEGER NOT NULL DEFAULT 0,
    skipped_rows         INTEGER NOT NULL DEFAULT 0,
    applied_rows         INTEGER NOT NULL DEFAULT 0,
    assignment_count     INTEGER NOT NULL DEFAULT 0,
    access_boundary_count INTEGER NOT NULL DEFAULT 0,
    grant_boundary_count INTEGER NOT NULL DEFAULT 0,
    preview_consumed_at  TIMESTAMP(6) WITH TIME ZONE,
    created_by           UUID,
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           UUID,
    updated_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted              TIMESTAMP(6) WITH TIME ZONE,
    version              BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_sys_user_import_task_status CHECK (status IN (
        'UPLOADED', 'VALIDATING', 'PREVIEWED', 'APPLYING',
        'SUCCEEDED', 'PARTIAL_FAILED', 'FAILED', 'EXPIRED'
    )),
    CONSTRAINT ck_sys_user_import_task_counts CHECK (
        total_rows >= 0 AND valid_rows >= 0 AND error_rows >= 0
        AND skipped_rows >= 0 AND applied_rows >= 0
        AND assignment_count >= 0 AND access_boundary_count >= 0 AND grant_boundary_count >= 0
    )
);

CREATE UNIQUE INDEX uk_sys_user_import_task_idempotency
    ON spectra_core.sys_user_import_task (operator_id, idempotency_key)
    WHERE deleted IS NULL;

CREATE INDEX idx_sys_user_import_task_operator_created
    ON spectra_core.sys_user_import_task (operator_id, created_at DESC)
    WHERE deleted IS NULL;

CREATE TABLE spectra_core.sys_user_import_row (
    id               UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    task_id          UUID NOT NULL REFERENCES spectra_core.sys_user_import_task (id) ON DELETE CASCADE,
    row_number       INTEGER NOT NULL,
    row_key          VARCHAR(120) NOT NULL,
    raw_data         JSONB NOT NULL,
    normalized_data  JSONB NOT NULL,
    state            VARCHAR(16) NOT NULL,
    errors           JSONB,
    user_id          UUID REFERENCES spectra_core.sys_user (id),
    created_by       UUID,
    created_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       UUID,
    updated_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted          TIMESTAMP(6) WITH TIME ZONE,
    version          BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_sys_user_import_row_number CHECK (row_number > 0),
    CONSTRAINT ck_sys_user_import_row_state CHECK (state IN ('VALID', 'ERROR', 'APPLIED', 'SKIPPED'))
);

CREATE UNIQUE INDEX uk_sys_user_import_row_number
    ON spectra_core.sys_user_import_row (task_id, row_number)
    WHERE deleted IS NULL;

CREATE INDEX idx_sys_user_import_row_task_state
    ON spectra_core.sys_user_import_row (task_id, state, row_number)
    WHERE deleted IS NULL;

COMMENT ON TABLE spectra_core.sys_user_import_task IS '用户批量导入 Preview/Apply 任务';
COMMENT ON TABLE spectra_core.sys_user_import_row IS '用户批量导入暂存行及校验结果';
COMMENT ON COLUMN spectra_core.sys_user_import_task.file_hash IS '上传文件摘要，不保存文件内容';
COMMENT ON COLUMN spectra_core.sys_user_import_task.request_hash IS '规范化导入请求摘要，防止 Preview/Apply 参数漂移';
COMMENT ON COLUMN spectra_core.sys_user_import_task.profile_version_hash IS '导入引用授权方案版本摘要';
COMMENT ON COLUMN spectra_core.sys_user_import_row.raw_data IS '固定模板原始字段；接口错误响应不回传';
COMMENT ON COLUMN spectra_core.sys_user_import_row.normalized_data IS '校验后的结构化字段；接口错误响应不回传';
