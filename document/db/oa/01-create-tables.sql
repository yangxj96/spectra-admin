-- ============================================
-- OA 模块 — 建表语句汇总（spectra_core schema）
-- 生成时间: 2026-07-11
-- 数据范围字段: department_id (UUID)
-- ============================================

-- 1. oa_asset（资产）
CREATE TABLE spectra_core.oa_asset (
    id          UUID PRIMARY KEY,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- 2. oa_attendance（考勤）
CREATE TABLE spectra_core.oa_attendance (
    id          UUID PRIMARY KEY,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- 3. oa_calendar（日历）
CREATE TABLE spectra_core.oa_calendar (
    id          UUID PRIMARY KEY,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- 4. oa_contact（通讯录）
CREATE TABLE spectra_core.oa_contact (
    id          UUID PRIMARY KEY,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- 5. oa_contract（合同）
CREATE TABLE spectra_core.oa_contract (
    id          UUID PRIMARY KEY,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- 6. oa_document（文档）
CREATE TABLE spectra_core.oa_document (
    id          UUID PRIMARY KEY,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- 7. oa_meeting（会议）
CREATE TABLE spectra_core.oa_meeting (
    id                  UUID PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    initiator_id        UUID NOT NULL,
    start_time          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    end_time            TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    location            VARCHAR(255),
    content             TEXT,
    status              VARCHAR(32) NOT NULL DEFAULT 'draft',
    process_instance_id VARCHAR(64),
    approval_status     VARCHAR(32) NOT NULL DEFAULT 'draft',
    created_by          UUID,
    created_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by          UUID,
    updated_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted             TIMESTAMP(6) WITH TIME ZONE,
    version             BIGINT DEFAULT 0,
    department_id       UUID
);

-- 8. oa_meeting_participant（会议参会人）
CREATE TABLE spectra_core.oa_meeting_participant (
    id          UUID PRIMARY KEY,
    meeting_id  UUID NOT NULL,
    user_id     UUID NOT NULL,
    role        VARCHAR(32) DEFAULT 'attendee',
    status      VARCHAR(32) DEFAULT 'pending',
    check_in_at TIMESTAMP(6) WITH TIME ZONE,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- 9. oa_meeting_record（会议纪要）
CREATE TABLE spectra_core.oa_meeting_record (
    id          UUID PRIMARY KEY,
    meeting_id  UUID NOT NULL,
    content     TEXT,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- 10. oa_notice（公告）
CREATE TABLE spectra_core.oa_notice (
    id          UUID PRIMARY KEY,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- 11. oa_report（报表）
CREATE TABLE spectra_core.oa_report (
    id          UUID PRIMARY KEY,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    department_id UUID
);

-- ============================================
-- ALTER TABLE — 为已有表添加 department_id（增量升级用）
-- ============================================

ALTER TABLE spectra_core.oa_asset        ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_attendance   ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_calendar     ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_contact      ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_contract     ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_document     ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_meeting      ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_meeting_participant ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_meeting_record      ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_notice       ADD COLUMN IF NOT EXISTS department_id UUID;
ALTER TABLE spectra_core.oa_report       ADD COLUMN IF NOT EXISTS department_id UUID;
