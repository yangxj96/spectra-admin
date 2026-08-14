-- Spectra target schema V1.
--
-- This migration is the complete new-environment schema contract. It deliberately
-- does not create the legacy sys_account/sys_role/sys_authority/data_scope tables,
-- and it does not enable Flyway baseline-on-migrate. Existing databases must be
-- migrated through an explicitly reviewed cutover procedure before this version
-- is applied.

CREATE SCHEMA IF NOT EXISTS spectra_core;

-- User identity is kept in the core domain; authentication identities and
-- credentials live in spectra_security. Organization membership is separate from
-- authorization boundaries.
CREATE TABLE spectra_core.sys_user (
    id                    UUID PRIMARY KEY,
    username              VARCHAR(100) NOT NULL,
    avatar                VARCHAR(255),
    status                VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    status_reason         VARCHAR(500),
    locked_until          TIMESTAMP(6) WITH TIME ZONE,
    departed_at           TIMESTAMP(6) WITH TIME ZONE,
    real_name             VARCHAR(50),
    gender                SMALLINT DEFAULT 0,
    birthday              TIMESTAMP(6) WITH TIME ZONE,
    phone                 VARCHAR(20),
    email                 VARCHAR(100),
    country               VARCHAR(50),
    city                  VARCHAR(50),
    language              VARCHAR(10) DEFAULT 'zh-CN',
    timezone              VARCHAR(40) DEFAULT 'Asia/Shanghai',
    primary_department_id UUID,
    security_version      BIGINT NOT NULL DEFAULT 0,
    created_by            UUID,
    created_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by            UUID,
    updated_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted               TIMESTAMP(6) WITH TIME ZONE,
    version               BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_sys_user_status CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED', 'DEPARTED')),
    CONSTRAINT ck_sys_user_security_version CHECK (security_version >= 0)
);

CREATE INDEX idx_sys_user_status ON spectra_core.sys_user (status) WHERE deleted IS NULL;
CREATE INDEX idx_sys_user_primary_department ON spectra_core.sys_user (primary_department_id) WHERE deleted IS NULL;

-- __V1_APPEND_1__
CREATE TABLE spectra_core.sys_department (
    id         UUID PRIMARY KEY,
    pid        UUID,
    name       VARCHAR(100) NOT NULL,
    code       VARCHAR(100) NOT NULL,
    type       VARCHAR(50),
    path       VARCHAR(255),
    remark     VARCHAR(255),
    created_by UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted    TIMESTAMP(6) WITH TIME ZONE,
    version    BIGINT DEFAULT 0,
    region_id  UUID,
    sort       INTEGER DEFAULT 0
);
COMMENT ON TABLE spectra_core.sys_department IS '部门表';
COMMENT ON COLUMN spectra_core.sys_department.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_department.pid IS '上级ID';
COMMENT ON COLUMN spectra_core.sys_department.name IS '名称';
COMMENT ON COLUMN spectra_core.sys_department.code IS '编码';
COMMENT ON COLUMN spectra_core.sys_department.type IS '公司类型';
COMMENT ON COLUMN spectra_core.sys_department.path IS '组织机构路径';
COMMENT ON COLUMN spectra_core.sys_department.remark IS '备注';
COMMENT ON COLUMN spectra_core.sys_department.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_department.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_department.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_department.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_department.deleted IS '是否删除';
COMMENT ON COLUMN spectra_core.sys_department.version IS '乐观锁';
COMMENT ON COLUMN spectra_core.sys_department.region_id IS '所属行政区划ID';
COMMENT ON COLUMN spectra_core.sys_department.sort IS '排序,默认0';

-- 菜单表
CREATE TABLE spectra_core.sys_menu (
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    pid         UUID,
    icon        VARCHAR(100),
    menu_type   VARCHAR(16) NOT NULL,
    route_name  VARCHAR(100),
    sort        INTEGER NOT NULL DEFAULT 0,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    CONSTRAINT ck_sys_menu_type
        CHECK (menu_type IN ('DIRECTORY', 'MENU')),
    CONSTRAINT ck_sys_menu_route_binding
        CHECK (deleted IS NOT NULL
            OR (menu_type = 'DIRECTORY' AND route_name IS NULL)
            OR (menu_type = 'MENU' AND route_name IS NOT NULL))
);
COMMENT ON TABLE spectra_core.sys_menu IS '菜单表';
COMMENT ON COLUMN spectra_core.sys_menu.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_menu.name IS '名称';
COMMENT ON COLUMN spectra_core.sys_menu.pid IS '父级ID';
-- __V1_APPEND_2__
COMMENT ON COLUMN spectra_core.sys_menu.icon IS '图标';
COMMENT ON COLUMN spectra_core.sys_menu.menu_type IS '菜单类型：DIRECTORY-目录，MENU-可点击菜单';
COMMENT ON COLUMN spectra_core.sys_menu.route_name IS '对应 Vue Router 的唯一命名路由';
COMMENT ON COLUMN spectra_core.sys_menu.sort IS '排序';
COMMENT ON COLUMN spectra_core.sys_menu.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_menu.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_menu.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_menu.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_menu.deleted IS '是否删除';
COMMENT ON COLUMN spectra_core.sys_menu.version IS '乐观锁';
CREATE UNIQUE INDEX uk_sys_menu_route_name_active
    ON spectra_core.sys_menu (route_name)
    WHERE deleted IS NULL AND route_name IS NOT NULL;

-- 区域表
CREATE TABLE spectra_core.sys_region (
    id         UUID PRIMARY KEY,
    pid        UUID,
    name       VARCHAR(100),
    full_name  VARCHAR(255),
    short_name VARCHAR(100),
    code       VARCHAR(100),
    path       VARCHAR(255),
    level      VARCHAR(50),
    status     BOOLEAN NOT NULL DEFAULT TRUE,
    sort       INTEGER,
    created_by UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted    TIMESTAMP(6) WITH TIME ZONE,
    version    BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_core.sys_region IS '行政区划表';
COMMENT ON COLUMN spectra_core.sys_region.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_region.pid IS '上级ID';
COMMENT ON COLUMN spectra_core.sys_region.name IS '区域名称';
COMMENT ON COLUMN spectra_core.sys_region.full_name IS '区域全称，如 北京市/北京市/东城区';
COMMENT ON COLUMN spectra_core.sys_region.short_name IS '简称';
COMMENT ON COLUMN spectra_core.sys_region.code IS '区域编码';
COMMENT ON COLUMN spectra_core.sys_region.path IS '区域路径，如 /110000/110100/110101';
COMMENT ON COLUMN spectra_core.sys_region.level IS '行政区划层级:1省 2地级市 3县级 4乡级 5村级';
COMMENT ON COLUMN spectra_core.sys_region.status IS '状态：true-启用 false-停用';
COMMENT ON COLUMN spectra_core.sys_region.sort IS '排序';
COMMENT ON COLUMN spectra_core.sys_region.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_region.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_region.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_region.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_region.deleted IS '删除标识';
COMMENT ON COLUMN spectra_core.sys_region.version IS '乐观锁';

-- 字典组
CREATE TABLE spectra_core.sys_dict_group (
    id         UUID PRIMARY KEY,
    name       VARCHAR(100),
    code       VARCHAR(100),
    created_by UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted    TIMESTAMP(6) WITH TIME ZONE,
    version    BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_core.sys_dict_group IS '字典组';
-- __V1_APPEND_3__
COMMENT ON COLUMN spectra_core.sys_dict_group.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_dict_group.name IS '字典组名称';
COMMENT ON COLUMN spectra_core.sys_dict_group.code IS '字典组编码';
COMMENT ON COLUMN spectra_core.sys_dict_group.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_dict_group.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_dict_group.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_dict_group.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_dict_group.deleted IS '是否删除';
COMMENT ON COLUMN spectra_core.sys_dict_group.version IS '乐观锁';

-- 字典项
CREATE TABLE spectra_core.sys_dict_item (
    id           UUID PRIMARY KEY,
    gid          UUID NOT NULL,
    label        VARCHAR(100) NOT NULL,
    value        VARCHAR(100) NOT NULL,
    sort         SMALLINT NOT NULL DEFAULT 0,
    state        SMALLINT NOT NULL,
    remark       VARCHAR(255),
    created_by   UUID,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by   UUID,
    updated_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted      TIMESTAMP(6) WITH TIME ZONE,
    version      BIGINT DEFAULT 0,
    default_flag BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE spectra_core.sys_dict_item IS '字典项';
COMMENT ON COLUMN spectra_core.sys_dict_item.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_dict_item.gid IS '字典组ID';
COMMENT ON COLUMN spectra_core.sys_dict_item.label IS '标签';
COMMENT ON COLUMN spectra_core.sys_dict_item.value IS '值';
COMMENT ON COLUMN spectra_core.sys_dict_item.sort IS '排序';
COMMENT ON COLUMN spectra_core.sys_dict_item.state IS '状态';
COMMENT ON COLUMN spectra_core.sys_dict_item.remark IS '备注';
COMMENT ON COLUMN spectra_core.sys_dict_item.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_dict_item.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_dict_item.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_dict_item.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_dict_item.deleted IS '是否删除';
COMMENT ON COLUMN spectra_core.sys_dict_item.version IS '乐观锁';
COMMENT ON COLUMN spectra_core.sys_dict_item.default_flag IS '是否默认';

-- 系统配置
CREATE TABLE spectra_core.sys_config (
    id         UUID PRIMARY KEY,
    key        VARCHAR(100) NOT NULL,
    value      TEXT NOT NULL,
    type       INTEGER NOT NULL,
    dict_code  VARCHAR(255),
    remarks    VARCHAR(255),
    created_by UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted    TIMESTAMP(6) WITH TIME ZONE,
    version    BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_core.sys_config IS '系统配置表';
COMMENT ON COLUMN spectra_core.sys_config.id IS '主键ID';
-- __V1_APPEND_4__
COMMENT ON COLUMN spectra_core.sys_config.key IS '配置key';
COMMENT ON COLUMN spectra_core.sys_config.value IS '配置VALUE';
COMMENT ON COLUMN spectra_core.sys_config.type IS '值类型';
COMMENT ON COLUMN spectra_core.sys_config.dict_code IS '字典组CODE';
COMMENT ON COLUMN spectra_core.sys_config.remarks IS '备注说明';
COMMENT ON COLUMN spectra_core.sys_config.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_config.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_config.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_config.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_config.deleted IS '删除时间';
COMMENT ON COLUMN spectra_core.sys_config.version IS '乐观锁版本号,默认0';

-- 操作日志
CREATE TABLE spectra_core.sys_log (
    id         UUID PRIMARY KEY,
    user_id    UUID,
    module     VARCHAR(100),
    action     VARCHAR(100),
    target     VARCHAR(255),
    ip         VARCHAR(50),
    user_agent VARCHAR(500),
    request_params TEXT,
    response_result TEXT,
    duration   BIGINT,
    created_by UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted    TIMESTAMP(6) WITH TIME ZONE,
    version    BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_core.sys_log IS '操作日志表';
COMMENT ON COLUMN spectra_core.sys_log.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_log.user_id IS '操作用户ID';
COMMENT ON COLUMN spectra_core.sys_log.module IS '操作模块';
COMMENT ON COLUMN spectra_core.sys_log.action IS '操作类型';
COMMENT ON COLUMN spectra_core.sys_log.target IS '操作对象';
COMMENT ON COLUMN spectra_core.sys_log.ip IS '操作IP';
COMMENT ON COLUMN spectra_core.sys_log.user_agent IS '客户端信息';
COMMENT ON COLUMN spectra_core.sys_log.request_params IS '请求参数';
COMMENT ON COLUMN spectra_core.sys_log.response_result IS '响应结果';
COMMENT ON COLUMN spectra_core.sys_log.duration IS '执行耗时(ms)';
COMMENT ON COLUMN spectra_core.sys_log.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_log.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_log.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_log.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_log.deleted IS '是否删除';
COMMENT ON COLUMN spectra_core.sys_log.version IS '乐观锁';

-- ============================================
-- 文件管理
-- ============================================

-- 文件信息
CREATE TABLE spectra_core.file_info (
    id             UUID PRIMARY KEY,
    filename       VARCHAR(255) NOT NULL,
    original_name  VARCHAR(255),
    content_type   VARCHAR(100),
    size           BIGINT NOT NULL,
-- __V1_APPEND_5__
    hash           VARCHAR(64) NOT NULL,
    storage_type   VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    ref_count      INTEGER DEFAULT 1,
    created_by     UUID,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by     UUID,
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted        TIMESTAMP(6) WITH TIME ZONE,
    version        BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_core.file_info IS '文件信息表';
COMMENT ON COLUMN spectra_core.file_info.id IS '主键ID';
COMMENT ON COLUMN spectra_core.file_info.filename IS '存储文件名(系统生成)';
COMMENT ON COLUMN spectra_core.file_info.original_name IS '原始文件名';
COMMENT ON COLUMN spectra_core.file_info.content_type IS '文件类型(MIME)';
COMMENT ON COLUMN spectra_core.file_info.size IS '文件大小(字节)';
COMMENT ON COLUMN spectra_core.file_info.hash IS '文件哈希(MD5/SHA256，用于秒传)';
COMMENT ON COLUMN spectra_core.file_info.storage_type IS '存储类型(LOCAL/S3/OSS)';
COMMENT ON COLUMN spectra_core.file_info.status IS '文件状态(ACTIVE/DELETED)';
COMMENT ON COLUMN spectra_core.file_info.ref_count IS '引用计数(用于秒传共享文件)';
COMMENT ON COLUMN spectra_core.file_info.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.file_info.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.file_info.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.file_info.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.file_info.deleted IS '是否删除';
COMMENT ON COLUMN spectra_core.file_info.version IS '乐观锁';

-- 文件类型
CREATE TABLE spectra_core.file_type (
    id              UUID PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    extension       JSONB NOT NULL,
    mime            JSONB NOT NULL,
    magic_rules     JSONB,
    max_size        BIGINT NOT NULL,
    previewable     BOOLEAN DEFAULT FALSE,
    allowed_upload  BOOLEAN DEFAULT TRUE,
    dangerous       BOOLEAN DEFAULT FALSE,
    remark          TEXT,
    created_by      UUID,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by      UUID,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted         TIMESTAMP(6) WITH TIME ZONE,
    version         BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_core.file_type IS '文件类型表';
COMMENT ON COLUMN spectra_core.file_type.id IS '主键ID';
COMMENT ON COLUMN spectra_core.file_type.name IS '类型名称';
COMMENT ON COLUMN spectra_core.file_type.extension IS '允许的扩展名(JSON数组)';
COMMENT ON COLUMN spectra_core.file_type.mime IS 'MIME类型(JSON数组)';
COMMENT ON COLUMN spectra_core.file_type.magic_rules IS '文件魔数规则(JSON)';
COMMENT ON COLUMN spectra_core.file_type.max_size IS '最大文件大小(字节)';
COMMENT ON COLUMN spectra_core.file_type.previewable IS '是否可预览';
COMMENT ON COLUMN spectra_core.file_type.allowed_upload IS '是否允许上传';
COMMENT ON COLUMN spectra_core.file_type.dangerous IS '是否危险文件';
COMMENT ON COLUMN spectra_core.file_type.remark IS '备注';
COMMENT ON COLUMN spectra_core.file_type.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.file_type.created_at IS '创建时间';
-- __V1_APPEND_6__
COMMENT ON COLUMN spectra_core.file_type.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.file_type.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.file_type.deleted IS '是否删除';
COMMENT ON COLUMN spectra_core.file_type.version IS '乐观锁';

-- 文件上传任务
CREATE TABLE spectra_core.file_upload_task (
    id               UUID PRIMARY KEY,
    status           VARCHAR(20) NOT NULL,
    total_chunks     INTEGER NOT NULL,
    completed_chunks INTEGER DEFAULT 0,
    created_by       UUID,
    created_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by       UUID,
    updated_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted          TIMESTAMP(6) WITH TIME ZONE,
    version          BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_core.file_upload_task IS '文件上传任务表';
COMMENT ON COLUMN spectra_core.file_upload_task.id IS '主键ID';
COMMENT ON COLUMN spectra_core.file_upload_task.status IS '任务状态';
COMMENT ON COLUMN spectra_core.file_upload_task.total_chunks IS '总分片数';
COMMENT ON COLUMN spectra_core.file_upload_task.completed_chunks IS '已完成分片数';
COMMENT ON COLUMN spectra_core.file_upload_task.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.file_upload_task.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.file_upload_task.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.file_upload_task.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.file_upload_task.deleted IS '是否删除';
COMMENT ON COLUMN spectra_core.file_upload_task.version IS '乐观锁';

-- 文件上传分片
CREATE TABLE spectra_core.file_upload_chunk (
    id          UUID PRIMARY KEY,
    task_id     UUID NOT NULL,
    chunk_index INTEGER NOT NULL,
    chunk_size  BIGINT NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_core.file_upload_chunk IS '文件上传分片表';
COMMENT ON COLUMN spectra_core.file_upload_chunk.id IS '主键ID';
COMMENT ON COLUMN spectra_core.file_upload_chunk.task_id IS '所属任务ID';
COMMENT ON COLUMN spectra_core.file_upload_chunk.chunk_index IS '分片序号';
COMMENT ON COLUMN spectra_core.file_upload_chunk.chunk_size IS '分片大小(字节)';
COMMENT ON COLUMN spectra_core.file_upload_chunk.status IS '分片状态';
COMMENT ON COLUMN spectra_core.file_upload_chunk.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.file_upload_chunk.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.file_upload_chunk.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.file_upload_chunk.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.file_upload_chunk.deleted IS '是否删除';
COMMENT ON COLUMN spectra_core.file_upload_chunk.version IS '乐观锁';

-- ============================================
-- AI 模块
-- ============================================
-- __V1_APPEND_7__

-- AI 会话状态
CREATE TABLE spectra_core.ai_session (
    id          UUID PRIMARY KEY,
    session_id  VARCHAR(255) NOT NULL,
    state_key   VARCHAR(255) NOT NULL,
    item_index  INTEGER DEFAULT 0 NOT NULL,
    state_data  TEXT NOT NULL,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_core.ai_session IS 'AI-Agent会话状态存储表';
COMMENT ON COLUMN spectra_core.ai_session.session_id IS 'session id';
COMMENT ON COLUMN spectra_core.ai_session.state_key IS 'state key';
COMMENT ON COLUMN spectra_core.ai_session.item_index IS 'item_index';
COMMENT ON COLUMN spectra_core.ai_session.state_data IS 'state_data';


-- ============================================
-- 消息中心
-- ============================================
-- ============================================
-- 消息中心相关表
-- ============================================

-- -------------------------------------------
-- 1. 系统通知消息表
-- -------------------------------------------
-- 统一通知模块启用后，本表仅作为历史数据迁移源保留，新的读写统一落到
-- docs/sql/spectra_notification/建表.sql 中的 ntf_* 表，不再由 spectra-core 映射。
DROP TABLE IF EXISTS "spectra_core"."sys_notification";
CREATE TABLE "spectra_core"."sys_notification"
(
    ------------- 主键字段
    "id"            uuid           NOT NULL,
    ------------- 业务字段
    "title"         VARCHAR(255)   NOT NULL,
    "content"       TEXT,
    "type"          VARCHAR(50)    NOT NULL,
    "sender_id"     uuid,
    "sender_name"   VARCHAR(100),
    "link"          VARCHAR(500),
    "is_read"       bool           NOT NULL DEFAULT false,
    "read_at"       timestamptz(6),
    "receiver_id"   uuid           NOT NULL,
    "extra"         jsonb,
    ------------- 审计字段
    "created_by"    uuid,
    "created_at"    timestamptz(6) NOT NULL,
    "updated_by"    uuid,
    "updated_at"    timestamptz(6) NOT NULL,
    "deleted"       timestamptz(6),
    "version"       int8           DEFAULT 0
);

COMMENT ON TABLE "spectra_core"."sys_notification" IS '系统通知消息表';
-- __V1_APPEND_8__

------------- 主键字段
COMMENT ON COLUMN "spectra_core"."sys_notification"."id" IS '主键ID';

------------- 业务字段
COMMENT ON COLUMN "spectra_core"."sys_notification"."title" IS '消息标题';
COMMENT ON COLUMN "spectra_core"."sys_notification"."content" IS '消息内容';
COMMENT ON COLUMN "spectra_core"."sys_notification"."type" IS '消息类型：system-系统通知, workflow-工作流通知, oa-OA通知, inner_mail-站内信, approval-待我审批';
COMMENT ON COLUMN "spectra_core"."sys_notification"."sender_id" IS '发送者ID（站内信场景）';
COMMENT ON COLUMN "spectra_core"."sys_notification"."sender_name" IS '发送者名称（冗余字段，避免频繁JOIN）';
COMMENT ON COLUMN "spectra_core"."sys_notification"."link" IS '点击跳转路径';
COMMENT ON COLUMN "spectra_core"."sys_notification"."is_read" IS '是否已读：true-已读, false-未读';
COMMENT ON COLUMN "spectra_core"."sys_notification"."read_at" IS '阅读时间';
COMMENT ON COLUMN "spectra_core"."sys_notification"."receiver_id" IS '接收者ID（消息归属用户）';
COMMENT ON COLUMN "spectra_core"."sys_notification"."extra" IS '扩展数据（JSON格式，如流程实例ID、会议ID等）';

------------- 审计字段
COMMENT ON COLUMN "spectra_core"."sys_notification"."created_by" IS '创建人';
COMMENT ON COLUMN "spectra_core"."sys_notification"."created_at" IS '创建时间';
COMMENT ON COLUMN "spectra_core"."sys_notification"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."sys_notification"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."sys_notification"."deleted" IS '删除标识（逻辑删除）';
COMMENT ON COLUMN "spectra_core"."sys_notification"."version" IS '乐观锁';

------------- 约束
ALTER TABLE "spectra_core"."sys_notification"
    ADD CONSTRAINT "sys_notification_pkey" PRIMARY KEY ("id");

------------- 索引
CREATE INDEX "idx_notification_receiver_id" ON "spectra_core"."sys_notification" ("receiver_id");
CREATE INDEX "idx_notification_is_read" ON "spectra_core"."sys_notification" ("is_read");
CREATE INDEX "idx_notification_type" ON "spectra_core"."sys_notification" ("type");
CREATE INDEX "idx_notification_created_at" ON "spectra_core"."sys_notification" ("created_at" DESC);

COMMENT ON INDEX "spectra_core"."idx_notification_receiver_id" IS '接收者ID索引';
COMMENT ON INDEX "spectra_core"."idx_notification_is_read" IS '已读状态索引';
COMMENT ON INDEX "spectra_core"."idx_notification_type" IS '消息类型索引';
COMMENT ON INDEX "spectra_core"."idx_notification_created_at" IS '创建时间索引（降序）';


-- -------------------------------------------
-- 2. 用户通知设置表
-- -------------------------------------------
-- 统一通知模块启用后，本表仅作为历史偏好迁移源保留，新的读写统一落到
-- spectra_notification.ntf_user_preference，不再由 spectra-core 映射。
DROP TABLE IF EXISTS "spectra_core"."sys_notification_setting";
CREATE TABLE "spectra_core"."sys_notification_setting"
(
    ------------- 主键字段
    "id"                  uuid           NOT NULL,
    ------------- 业务字段
    "user_id"             uuid           NOT NULL,
    "system_enabled"      bool           NOT NULL DEFAULT true,
    "workflow_enabled"    bool           NOT NULL DEFAULT true,
    "oa_enabled"          bool           NOT NULL DEFAULT true,
    "inner_mail_enabled"  bool           NOT NULL DEFAULT true,
    "approval_enabled"    bool           NOT NULL DEFAULT true,
    "do_not_disturb"      bool           NOT NULL DEFAULT false,
    "do_not_disturb_start" TIMESTAMP(6) WITH TIME ZONE,
    "do_not_disturb_end"  TIMESTAMP(6) WITH TIME ZONE,
-- __V1_APPEND_9__
    ------------- 审计字段
    "created_by"          uuid,
    "created_at"          timestamptz(6) NOT NULL,
    "updated_by"          uuid,
    "updated_at"          timestamptz(6) NOT NULL,
    "deleted"             timestamptz(6),
    "version"             int8           DEFAULT 0
);

COMMENT ON TABLE "spectra_core"."sys_notification_setting" IS '用户通知设置表';

------------- 主键字段
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."id" IS '主键ID';

------------- 业务字段
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."user_id" IS '用户ID（一对一关联）';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."system_enabled" IS '是否接收系统通知：true-接收, false-不接收';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."workflow_enabled" IS '是否接收工作流通知：true-接收, false-不接收';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."oa_enabled" IS '是否接收OA通知：true-接收, false-不接收';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."inner_mail_enabled" IS '是否接收站内信：true-接收, false-不接收';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."approval_enabled" IS '是否接收待审批通知：true-接收, false-不接收';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."do_not_disturb" IS '免打扰模式：true-开启, false-关闭';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."do_not_disturb_start" IS '免打扰开始时间（如22:00:00）';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."do_not_disturb_end" IS '免打扰结束时间（如08:00:00）';

------------- 审计字段
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."created_by" IS '创建人';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."created_at" IS '创建时间';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."deleted" IS '删除标识（逻辑删除）';
COMMENT ON COLUMN "spectra_core"."sys_notification_setting"."version" IS '乐观锁';

------------- 约束
ALTER TABLE "spectra_core"."sys_notification_setting"
    ADD CONSTRAINT "sys_notification_setting_pkey" PRIMARY KEY ("id");

ALTER TABLE "spectra_core"."sys_notification_setting"
    ADD CONSTRAINT "uk_notification_setting_user_id" UNIQUE ("user_id");

------------- 索引
CREATE UNIQUE INDEX "idx_notification_setting_user_id" ON "spectra_core"."sys_notification_setting" ("user_id");

COMMENT ON INDEX "spectra_core"."idx_notification_setting_user_id" IS '用户ID唯一索引';
-- __V1_APPEND_10__
-- Organization membership is independent of security DataScope.
CREATE TABLE spectra_core.sys_user_department_membership (
    user_id         UUID NOT NULL REFERENCES spectra_core.sys_user (id) ON DELETE RESTRICT,
    department_id   UUID NOT NULL REFERENCES spectra_core.sys_department (id) ON DELETE RESTRICT,
    membership_type VARCHAR(16) NOT NULL,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, department_id),
    CONSTRAINT ck_sys_user_department_membership_type CHECK (membership_type IN ('PRIMARY', 'ASSOCIATED'))
);

CREATE UNIQUE INDEX uk_sys_user_primary_department_membership
    ON spectra_core.sys_user_department_membership (user_id)
    WHERE membership_type = 'PRIMARY';
CREATE INDEX idx_sys_user_department_membership_department
    ON spectra_core.sys_user_department_membership (department_id, user_id);

CREATE TABLE spectra_core.sys_department_closure (
    ancestor_id    UUID NOT NULL REFERENCES spectra_core.sys_department (id) ON DELETE CASCADE,
    descendant_id  UUID NOT NULL REFERENCES spectra_core.sys_department (id) ON DELETE CASCADE,
    depth          INTEGER NOT NULL,
    PRIMARY KEY (ancestor_id, descendant_id),
    CONSTRAINT ck_sys_department_closure_depth CHECK (depth >= 0)
);

CREATE TABLE spectra_core.sys_organization_version (
    singleton_key VARCHAR(16) PRIMARY KEY DEFAULT 'SYSTEM',
    version       BIGINT NOT NULL DEFAULT 0,
    changed_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_sys_organization_version_key CHECK (singleton_key = 'SYSTEM'),
    CONSTRAINT ck_sys_organization_version_value CHECK (version >= 0)
);

INSERT INTO spectra_core.sys_organization_version (singleton_key, version)
VALUES ('SYSTEM', 0)
ON CONFLICT (singleton_key) DO NOTHING;

ALTER TABLE spectra_core.sys_user
    ADD CONSTRAINT fk_sys_user_primary_department
    FOREIGN KEY (primary_department_id) REFERENCES spectra_core.sys_department (id) ON DELETE RESTRICT;

-- __V1_APPEND_11__
-- ============================================
-- spectra_security 目标安全 schema
--
-- 该文件是 Phase 1 的目标 DDL 契约，最终由 Flyway V1 汇总执行。
-- 不包含兼容旧 user_role、authority 或全局 data_scope 的表。
-- ============================================

CREATE SCHEMA IF NOT EXISTS spectra_security;

-- ============================================
-- Permission / Role / Assignment
-- ============================================

CREATE TABLE spectra_security.permission (
    id                 UUID PRIMARY KEY,
    code               VARCHAR(120) NOT NULL,
    name               VARCHAR(120) NOT NULL,
    resource_code      VARCHAR(80) NOT NULL,
    action_code        VARCHAR(80) NOT NULL,
    allowed_scope_modes VARCHAR(128) NOT NULL DEFAULT 'NONE',
    state              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    system_managed     BOOLEAN NOT NULL DEFAULT FALSE,
    created_by         UUID,
    created_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by         UUID,
    updated_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version            BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_security_permission_code CHECK (code ~ '^[a-z][a-z0-9_-]*(:[a-z][a-z0-9_-]*){1,2}$'),
    CONSTRAINT ck_security_permission_state CHECK (state IN ('ACTIVE', 'DEPRECATED')),
    CONSTRAINT uk_security_permission_code UNIQUE (code)
);

CREATE TABLE spectra_security.role (
    id              UUID PRIMARY KEY,
    code            VARCHAR(80) NOT NULL,
    name            VARCHAR(120) NOT NULL,
    authority_level SMALLINT NOT NULL,
    state           VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    role_kind       VARCHAR(24) NOT NULL DEFAULT 'BUSINESS',
    system_managed  BOOLEAN NOT NULL DEFAULT FALSE,
    remark          VARCHAR(500),
    created_by      UUID,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      UUID,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_security_role_code CHECK (code ~ '^ROLE_[A-Z0-9_]+$'),
    CONSTRAINT ck_security_role_authority CHECK (authority_level > 0),
    CONSTRAINT ck_security_role_state CHECK (state IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT ck_security_role_kind CHECK (role_kind IN ('BUSINESS', 'DEV_OPS', 'SYSTEM_ADMIN', 'AUDITOR')),
    CONSTRAINT uk_security_role_code UNIQUE (code)
);

CREATE TABLE spectra_security.role_permission (
    role_id       UUID NOT NULL REFERENCES spectra_security.role (id) ON DELETE RESTRICT,
    permission_id UUID NOT NULL REFERENCES spectra_security.permission (id) ON DELETE RESTRICT,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

-- __V1_APPEND_12__
CREATE TABLE spectra_security.role_grantable_permission (
    role_id       UUID NOT NULL REFERENCES spectra_security.role (id) ON DELETE RESTRICT,
    permission_id UUID NOT NULL REFERENCES spectra_security.permission (id) ON DELETE RESTRICT,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE spectra_security.role_assignment (
    id            UUID PRIMARY KEY,
    user_id       UUID NOT NULL REFERENCES spectra_core.sys_user (id) ON DELETE RESTRICT,
    role_id       UUID NOT NULL REFERENCES spectra_security.role (id) ON DELETE RESTRICT,
    state         VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    valid_from    TIMESTAMP(6) WITH TIME ZONE,
    valid_until   TIMESTAMP(6) WITH TIME ZONE,
    assigned_by   UUID,
    assigned_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_by    UUID,
    revoked_at    TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_security_role_assignment_state CHECK (state IN ('ACTIVE', 'REVOKED', 'EXPIRED')),
    CONSTRAINT ck_security_role_assignment_period CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until > valid_from)
);

CREATE INDEX idx_security_role_assignment_user_state
    ON spectra_security.role_assignment (user_id, state);
CREATE INDEX idx_security_role_assignment_role_state
    ON spectra_security.role_assignment (role_id, state);

-- ============================================
-- Permission-specific Access / Grant Boundary
-- ============================================

CREATE TABLE spectra_security.authorization_scope (
    id            UUID PRIMARY KEY,
    scope_mode    VARCHAR(8) NOT NULL,
    resource_code VARCHAR(80),
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_security_scope_mode CHECK (scope_mode IN ('NONE', 'ALL', 'SELF', 'RULES')),
    CONSTRAINT ck_security_scope_resource CHECK (scope_mode IN ('NONE', 'SELF') OR resource_code IS NOT NULL)
);

CREATE TABLE spectra_security.assignment_permission_boundary (
    assignment_id UUID NOT NULL REFERENCES spectra_security.role_assignment (id) ON DELETE RESTRICT,
    permission_id UUID NOT NULL REFERENCES spectra_security.permission (id) ON DELETE RESTRICT,
    scope_id      UUID NOT NULL REFERENCES spectra_security.authorization_scope (id) ON DELETE RESTRICT,
    version       BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (assignment_id, permission_id)
);

CREATE TABLE spectra_security.assignment_grant_boundary (
    assignment_id UUID NOT NULL REFERENCES spectra_security.role_assignment (id) ON DELETE RESTRICT,
    permission_id UUID NOT NULL REFERENCES spectra_security.permission (id) ON DELETE RESTRICT,
    scope_id      UUID NOT NULL REFERENCES spectra_security.authorization_scope (id) ON DELETE RESTRICT,
    version       BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (assignment_id, permission_id)
);

CREATE TABLE spectra_security.scope_rule (
    id                  UUID PRIMARY KEY,
    scope_id             UUID NOT NULL REFERENCES spectra_security.authorization_scope (id) ON DELETE RESTRICT,
-- __V1_APPEND_13__
    rule_type            VARCHAR(24) NOT NULL,
    department_id        UUID REFERENCES spectra_core.sys_department (id) ON DELETE RESTRICT,
    include_descendants  BOOLEAN NOT NULL DEFAULT FALSE,
    rule_payload         JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_security_scope_rule_type CHECK (rule_type IN ('DEPARTMENT', 'RESOURCE_RULE')),
    CONSTRAINT ck_security_scope_rule_department CHECK (rule_type <> 'DEPARTMENT' OR department_id IS NOT NULL)
);

CREATE UNIQUE INDEX uk_security_scope_rule_department
    ON spectra_security.scope_rule (scope_id, department_id, include_descendants)
    WHERE rule_type = 'DEPARTMENT';
CREATE INDEX idx_security_scope_rule_department
    ON spectra_security.scope_rule (department_id, include_descendants);

-- ============================================
-- Authentication / Client / Policy
-- ============================================

CREATE TABLE spectra_security.authentication_identity (
    id             UUID PRIMARY KEY,
    user_id        UUID NOT NULL REFERENCES spectra_core.sys_user (id) ON DELETE RESTRICT,
    method_code    VARCHAR(32) NOT NULL,
    provider_code  VARCHAR(64) NOT NULL DEFAULT 'LOCAL',
    identifier_hash VARCHAR(128) NOT NULL,
    state          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    verified_at    TIMESTAMP(6) WITH TIME ZONE,
    last_used_at   TIMESTAMP(6) WITH TIME ZONE,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version        BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_security_identity_state CHECK (state IN ('ACTIVE', 'DISABLED', 'REVOKED')),
    CONSTRAINT uk_security_identity_identifier UNIQUE (method_code, provider_code, identifier_hash)
);

CREATE INDEX idx_security_identity_user_state
    ON spectra_security.authentication_identity (user_id, state);

CREATE TABLE spectra_security.password_credential (
    user_id          UUID PRIMARY KEY REFERENCES spectra_core.sys_user (id) ON DELETE RESTRICT,
    password_hash    VARCHAR(255) NOT NULL,
    changed_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expires_at       TIMESTAMP(6) WITH TIME ZONE,
    must_change      BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts  INTEGER NOT NULL DEFAULT 0,
    locked_until     TIMESTAMP(6) WITH TIME ZONE,
    version          BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE spectra_security.security_client (
    id          UUID PRIMARY KEY,
    code        VARCHAR(32) NOT NULL,
    name        VARCHAR(80) NOT NULL,
    state       VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_security_client_state CHECK (state IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT uk_security_client_code UNIQUE (code)
);
-- __V1_APPEND_14__

CREATE TABLE spectra_security.authentication_method (
    id            UUID PRIMARY KEY,
    code          VARCHAR(32) NOT NULL,
    name          VARCHAR(80) NOT NULL,
    state         VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    secret_ref    VARCHAR(255),
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version       BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_security_auth_method_state CHECK (state IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT uk_security_auth_method_code UNIQUE (code)
);

CREATE TABLE spectra_security.client_auth_method (
    client_id              UUID NOT NULL REFERENCES spectra_security.security_client (id) ON DELETE RESTRICT,
    authentication_method_id UUID NOT NULL REFERENCES spectra_security.authentication_method (id) ON DELETE RESTRICT,
    PRIMARY KEY (client_id, authentication_method_id)
);

CREATE TABLE spectra_security.session_policy (
    client_id          UUID PRIMARY KEY REFERENCES spectra_security.security_client (id) ON DELETE RESTRICT,
    concurrency_mode   VARCHAR(16) NOT NULL DEFAULT 'ALLOW',
    allow_concurrent   BOOLEAN NOT NULL DEFAULT TRUE,
    max_sessions       INTEGER NOT NULL DEFAULT 1,
    access_ttl_seconds INTEGER NOT NULL DEFAULT 300,
    refresh_ttl_seconds INTEGER NOT NULL DEFAULT 604800,
    absolute_ttl_seconds INTEGER,
    idle_ttl_seconds INTEGER,
    version            BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_security_session_mode CHECK (concurrency_mode IN ('ALLOW', 'KICK_OLD', 'REJECT_NEW')),
    CONSTRAINT ck_security_session_limits CHECK (max_sessions > 0 AND access_ttl_seconds > 0 AND refresh_ttl_seconds > 0)
);

CREATE TABLE spectra_security.password_policy (
    policy_key          VARCHAR(32) PRIMARY KEY DEFAULT 'SYSTEM',
    min_length          INTEGER NOT NULL DEFAULT 12,
    require_uppercase   BOOLEAN NOT NULL DEFAULT TRUE,
    require_lowercase   BOOLEAN NOT NULL DEFAULT TRUE,
    require_digit       BOOLEAN NOT NULL DEFAULT TRUE,
    require_special     BOOLEAN NOT NULL DEFAULT TRUE,
    max_age_days        INTEGER,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_security_password_policy_key CHECK (policy_key = 'SYSTEM'),
    CONSTRAINT ck_security_password_policy_length CHECK (min_length >= 8)
);

-- ============================================
-- MFA / Recovery
-- ============================================

CREATE TABLE spectra_security.mfa_enrollment (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL REFERENCES spectra_core.sys_user (id) ON DELETE RESTRICT,
    factor_type  VARCHAR(24) NOT NULL,
    state        VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    enrolled_at  TIMESTAMP(6) WITH TIME ZONE,
    revoked_at   TIMESTAMP(6) WITH TIME ZONE,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version      BIGINT NOT NULL DEFAULT 0,
-- __V1_APPEND_15__
    CONSTRAINT ck_security_mfa_factor CHECK (factor_type IN ('TOTP', 'WEBAUTHN', 'PASSKEY')),
    CONSTRAINT ck_security_mfa_state CHECK (state IN ('PENDING', 'ACTIVE', 'REVOKED'))
);

CREATE UNIQUE INDEX uk_security_mfa_user_factor_active
    ON spectra_security.mfa_enrollment (user_id, factor_type)
    WHERE state = 'ACTIVE';

CREATE TABLE spectra_security.totp_credential (
    enrollment_id UUID PRIMARY KEY REFERENCES spectra_security.mfa_enrollment (id) ON DELETE RESTRICT,
    encrypted_secret BYTEA NOT NULL,
    key_version    VARCHAR(64) NOT NULL,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE spectra_security.recovery_code (
    id            UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES spectra_security.mfa_enrollment (id) ON DELETE RESTRICT,
    code_hash     VARCHAR(255) NOT NULL,
    used_at       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_security_recovery_code_hash UNIQUE (enrollment_id, code_hash)
);

-- ============================================
-- Root policy / immutable Security Audit / outbox
-- ============================================

CREATE TABLE spectra_security.root_policy (
    policy_key                    VARCHAR(32) PRIMARY KEY DEFAULT 'SYSTEM',
    min_effective_dev_ops_users   INTEGER NOT NULL DEFAULT 1,
    max_dev_ops_users             INTEGER NOT NULL DEFAULT 3,
    version                       BIGINT NOT NULL DEFAULT 0,
    created_at                    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_security_root_policy_key CHECK (policy_key = 'SYSTEM'),
    CONSTRAINT ck_security_root_policy_limits CHECK (min_effective_dev_ops_users >= 1
        AND max_dev_ops_users >= min_effective_dev_ops_users)
);

INSERT INTO spectra_security.root_policy (policy_key, min_effective_dev_ops_users, max_dev_ops_users)
VALUES ('SYSTEM', 1, 3)
ON CONFLICT (policy_key) DO NOTHING;

CREATE TABLE spectra_security.security_audit_event (
    event_id          UUID NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    operator_id       UUID,
    target_id         UUID,
    client            VARCHAR(32),
    ip                VARCHAR(64),
    user_agent        VARCHAR(500),
    before_snapshot   JSONB NOT NULL DEFAULT '{}'::JSONB,
    after_snapshot    JSONB NOT NULL DEFAULT '{}'::JSONB,
    reason            VARCHAR(500),
    occurred_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    result            VARCHAR(16) NOT NULL,
    correlation_id    VARCHAR(100),
    PRIMARY KEY (event_id, occurred_at),
    CONSTRAINT ck_security_audit_result CHECK (result IN ('STARTED', 'SUCCEEDED', 'FAILED', 'DENIED'))
-- __V1_APPEND_16__
) PARTITION BY RANGE (occurred_at);

CREATE TABLE spectra_security.security_audit_event_default
    PARTITION OF spectra_security.security_audit_event DEFAULT;

CREATE INDEX idx_security_audit_event_time
    ON spectra_security.security_audit_event (occurred_at DESC);
CREATE INDEX idx_security_audit_event_type
    ON spectra_security.security_audit_event (event_type, occurred_at DESC);
CREATE INDEX idx_security_audit_event_operator
    ON spectra_security.security_audit_event (operator_id, occurred_at DESC);
CREATE INDEX idx_security_audit_event_target
    ON spectra_security.security_audit_event (target_id, occurred_at DESC);

CREATE OR REPLACE FUNCTION spectra_security.reject_audit_mutation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'security_audit_event is append-only';
END;
$$;

CREATE TRIGGER trg_security_audit_event_immutable
    BEFORE UPDATE OR DELETE ON spectra_security.security_audit_event
    FOR EACH ROW EXECUTE FUNCTION spectra_security.reject_audit_mutation();

REVOKE UPDATE, DELETE ON spectra_security.security_audit_event FROM PUBLIC;

CREATE TABLE spectra_security.security_change_outbox (
    id              UUID PRIMARY KEY,
    event_type      VARCHAR(100) NOT NULL,
    aggregate_type  VARCHAR(80) NOT NULL,
    aggregate_id    UUID,
    payload         JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at    TIMESTAMP(6) WITH TIME ZONE,
    attempts        INTEGER NOT NULL DEFAULT 0,
    last_error      VARCHAR(1000),
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_security_outbox_pending
    ON spectra_security.security_change_outbox (created_at)
    WHERE processed_at IS NULL;
-- __V1_APPEND_17__
-- Navigation visibility is a role relation, not a permission relation.
CREATE TABLE spectra_security.role_menu (
    role_id    UUID NOT NULL REFERENCES spectra_security.role (id) ON DELETE RESTRICT,
    menu_id    UUID NOT NULL REFERENCES spectra_core.sys_menu (id) ON DELETE RESTRICT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, menu_id)
);

CREATE INDEX idx_security_role_menu_menu ON spectra_security.role_menu (menu_id);

-- __V1_APPEND_18__
-- ============================================
-- spectra_oa schema 建表语句
-- OA 业务、申请、资产、库存、采购、报销、文档和合同最终结构（33 张表）
-- ============================================

CREATE SCHEMA IF NOT EXISTS spectra_oa;

-- OA 资产
CREATE TABLE spectra_oa.oa_asset (
    id                     UUID PRIMARY KEY,
    department_id          UUID,
    category_id            UUID,
    asset_no               VARCHAR(128),
    name                   VARCHAR(256),
    specification          VARCHAR(1000),
    serial_no              VARCHAR(128),
    asset_type             VARCHAR(32) NOT NULL DEFAULT 'FIXED',
    status                 VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    quantity               NUMERIC(14, 3) NOT NULL DEFAULT 1,
    acquisition_date       TIMESTAMP(6) WITH TIME ZONE,
    acquisition_amount     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    currency               VARCHAR(3) NOT NULL DEFAULT 'CNY',
    supplier               VARCHAR(256),
    location               VARCHAR(256),
    custodian_id           UUID,
    warranty_until         TIMESTAMP(6) WITH TIME ZONE,
    source_purchase_id     UUID,
    source_receipt_id      UUID,
    source_purchase_item_id UUID,
    remark                 VARCHAR(1000),
    created_by             UUID,
    created_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by             UUID,
    updated_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted                TIMESTAMP(6) WITH TIME ZONE,
    version                BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_asset IS '资产表';
COMMENT ON COLUMN spectra_oa.oa_asset.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_asset.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_asset.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_asset.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_asset.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_asset.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_asset.deleted IS '是否删除';
COMMENT ON COLUMN spectra_oa.oa_asset.version IS '乐观锁';

-- OA 日历
CREATE TABLE spectra_oa.oa_calendar (
    id               UUID PRIMARY KEY,
    owner_id         UUID,
    department_id    UUID,
    title            VARCHAR(255),
    content          TEXT,
    start_time       TIMESTAMP(6) WITH TIME ZONE,
    end_time         TIMESTAMP(6) WITH TIME ZONE,
    all_day          BOOLEAN NOT NULL DEFAULT FALSE,
    event_type       VARCHAR(32) NOT NULL DEFAULT 'PERSONAL',
    visibility       VARCHAR(32) NOT NULL DEFAULT 'PRIVATE',
    location         VARCHAR(255),
-- __V1_APPEND_19__
    participant_ids  TEXT,
    source_type      VARCHAR(32),
    source_id        UUID,
    created_by       UUID,
    created_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by       UUID,
    updated_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted          TIMESTAMP(6) WITH TIME ZONE,
    version          BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_calendar IS '日历表';
COMMENT ON COLUMN spectra_oa.oa_calendar.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_calendar.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_calendar.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_calendar.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_calendar.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_calendar.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_calendar.deleted IS '是否删除';
COMMENT ON COLUMN spectra_oa.oa_calendar.version IS '乐观锁';

-- OA 合同
CREATE TABLE spectra_oa.oa_contract (
    id                   UUID PRIMARY KEY,
    contract_no          VARCHAR(64) NOT NULL DEFAULT '',
    title                VARCHAR(255) NOT NULL DEFAULT '',
    contract_type        VARCHAR(64) NOT NULL DEFAULT 'OTHER',
    counterparty_name    VARCHAR(255) NOT NULL DEFAULT '',
    counterparty_contact VARCHAR(128),
    owner_id             UUID,
    amount               NUMERIC(18, 2) NOT NULL DEFAULT 0,
    currency             VARCHAR(16) NOT NULL DEFAULT 'CNY',
    start_date           TIMESTAMP(6) WITH TIME ZONE,
    end_date             TIMESTAMP(6) WITH TIME ZONE,
    status               VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    signing_status       VARCHAR(32) NOT NULL DEFAULT 'UNSIGNED',
    signed_at            TIMESTAMP(6) WITH TIME ZONE,
    visibility           VARCHAR(32) NOT NULL DEFAULT 'DEPARTMENT',
    summary              TEXT,
    department_id        UUID,
    created_by           UUID,
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by           UUID,
    updated_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted              TIMESTAMP(6) WITH TIME ZONE,
    version              BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_contract IS '合同表';
COMMENT ON COLUMN spectra_oa.oa_contract.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_contract.contract_no IS '合同编号';
COMMENT ON COLUMN spectra_oa.oa_contract.title IS '合同标题';
COMMENT ON COLUMN spectra_oa.oa_contract.contract_type IS '合同类型';
COMMENT ON COLUMN spectra_oa.oa_contract.counterparty_name IS '相对方名称';
COMMENT ON COLUMN spectra_oa.oa_contract.counterparty_contact IS '相对方联系人';
COMMENT ON COLUMN spectra_oa.oa_contract.owner_id IS '合同负责人';
COMMENT ON COLUMN spectra_oa.oa_contract.amount IS '合同金额';
COMMENT ON COLUMN spectra_oa.oa_contract.currency IS '币种';
COMMENT ON COLUMN spectra_oa.oa_contract.start_date IS '生效日期';
COMMENT ON COLUMN spectra_oa.oa_contract.end_date IS '到期日期';
COMMENT ON COLUMN spectra_oa.oa_contract.status IS '合同状态（DRAFT/ACTIVE/EXPIRED/TERMINATED/ARCHIVED）';
COMMENT ON COLUMN spectra_oa.oa_contract.signing_status IS '签署状态（UNSIGNED/SIGNED）';
-- __V1_APPEND_20__
COMMENT ON COLUMN spectra_oa.oa_contract.signed_at IS '签署时间';
COMMENT ON COLUMN spectra_oa.oa_contract.visibility IS '可见范围（PUBLIC/DEPARTMENT/PRIVATE）';
COMMENT ON COLUMN spectra_oa.oa_contract.summary IS '合同摘要';
COMMENT ON COLUMN spectra_oa.oa_contract.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_contract.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_contract.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_contract.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_contract.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_contract.deleted IS '是否删除';
COMMENT ON COLUMN spectra_oa.oa_contract.version IS '乐观锁';

CREATE TABLE spectra_oa.oa_contract_version (
    id            UUID PRIMARY KEY,
    contract_id   UUID NOT NULL,
    version_no    INTEGER NOT NULL,
    file_id       UUID NOT NULL,
    file_name     VARCHAR(255),
    file_size     BIGINT,
    content_type  VARCHAR(128),
    version_note  VARCHAR(500),
    is_current    BOOLEAN NOT NULL DEFAULT FALSE,
    created_by    UUID,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by    UUID,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_contract_version IS '合同文件版本表';

CREATE TABLE spectra_oa.oa_contract_milestone (
    id                UUID PRIMARY KEY,
    contract_id       UUID NOT NULL,
    name              VARCHAR(255) NOT NULL,
    milestone_type    VARCHAR(64) NOT NULL DEFAULT 'OTHER',
    due_date          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    status            VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    assignee_id       UUID,
    completed_at      TIMESTAMP(6) WITH TIME ZONE,
    reminder_sent_at  TIMESTAMP(6) WITH TIME ZONE,
    remark            VARCHAR(1000),
    created_by        UUID,
    created_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by        UUID,
    updated_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted           TIMESTAMP(6) WITH TIME ZONE,
    version           BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_contract_milestone IS '合同履约节点表';

CREATE UNIQUE INDEX uk_oa_contract_no ON spectra_oa.oa_contract (NULLIF(contract_no, ''))
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX uk_oa_contract_version_no ON spectra_oa.oa_contract_version (contract_id, version_no)
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX uk_oa_contract_current_version ON spectra_oa.oa_contract_version (contract_id)
    WHERE is_current = TRUE AND deleted IS NULL;
CREATE INDEX idx_oa_contract_status ON spectra_oa.oa_contract (status, end_date);
CREATE INDEX idx_oa_contract_counterparty ON spectra_oa.oa_contract (counterparty_name);
CREATE INDEX idx_oa_contract_version_contract ON spectra_oa.oa_contract_version (contract_id, version_no DESC);
CREATE INDEX idx_oa_contract_milestone_due ON spectra_oa.oa_contract_milestone (due_date, status);
-- __V1_APPEND_21__

-- OA 文档
CREATE TABLE spectra_oa.oa_document (
    id            UUID PRIMARY KEY,
    folder_id     UUID,
    title         VARCHAR(255) NOT NULL DEFAULT '',
    summary       TEXT,
    status        VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    visibility    VARCHAR(32) NOT NULL DEFAULT 'DEPARTMENT',
    owner_id      UUID,
    published_at  TIMESTAMP(6) WITH TIME ZONE,
    department_id UUID,
    created_by    UUID,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by    UUID,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_document IS '文档表';
COMMENT ON COLUMN spectra_oa.oa_document.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_document.folder_id IS '所属目录ID';
COMMENT ON COLUMN spectra_oa.oa_document.title IS '文档标题';
COMMENT ON COLUMN spectra_oa.oa_document.summary IS '文档摘要';
COMMENT ON COLUMN spectra_oa.oa_document.status IS '文档状态（DRAFT/PUBLISHED/ARCHIVED）';
COMMENT ON COLUMN spectra_oa.oa_document.visibility IS '可见范围（PUBLIC/DEPARTMENT/PRIVATE）';
COMMENT ON COLUMN spectra_oa.oa_document.owner_id IS '文档所有者';
COMMENT ON COLUMN spectra_oa.oa_document.published_at IS '发布时间';
COMMENT ON COLUMN spectra_oa.oa_document.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_document.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_document.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_document.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_document.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_document.deleted IS '是否删除';
COMMENT ON COLUMN spectra_oa.oa_document.version IS '乐观锁';

-- OA 文档目录
CREATE TABLE IF NOT EXISTS spectra_oa.oa_document_folder (
    id            UUID PRIMARY KEY,
    pid           UUID,
    name          VARCHAR(128) NOT NULL,
    department_id UUID,
    visibility    VARCHAR(32) NOT NULL DEFAULT 'DEPARTMENT',
    sort          INTEGER NOT NULL DEFAULT 0,
    created_by    UUID,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by    UUID,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT DEFAULT 0
);

-- OA 文档版本
CREATE TABLE IF NOT EXISTS spectra_oa.oa_document_version (
    id            UUID PRIMARY KEY,
    document_id   UUID NOT NULL,
    version_no    INTEGER NOT NULL,
    file_id       UUID NOT NULL,
    file_name     VARCHAR(255),
    file_size     BIGINT,
-- __V1_APPEND_22__
    content_type  VARCHAR(128),
    version_note  VARCHAR(500),
    is_current    BOOLEAN NOT NULL DEFAULT FALSE,
    created_by    UUID,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by    UUID,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_oa_document_version_no
    ON spectra_oa.oa_document_version (document_id, version_no)
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_oa_document_current_version
    ON spectra_oa.oa_document_version (document_id)
    WHERE is_current = TRUE AND deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_document_folder ON spectra_oa.oa_document (folder_id);
CREATE INDEX IF NOT EXISTS idx_oa_document_status ON spectra_oa.oa_document (status, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_oa_document_version_document
    ON spectra_oa.oa_document_version (document_id, version_no DESC);

-- OA 会议
CREATE TABLE spectra_oa.oa_meeting (
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
    department_id       UUID,
    created_by          UUID,
    created_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by          UUID,
    updated_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted             TIMESTAMP(6) WITH TIME ZONE,
    version             BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_meeting IS '会议表';
COMMENT ON COLUMN spectra_oa.oa_meeting.title IS '会议标题';
COMMENT ON COLUMN spectra_oa.oa_meeting.initiator_id IS '发起人ID';
COMMENT ON COLUMN spectra_oa.oa_meeting.start_time IS '开始时间';
COMMENT ON COLUMN spectra_oa.oa_meeting.end_time IS '结束时间';
COMMENT ON COLUMN spectra_oa.oa_meeting.location IS '会议地点';
COMMENT ON COLUMN spectra_oa.oa_meeting.content IS '会议内容';
COMMENT ON COLUMN spectra_oa.oa_meeting.status IS '状态 draft/cancelled';
COMMENT ON COLUMN spectra_oa.oa_meeting.process_instance_id IS '流程实例ID';
COMMENT ON COLUMN spectra_oa.oa_meeting.approval_status IS '审批状态 draft/cancelled';

-- OA 参会人员
CREATE TABLE spectra_oa.oa_meeting_participant (
    id            UUID PRIMARY KEY,
    meeting_id    UUID NOT NULL,
    user_id       UUID NOT NULL,
    role          VARCHAR(32) DEFAULT 'attendee',
    status        VARCHAR(32) DEFAULT 'pending',
-- __V1_APPEND_23__
    check_in_at   TIMESTAMP(6) WITH TIME ZONE,
    department_id UUID,
    created_by    UUID,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by    UUID,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_meeting_participant IS '参会人员表';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.meeting_id IS '会议ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.user_id IS '用户ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.role IS '角色 attendee/organizer';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.status IS '状态 pending/accepted/declined';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.check_in_at IS '签到时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.deleted IS '是否删除';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.version IS '乐观锁';

-- OA 会议纪要
CREATE TABLE spectra_oa.oa_meeting_record (
    id            UUID PRIMARY KEY,
    meeting_id    UUID NOT NULL,
    content       TEXT,
    department_id UUID,
    created_by    UUID,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by    UUID,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_meeting_record IS '会议纪要表';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.meeting_id IS '会议ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.content IS '纪要内容';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.deleted IS '是否删除';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.version IS '乐观锁';

-- OA 公告通知
CREATE TABLE spectra_oa.oa_notice (
    id                    UUID PRIMARY KEY,
    title                 VARCHAR(255),
    summary               VARCHAR(1000),
    content               TEXT,
    status                VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    target_type           VARCHAR(32) NOT NULL DEFAULT 'ALL',
    target_department_id  UUID,
    publisher_id          UUID,
    publish_at            TIMESTAMP(6) WITH TIME ZONE,
-- __V1_APPEND_24__
    required_read         BOOLEAN NOT NULL DEFAULT FALSE,
    department_id         UUID,
    created_by            UUID,
    created_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by            UUID,
    updated_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted               TIMESTAMP(6) WITH TIME ZONE,
    version               BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_notice IS '公告通知表';
COMMENT ON COLUMN spectra_oa.oa_notice.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_notice.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_notice.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_notice.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_notice.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_notice.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_notice.deleted IS '是否删除';
COMMENT ON COLUMN spectra_oa.oa_notice.version IS '乐观锁';

-- ============================================================
-- 通用 OA P0：统一申请内核
-- ============================================================

CREATE TABLE IF NOT EXISTS spectra_oa.oa_application_type (
    id                     UUID PRIMARY KEY,
    code                   VARCHAR(64) NOT NULL UNIQUE,
    name                   VARCHAR(128) NOT NULL,
    form_definition_id     UUID,
    process_definition_key VARCHAR(128),
    enabled                BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order             INTEGER NOT NULL DEFAULT 0,
    description            VARCHAR(500),
    created_by             UUID,
    created_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by             UUID,
    updated_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted                TIMESTAMP(6) WITH TIME ZONE,
    version                BIGINT DEFAULT 0
);
COMMENT ON TABLE spectra_oa.oa_application_type IS 'OA 通用申请类型';

CREATE TABLE IF NOT EXISTS spectra_oa.oa_application (
    id                   UUID PRIMARY KEY,
    application_no       VARCHAR(64) NOT NULL UNIQUE,
    type_code            VARCHAR(64) NOT NULL,
    biz_id               UUID,
    applicant_id         UUID NOT NULL,
    department_id        UUID,
    title                VARCHAR(255) NOT NULL,
    status               VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    process_instance_id  VARCHAR(64),
    submitted_at         TIMESTAMP(6) WITH TIME ZONE,
    completed_at         TIMESTAMP(6) WITH TIME ZONE,
    reject_reason        VARCHAR(1000),
    created_by            UUID,
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by            UUID,
    updated_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted              TIMESTAMP(6) WITH TIME ZONE,
    version              BIGINT DEFAULT 0
-- __V1_APPEND_25__
);
COMMENT ON TABLE spectra_oa.oa_application IS 'OA 通用申请主表';
COMMENT ON COLUMN spectra_oa.oa_application.status IS 'DRAFT/IN_REVIEW/APPROVED/REJECTED/WITHDRAWN/CANCELLED';
COMMENT ON COLUMN spectra_oa.oa_application.biz_id IS '类型业务明细ID';

CREATE TABLE IF NOT EXISTS spectra_oa.oa_application_attachment (
    id             UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    file_id        UUID NOT NULL,
    file_name      VARCHAR(255),
    created_by     UUID,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by     UUID,
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted        TIMESTAMP(6) WITH TIME ZONE,
    version        BIGINT DEFAULT 0,
    CONSTRAINT uk_oa_application_attachment UNIQUE (application_id, file_id)
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_application_cc (
    id             UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    user_id        UUID NOT NULL,
    created_by     UUID,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by     UUID,
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted        TIMESTAMP(6) WITH TIME ZONE,
    version        BIGINT DEFAULT 0,
    CONSTRAINT uk_oa_application_cc UNIQUE (application_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_oa_application_applicant_status
    ON spectra_oa.oa_application (applicant_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_oa_application_process_instance
    ON spectra_oa.oa_application (process_instance_id);

-- ============================================================
-- 通用 OA P0：请假、固定班次与考勤回写
-- ============================================================

CREATE TABLE IF NOT EXISTS spectra_oa.oa_leave_type (
    id            UUID PRIMARY KEY,
    code          VARCHAR(64) NOT NULL UNIQUE,
    name          VARCHAR(128) NOT NULL,
    unit          VARCHAR(16) NOT NULL DEFAULT 'HOUR',
    default_hours NUMERIC(12, 2),
    enabled       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by    UUID,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by    UUID,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_leave_application (
    id             UUID PRIMARY KEY,
    application_id UUID NOT NULL UNIQUE,
    department_id  UUID,
-- __V1_APPEND_26__
    leave_type_code VARCHAR(64) NOT NULL,
    start_time     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    end_time       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    duration_hours NUMERIC(12, 2) NOT NULL,
    reason         VARCHAR(2000) NOT NULL,
    contact_address VARCHAR(500),
    created_by     UUID,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by     UUID,
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted        TIMESTAMP(6) WITH TIME ZONE,
    version        BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_leave_balance (
    id             UUID PRIMARY KEY,
    user_id        UUID NOT NULL,
    department_id  UUID,
    leave_type_code VARCHAR(64) NOT NULL,
    year           INTEGER NOT NULL,
    total_hours    NUMERIC(12, 2) NOT NULL DEFAULT 0,
    used_hours     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    reserved_hours NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_by     UUID,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by     UUID,
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted        TIMESTAMP(6) WITH TIME ZONE,
    version        BIGINT DEFAULT 0,
    CONSTRAINT uk_oa_leave_balance UNIQUE (user_id, leave_type_code, year)
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_work_shift (
    id            UUID PRIMARY KEY,
    name          VARCHAR(128) NOT NULL,
    work_start    TIME NOT NULL DEFAULT '09:00',
    lunch_start   TIME NOT NULL DEFAULT '12:00',
    lunch_end     TIME NOT NULL DEFAULT '13:00',
    work_end      TIME NOT NULL DEFAULT '18:00',
    enabled       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by    UUID,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by    UUID,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_work_calendar (
    id            UUID PRIMARY KEY,
    calendar_date DATE NOT NULL UNIQUE,
    is_workday    BOOLEAN NOT NULL DEFAULT TRUE,
    shift_id      UUID,
    remark        VARCHAR(255),
    created_by    UUID,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by    UUID,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted       TIMESTAMP(6) WITH TIME ZONE,
    version       BIGINT DEFAULT 0
-- __V1_APPEND_27__
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_attendance_record (
    id              UUID PRIMARY KEY,
    application_id  UUID NOT NULL,
    user_id         UUID NOT NULL,
    attendance_date TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    status          VARCHAR(32) NOT NULL,
    source          VARCHAR(32) NOT NULL,
    department_id   UUID,
    created_by      UUID,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by      UUID,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted         TIMESTAMP(6) WITH TIME ZONE,
    version         BIGINT DEFAULT 0,
    CONSTRAINT uk_oa_attendance_record UNIQUE (application_id, attendance_date)
);

CREATE INDEX IF NOT EXISTS idx_oa_leave_application_time
    ON spectra_oa.oa_leave_application (start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_oa_attendance_record_user_date
    ON spectra_oa.oa_attendance_record (user_id, attendance_date);

-- P0 最小内置配置：请假类型与流程定义 KEY 对齐，流程图由 Workflow 模块部署。
INSERT INTO spectra_oa.oa_application_type
    (id, code, name, process_definition_key, enabled, sort_order, description,
     created_at, updated_at, version)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'leave', '请假申请', 'oa_leave_approval', TRUE, 10,
     '通用 OA 请假审批', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS spectra_oa.oa_asset_category (
    id UUID PRIMARY KEY, pid UUID, code VARCHAR(64) NOT NULL, name VARCHAR(128) NOT NULL,
    asset_type VARCHAR(32) NOT NULL DEFAULT 'FIXED', sort INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE, description VARCHAR(500),
    created_by UUID, created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by UUID, updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted TIMESTAMP(6) WITH TIME ZONE, version BIGINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS spectra_oa.oa_asset_operation (
    id UUID PRIMARY KEY, asset_id UUID NOT NULL, operation_type VARCHAR(32) NOT NULL,
    from_department_id UUID, to_department_id UUID, from_user_id UUID, to_user_id UUID,
    from_location VARCHAR(256), to_location VARCHAR(256), operation_date TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    reason VARCHAR(1000), maintenance_content VARCHAR(2000), maintenance_cost NUMERIC(14, 2),
    status VARCHAR(32) NOT NULL DEFAULT 'COMPLETE', created_by UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL, updated_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL, deleted TIMESTAMP(6) WITH TIME ZONE,
    version BIGINT DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_oa_asset_asset_no ON spectra_oa.oa_asset (asset_no)
    WHERE deleted IS NULL AND asset_no IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_oa_asset_category_code ON spectra_oa.oa_asset_category (code)
    WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_asset_status_department
    ON spectra_oa.oa_asset (status, department_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_oa_asset_source_receipt_item
    ON spectra_oa.oa_asset (source_receipt_id, source_purchase_item_id);
CREATE INDEX IF NOT EXISTS idx_oa_asset_operation_asset_date
-- __V1_APPEND_28__
    ON spectra_oa.oa_asset_operation (asset_id, operation_date DESC, created_at DESC);
INSERT INTO spectra_oa.oa_asset_category
    (id, code, name, asset_type, sort, enabled, created_at, updated_at, version)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'OFFICE_EQUIPMENT', '办公设备', 'FIXED', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000102', 'IT_EQUIPMENT', '信息设备', 'FIXED', 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000103', 'FURNITURE', '办公家具', 'FIXED', 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000104', 'VEHICLE', '车辆', 'FIXED', 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- P1 阶段 3：办公用品库存 MVP
CREATE TABLE IF NOT EXISTS spectra_oa.oa_supply_item (
    id UUID PRIMARY KEY, category VARCHAR(128), sku VARCHAR(128) NOT NULL,
    name VARCHAR(256) NOT NULL, specification VARCHAR(1000), unit VARCHAR(32) NOT NULL DEFAULT '件',
    current_stock NUMERIC(14, 3) NOT NULL DEFAULT 0, min_stock NUMERIC(14, 3) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', supplier VARCHAR(256), location VARCHAR(256),
    department_id UUID, remark VARCHAR(1000), created_by UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL, updated_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL, deleted TIMESTAMP(6) WITH TIME ZONE,
    version BIGINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS spectra_oa.oa_supply_operation (
    id UUID PRIMARY KEY, supply_id UUID NOT NULL, operation_type VARCHAR(32) NOT NULL,
    quantity NUMERIC(14, 3) NOT NULL, before_stock NUMERIC(14, 3) NOT NULL,
    after_stock NUMERIC(14, 3) NOT NULL, department_id UUID, user_id UUID,
    location VARCHAR(256), operation_date TIMESTAMP(6) WITH TIME ZONE NOT NULL, reason VARCHAR(1000),
    source_purchase_id UUID, source_receipt_id UUID, source_purchase_item_id UUID,
    status VARCHAR(32) NOT NULL DEFAULT 'COMPLETE', created_by UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL, updated_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL, deleted TIMESTAMP(6) WITH TIME ZONE,
    version BIGINT DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_oa_supply_item_sku ON spectra_oa.oa_supply_item (sku)
    WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_supply_item_stock
    ON spectra_oa.oa_supply_item (status, current_stock, min_stock, name);
CREATE INDEX IF NOT EXISTS idx_oa_supply_operation_supply_date
    ON spectra_oa.oa_supply_operation (supply_id, operation_date DESC, created_at DESC);

-- P1 阶段 3：采购申请、执行跟踪与分批收货。
CREATE TABLE IF NOT EXISTS spectra_oa.oa_purchase (
    id                  UUID PRIMARY KEY,
    application_id      UUID NOT NULL UNIQUE,
    department_id       UUID,
    purpose             VARCHAR(2000) NOT NULL,
    expected_date       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    budget_amount       NUMERIC(14, 2) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'CNY',
    suggested_supplier  VARCHAR(256),
    execution_status    VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED',
    purchaser_id        UUID,
    order_no            VARCHAR(128),
    ordered_at          TIMESTAMP(6) WITH TIME ZONE,
    completed_at        TIMESTAMP(6) WITH TIME ZONE,
    execution_remark    VARCHAR(1000),
    created_by          UUID,
    created_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by          UUID,
    updated_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted             TIMESTAMP(6) WITH TIME ZONE,
-- __V1_APPEND_29__
    version             BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_purchase_item (
    id                    UUID PRIMARY KEY,
    purchase_id           UUID NOT NULL,
    department_id         UUID,
    item_type             VARCHAR(32) NOT NULL,
    item_name             VARCHAR(256) NOT NULL,
    specification         VARCHAR(1000),
    quantity              NUMERIC(14, 3) NOT NULL,
    estimated_unit_price  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    estimated_amount      NUMERIC(14, 2) NOT NULL DEFAULT 0,
    purpose               VARCHAR(500),
    received_quantity     NUMERIC(14, 3) NOT NULL DEFAULT 0,
    created_by            UUID,
    created_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by            UUID,
    updated_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted               TIMESTAMP(6) WITH TIME ZONE,
    version               BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_purchase_receipt (
    id              UUID PRIMARY KEY,
    purchase_id     UUID NOT NULL,
    receipt_no      VARCHAR(128) NOT NULL,
    received_date   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    receiver_id     UUID,
    status          VARCHAR(32) NOT NULL DEFAULT 'PARTIAL',
    remark          VARCHAR(1000),
    created_by      UUID,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by      UUID,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted         TIMESTAMP(6) WITH TIME ZONE,
    version         BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_purchase_receipt_item (
    id                UUID PRIMARY KEY,
    receipt_id        UUID NOT NULL,
    purchase_item_id  UUID NOT NULL,
    quantity          NUMERIC(14, 3) NOT NULL,
    accepted          BOOLEAN NOT NULL DEFAULT TRUE,
    difference_reason VARCHAR(1000),
    created_by        UUID,
    created_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by        UUID,
    updated_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted           TIMESTAMP(6) WITH TIME ZONE,
    version           BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_oa_purchase_department_status
    ON spectra_oa.oa_purchase (department_id, execution_status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_oa_purchase_item_purchase
    ON spectra_oa.oa_purchase_item (purchase_id, created_at);
CREATE INDEX IF NOT EXISTS idx_oa_purchase_receipt_purchase
    ON spectra_oa.oa_purchase_receipt (purchase_id, received_date DESC);
-- __V1_APPEND_30__
CREATE UNIQUE INDEX IF NOT EXISTS uk_oa_purchase_receipt_no
    ON spectra_oa.oa_purchase_receipt (receipt_no)
    WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_purchase_receipt_item_receipt
    ON spectra_oa.oa_purchase_receipt_item (receipt_id);

INSERT INTO spectra_oa.oa_application_type
    (id, code, name, process_definition_key, enabled, sort_order, description,
     created_at, updated_at, version)
VALUES
    ('00000000-0000-0000-0000-000000000003', 'purchase', '采购申请', 'oa_purchase_approval', TRUE, 30,
     'P1 采购申请审批', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS spectra_oa.oa_notice_reader (
    id         UUID PRIMARY KEY,
    notice_id  UUID NOT NULL,
    user_id    UUID NOT NULL,
    read_at    TIMESTAMP(6) WITH TIME ZONE,
    created_by UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted    TIMESTAMP(6) WITH TIME ZONE,
    version    BIGINT DEFAULT 0,
    CONSTRAINT uk_oa_notice_reader UNIQUE (notice_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_oa_calendar_owner_time
    ON spectra_oa.oa_calendar (owner_id, start_time, end_time);

CREATE INDEX IF NOT EXISTS idx_oa_notice_publish_scope
    ON spectra_oa.oa_notice (status, target_type, target_department_id, publish_at);

CREATE UNIQUE INDEX IF NOT EXISTS uk_oa_meeting_participant_user
    ON spectra_oa.oa_meeting_participant (meeting_id, user_id)
    WHERE deleted IS NULL;

CREATE INDEX IF NOT EXISTS idx_oa_meeting_location_time
    ON spectra_oa.oa_meeting (location, start_time, end_time);

-- P1 阶段 3：费用报销 MVP，审批状态复用 oa_application，付款状态独立维护。
CREATE TABLE IF NOT EXISTS spectra_oa.oa_reimbursement (
    id              UUID PRIMARY KEY,
    application_id  UUID NOT NULL UNIQUE,
    department_id   UUID,
    purpose         VARCHAR(2000) NOT NULL,
    expense_start   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expense_end     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    total_amount    NUMERIC(14, 2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'CNY',
    payee_name      VARCHAR(128) NOT NULL,
    payee_account   VARCHAR(256),
    payment_status  VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    payment_at      TIMESTAMP(6) WITH TIME ZONE,
    payment_remark  VARCHAR(1000),
    created_by      UUID,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by      UUID,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
-- __V1_APPEND_31__
    deleted         TIMESTAMP(6) WITH TIME ZONE,
    version         BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS spectra_oa.oa_reimbursement_item (
    id              UUID PRIMARY KEY,
    reimbursement_id UUID NOT NULL,
    department_id   UUID,
    expense_date    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    category        VARCHAR(64) NOT NULL,
    description     VARCHAR(500) NOT NULL,
    amount          NUMERIC(14, 2) NOT NULL,
    tax_amount      NUMERIC(14, 2) NOT NULL DEFAULT 0,
    invoice_no      VARCHAR(128),
    created_by      UUID,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by      UUID,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted         TIMESTAMP(6) WITH TIME ZONE,
    version         BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_oa_reimbursement_department_status
    ON spectra_oa.oa_reimbursement (department_id, payment_status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_oa_reimbursement_item_reimbursement
    ON spectra_oa.oa_reimbursement_item (reimbursement_id, expense_date);

INSERT INTO spectra_oa.oa_application_type
    (id, code, name, process_definition_key, enabled, sort_order, description,
     created_at, updated_at, version)
VALUES
    ('00000000-0000-0000-0000-000000000002', 'reimbursement', '费用报销', 'oa_reimbursement_approval', TRUE, 20,
     'P1 费用报销审批', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (code) DO NOTHING;

INSERT INTO spectra_oa.oa_leave_type
    (id, code, name, unit, default_hours, enabled, created_at, updated_at, version)
VALUES
    ('00000000-0000-0000-0000-000000000011', 'annual', '年假', 'HOUR', 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000012', 'sick', '病假', 'HOUR', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000013', 'personal', '事假', 'HOUR', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- OA 表及字段注释（最终建表结构）
-- ============================================================

COMMENT ON TABLE spectra_oa.oa_asset IS '资产台账表';
COMMENT ON COLUMN spectra_oa.oa_asset.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_asset.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_asset.category_id IS '资产分类ID';
COMMENT ON COLUMN spectra_oa.oa_asset.asset_no IS '资产编号';
COMMENT ON COLUMN spectra_oa.oa_asset.name IS '资产名称';
COMMENT ON COLUMN spectra_oa.oa_asset.specification IS '规格型号';
COMMENT ON COLUMN spectra_oa.oa_asset.serial_no IS '序列号';
COMMENT ON COLUMN spectra_oa.oa_asset.asset_type IS '资产类型';
COMMENT ON COLUMN spectra_oa.oa_asset.status IS '资产状态（DRAFT/IN_STOCK/IN_USE/RETURNED/MAINTENANCE/SCRAPPED）';
COMMENT ON COLUMN spectra_oa.oa_asset.quantity IS '资产数量';
COMMENT ON COLUMN spectra_oa.oa_asset.acquisition_date IS '购置日期';
COMMENT ON COLUMN spectra_oa.oa_asset.acquisition_amount IS '购置金额';
-- __V1_APPEND_32__
COMMENT ON COLUMN spectra_oa.oa_asset.currency IS '币种';
COMMENT ON COLUMN spectra_oa.oa_asset.supplier IS '供应商';
COMMENT ON COLUMN spectra_oa.oa_asset.location IS '存放位置';
COMMENT ON COLUMN spectra_oa.oa_asset.custodian_id IS '资产保管人ID';
COMMENT ON COLUMN spectra_oa.oa_asset.warranty_until IS '保修截止时间';
COMMENT ON COLUMN spectra_oa.oa_asset.source_purchase_id IS '来源采购申请ID';
COMMENT ON COLUMN spectra_oa.oa_asset.source_receipt_id IS '来源收货单ID';
COMMENT ON COLUMN spectra_oa.oa_asset.source_purchase_item_id IS '来源采购明细ID';
COMMENT ON COLUMN spectra_oa.oa_asset.remark IS '备注';
COMMENT ON COLUMN spectra_oa.oa_asset.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_asset.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_asset.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_asset.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_asset.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_asset.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_calendar IS '日历日程表';
COMMENT ON COLUMN spectra_oa.oa_calendar.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_calendar.owner_id IS '日程所有者ID';
COMMENT ON COLUMN spectra_oa.oa_calendar.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_calendar.title IS '日程标题';
COMMENT ON COLUMN spectra_oa.oa_calendar.content IS '日程内容';
COMMENT ON COLUMN spectra_oa.oa_calendar.start_time IS '开始时间';
COMMENT ON COLUMN spectra_oa.oa_calendar.end_time IS '结束时间';
COMMENT ON COLUMN spectra_oa.oa_calendar.all_day IS '是否全天事件';
COMMENT ON COLUMN spectra_oa.oa_calendar.event_type IS '事件类型';
COMMENT ON COLUMN spectra_oa.oa_calendar.visibility IS '可见范围';
COMMENT ON COLUMN spectra_oa.oa_calendar.location IS '日程地点';
COMMENT ON COLUMN spectra_oa.oa_calendar.participant_ids IS '参与人ID列表（文本）';
COMMENT ON COLUMN spectra_oa.oa_calendar.source_type IS '来源业务类型';
COMMENT ON COLUMN spectra_oa.oa_calendar.source_id IS '来源业务ID';
COMMENT ON COLUMN spectra_oa.oa_calendar.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_calendar.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_calendar.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_calendar.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_calendar.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_calendar.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_contract IS '合同台账表';
COMMENT ON COLUMN spectra_oa.oa_contract.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_contract.contract_no IS '合同编号';
COMMENT ON COLUMN spectra_oa.oa_contract.title IS '合同标题';
COMMENT ON COLUMN spectra_oa.oa_contract.contract_type IS '合同类型';
COMMENT ON COLUMN spectra_oa.oa_contract.counterparty_name IS '相对方名称';
COMMENT ON COLUMN spectra_oa.oa_contract.counterparty_contact IS '相对方联系人';
COMMENT ON COLUMN spectra_oa.oa_contract.owner_id IS '合同负责人ID';
COMMENT ON COLUMN spectra_oa.oa_contract.amount IS '合同金额';
COMMENT ON COLUMN spectra_oa.oa_contract.currency IS '币种';
COMMENT ON COLUMN spectra_oa.oa_contract.start_date IS '生效日期';
COMMENT ON COLUMN spectra_oa.oa_contract.end_date IS '到期日期';
COMMENT ON COLUMN spectra_oa.oa_contract.status IS '合同状态（DRAFT/ACTIVE/EXPIRED/TERMINATED/ARCHIVED）';
COMMENT ON COLUMN spectra_oa.oa_contract.signing_status IS '签署状态（UNSIGNED/SIGNED）';
COMMENT ON COLUMN spectra_oa.oa_contract.signed_at IS '签署时间';
COMMENT ON COLUMN spectra_oa.oa_contract.visibility IS '可见范围（PUBLIC/DEPARTMENT/PRIVATE）';
COMMENT ON COLUMN spectra_oa.oa_contract.summary IS '合同摘要';
COMMENT ON COLUMN spectra_oa.oa_contract.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_contract.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_contract.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_contract.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_contract.updated_at IS '最后更新时间';
-- __V1_APPEND_33__
COMMENT ON COLUMN spectra_oa.oa_contract.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_contract.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_contract_version IS '合同文件版本表';
COMMENT ON COLUMN spectra_oa.oa_contract_version.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_contract_version.contract_id IS '合同ID';
COMMENT ON COLUMN spectra_oa.oa_contract_version.version_no IS '版本号';
COMMENT ON COLUMN spectra_oa.oa_contract_version.file_id IS '文件ID';
COMMENT ON COLUMN spectra_oa.oa_contract_version.file_name IS '文件名称';
COMMENT ON COLUMN spectra_oa.oa_contract_version.file_size IS '文件大小（字节）';
COMMENT ON COLUMN spectra_oa.oa_contract_version.content_type IS '文件MIME类型';
COMMENT ON COLUMN spectra_oa.oa_contract_version.version_note IS '版本说明';
COMMENT ON COLUMN spectra_oa.oa_contract_version.is_current IS '是否当前版本';
COMMENT ON COLUMN spectra_oa.oa_contract_version.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_contract_version.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_contract_version.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_contract_version.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_contract_version.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_contract_version.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_contract_milestone IS '合同履约节点表';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.contract_id IS '合同ID';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.name IS '节点名称';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.milestone_type IS '节点类型';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.due_date IS '计划截止时间';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.status IS '节点状态';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.assignee_id IS '节点负责人ID';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.completed_at IS '完成时间';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.reminder_sent_at IS '提醒发送时间';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.remark IS '备注';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_contract_milestone.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_document IS '文档表';
COMMENT ON COLUMN spectra_oa.oa_document.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_document.folder_id IS '所属目录ID';
COMMENT ON COLUMN spectra_oa.oa_document.title IS '文档标题';
COMMENT ON COLUMN spectra_oa.oa_document.summary IS '文档摘要';
COMMENT ON COLUMN spectra_oa.oa_document.status IS '文档状态（DRAFT/PUBLISHED/ARCHIVED）';
COMMENT ON COLUMN spectra_oa.oa_document.visibility IS '可见范围（PUBLIC/DEPARTMENT/PRIVATE）';
COMMENT ON COLUMN spectra_oa.oa_document.owner_id IS '文档所有者ID';
COMMENT ON COLUMN spectra_oa.oa_document.published_at IS '发布时间';
COMMENT ON COLUMN spectra_oa.oa_document.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_document.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_document.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_document.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_document.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_document.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_document.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_document_folder IS '文档目录表';
COMMENT ON COLUMN spectra_oa.oa_document_folder.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_document_folder.pid IS '父目录ID';
COMMENT ON COLUMN spectra_oa.oa_document_folder.name IS '目录名称';
COMMENT ON COLUMN spectra_oa.oa_document_folder.department_id IS '所属部门ID';
-- __V1_APPEND_34__
COMMENT ON COLUMN spectra_oa.oa_document_folder.visibility IS '可见范围';
COMMENT ON COLUMN spectra_oa.oa_document_folder.sort IS '排序号';
COMMENT ON COLUMN spectra_oa.oa_document_folder.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_document_folder.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_document_folder.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_document_folder.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_document_folder.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_document_folder.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_document_version IS '文档文件版本表';
COMMENT ON COLUMN spectra_oa.oa_document_version.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_document_version.document_id IS '文档ID';
COMMENT ON COLUMN spectra_oa.oa_document_version.version_no IS '版本号';
COMMENT ON COLUMN spectra_oa.oa_document_version.file_id IS '文件ID';
COMMENT ON COLUMN spectra_oa.oa_document_version.file_name IS '文件名称';
COMMENT ON COLUMN spectra_oa.oa_document_version.file_size IS '文件大小（字节）';
COMMENT ON COLUMN spectra_oa.oa_document_version.content_type IS '文件MIME类型';
COMMENT ON COLUMN spectra_oa.oa_document_version.version_note IS '版本说明';
COMMENT ON COLUMN spectra_oa.oa_document_version.is_current IS '是否当前版本';
COMMENT ON COLUMN spectra_oa.oa_document_version.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_document_version.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_document_version.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_document_version.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_document_version.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_document_version.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_meeting IS '会议表';
COMMENT ON COLUMN spectra_oa.oa_meeting.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_meeting.title IS '会议标题';
COMMENT ON COLUMN spectra_oa.oa_meeting.initiator_id IS '发起人ID';
COMMENT ON COLUMN spectra_oa.oa_meeting.start_time IS '开始时间';
COMMENT ON COLUMN spectra_oa.oa_meeting.end_time IS '结束时间';
COMMENT ON COLUMN spectra_oa.oa_meeting.location IS '会议地点';
COMMENT ON COLUMN spectra_oa.oa_meeting.content IS '会议内容';
COMMENT ON COLUMN spectra_oa.oa_meeting.status IS '会议状态';
COMMENT ON COLUMN spectra_oa.oa_meeting.process_instance_id IS '流程实例ID';
COMMENT ON COLUMN spectra_oa.oa_meeting.approval_status IS '审批状态';
COMMENT ON COLUMN spectra_oa.oa_meeting.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_meeting.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_meeting.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_meeting.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_meeting.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_meeting.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_meeting.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_meeting_participant IS '会议参会人员表';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.meeting_id IS '会议ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.user_id IS '用户ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.role IS '参会角色（attendee/organizer）';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.status IS '参会状态（pending/accepted/declined）';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.check_in_at IS '签到时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_meeting_participant.version IS '乐观锁版本号';

-- __V1_APPEND_35__
COMMENT ON TABLE spectra_oa.oa_meeting_record IS '会议纪要表';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.meeting_id IS '会议ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.content IS '纪要内容';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_meeting_record.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_notice IS '公告通知表';
COMMENT ON COLUMN spectra_oa.oa_notice.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_notice.title IS '公告标题';
COMMENT ON COLUMN spectra_oa.oa_notice.summary IS '公告摘要';
COMMENT ON COLUMN spectra_oa.oa_notice.content IS '公告内容';
COMMENT ON COLUMN spectra_oa.oa_notice.status IS '公告状态';
COMMENT ON COLUMN spectra_oa.oa_notice.target_type IS '发布目标类型（ALL/DEPARTMENT）';
COMMENT ON COLUMN spectra_oa.oa_notice.target_department_id IS '目标部门ID';
COMMENT ON COLUMN spectra_oa.oa_notice.publisher_id IS '发布人ID';
COMMENT ON COLUMN spectra_oa.oa_notice.publish_at IS '发布时间';
COMMENT ON COLUMN spectra_oa.oa_notice.required_read IS '是否要求阅读';
COMMENT ON COLUMN spectra_oa.oa_notice.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_notice.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_notice.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_notice.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_notice.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_notice.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_notice.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_notice_reader IS '公告阅读回执表';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.notice_id IS '公告ID';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.user_id IS '阅读用户ID';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.read_at IS '阅读时间';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_notice_reader.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_application_type IS 'OA 通用申请类型表';
COMMENT ON COLUMN spectra_oa.oa_application_type.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_application_type.code IS '申请类型编码';
COMMENT ON COLUMN spectra_oa.oa_application_type.name IS '申请类型名称';
COMMENT ON COLUMN spectra_oa.oa_application_type.form_definition_id IS '表单定义ID';
COMMENT ON COLUMN spectra_oa.oa_application_type.process_definition_key IS '流程定义KEY';
COMMENT ON COLUMN spectra_oa.oa_application_type.enabled IS '是否启用';
COMMENT ON COLUMN spectra_oa.oa_application_type.sort_order IS '排序号';
COMMENT ON COLUMN spectra_oa.oa_application_type.description IS '类型说明';
COMMENT ON COLUMN spectra_oa.oa_application_type.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_application_type.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_application_type.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_application_type.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_application_type.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_application_type.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_application IS 'OA 通用申请主表';
-- __V1_APPEND_36__
COMMENT ON COLUMN spectra_oa.oa_application.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_application.application_no IS '申请单编号';
COMMENT ON COLUMN spectra_oa.oa_application.type_code IS '申请类型编码';
COMMENT ON COLUMN spectra_oa.oa_application.biz_id IS '类型业务明细ID';
COMMENT ON COLUMN spectra_oa.oa_application.applicant_id IS '申请人ID';
COMMENT ON COLUMN spectra_oa.oa_application.department_id IS '申请人所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_application.title IS '申请标题';
COMMENT ON COLUMN spectra_oa.oa_application.status IS '申请状态（DRAFT/IN_REVIEW/APPROVED/REJECTED/WITHDRAWN/CANCELLED）';
COMMENT ON COLUMN spectra_oa.oa_application.process_instance_id IS '流程实例ID';
COMMENT ON COLUMN spectra_oa.oa_application.submitted_at IS '提交时间';
COMMENT ON COLUMN spectra_oa.oa_application.completed_at IS '完成时间';
COMMENT ON COLUMN spectra_oa.oa_application.reject_reason IS '驳回原因';
COMMENT ON COLUMN spectra_oa.oa_application.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_application.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_application.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_application.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_application.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_application.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_application_attachment IS '申请附件关联表';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.application_id IS '申请ID';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.file_id IS '文件ID';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.file_name IS '文件名称';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_application_attachment.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_application_cc IS '申请抄送关联表';
COMMENT ON COLUMN spectra_oa.oa_application_cc.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_application_cc.application_id IS '申请ID';
COMMENT ON COLUMN spectra_oa.oa_application_cc.user_id IS '抄送用户ID';
COMMENT ON COLUMN spectra_oa.oa_application_cc.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_application_cc.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_application_cc.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_application_cc.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_application_cc.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_application_cc.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_leave_type IS '请假类型表';
COMMENT ON COLUMN spectra_oa.oa_leave_type.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_leave_type.code IS '请假类型编码';
COMMENT ON COLUMN spectra_oa.oa_leave_type.name IS '请假类型名称';
COMMENT ON COLUMN spectra_oa.oa_leave_type.unit IS '额度单位';
COMMENT ON COLUMN spectra_oa.oa_leave_type.default_hours IS '默认年度时长';
COMMENT ON COLUMN spectra_oa.oa_leave_type.enabled IS '是否启用';
COMMENT ON COLUMN spectra_oa.oa_leave_type.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_leave_type.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_leave_type.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_leave_type.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_leave_type.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_leave_type.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_leave_application IS '请假申请明细表';
COMMENT ON COLUMN spectra_oa.oa_leave_application.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_leave_application.application_id IS '通用申请ID';
COMMENT ON COLUMN spectra_oa.oa_leave_application.department_id IS '所属部门ID';
-- __V1_APPEND_37__
COMMENT ON COLUMN spectra_oa.oa_leave_application.leave_type_code IS '请假类型编码';
COMMENT ON COLUMN spectra_oa.oa_leave_application.start_time IS '请假开始时间';
COMMENT ON COLUMN spectra_oa.oa_leave_application.end_time IS '请假结束时间';
COMMENT ON COLUMN spectra_oa.oa_leave_application.duration_hours IS '请假时长（小时）';
COMMENT ON COLUMN spectra_oa.oa_leave_application.reason IS '请假原因';
COMMENT ON COLUMN spectra_oa.oa_leave_application.contact_address IS '请假期间联系地址';
COMMENT ON COLUMN spectra_oa.oa_leave_application.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_leave_application.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_leave_application.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_leave_application.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_leave_application.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_leave_application.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_leave_balance IS '请假年度额度表';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.user_id IS '用户ID';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.leave_type_code IS '请假类型编码';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.year IS '额度年度';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.total_hours IS '年度总额度（小时）';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.used_hours IS '已使用额度（小时）';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.reserved_hours IS '已预占额度（小时）';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_leave_balance.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_work_shift IS '固定工作班次表';
COMMENT ON COLUMN spectra_oa.oa_work_shift.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_work_shift.name IS '班次名称';
COMMENT ON COLUMN spectra_oa.oa_work_shift.work_start IS '上班时间';
COMMENT ON COLUMN spectra_oa.oa_work_shift.lunch_start IS '午休开始时间';
COMMENT ON COLUMN spectra_oa.oa_work_shift.lunch_end IS '午休结束时间';
COMMENT ON COLUMN spectra_oa.oa_work_shift.work_end IS '下班时间';
COMMENT ON COLUMN spectra_oa.oa_work_shift.enabled IS '是否启用';
COMMENT ON COLUMN spectra_oa.oa_work_shift.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_work_shift.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_work_shift.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_work_shift.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_work_shift.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_work_shift.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_work_calendar IS '工作日日历表';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.calendar_date IS '日历日期';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.is_workday IS '是否工作日';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.shift_id IS '班次ID';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.remark IS '备注';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_work_calendar.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_attendance_record IS '请假审批回写的考勤影响记录表';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.application_id IS '通用申请ID';
-- __V1_APPEND_38__
COMMENT ON COLUMN spectra_oa.oa_attendance_record.user_id IS '用户ID';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.attendance_date IS '考勤日期';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.status IS '考勤状态';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.source IS '记录来源';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_attendance_record.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_asset_category IS '资产分类表';
COMMENT ON COLUMN spectra_oa.oa_asset_category.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_asset_category.pid IS '父分类ID';
COMMENT ON COLUMN spectra_oa.oa_asset_category.code IS '分类编码';
COMMENT ON COLUMN spectra_oa.oa_asset_category.name IS '分类名称';
COMMENT ON COLUMN spectra_oa.oa_asset_category.asset_type IS '资产类型';
COMMENT ON COLUMN spectra_oa.oa_asset_category.sort IS '排序号';
COMMENT ON COLUMN spectra_oa.oa_asset_category.enabled IS '是否启用';
COMMENT ON COLUMN spectra_oa.oa_asset_category.description IS '分类说明';
COMMENT ON COLUMN spectra_oa.oa_asset_category.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_asset_category.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_asset_category.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_asset_category.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_asset_category.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_asset_category.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_asset_operation IS '资产生命周期操作表';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.asset_id IS '资产ID';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.operation_type IS '操作类型';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.from_department_id IS '变更前部门ID';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.to_department_id IS '变更后部门ID';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.from_user_id IS '变更前责任人ID';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.to_user_id IS '变更后责任人ID';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.from_location IS '变更前位置';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.to_location IS '变更后位置';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.operation_date IS '操作时间';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.reason IS '操作原因';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.maintenance_content IS '维修内容';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.maintenance_cost IS '维修费用';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.status IS '操作状态';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_asset_operation.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_supply_item IS '办公用品 SKU 库存台账表';
COMMENT ON COLUMN spectra_oa.oa_supply_item.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_supply_item.category IS '用品分类';
COMMENT ON COLUMN spectra_oa.oa_supply_item.sku IS 'SKU编码';
COMMENT ON COLUMN spectra_oa.oa_supply_item.name IS '用品名称';
COMMENT ON COLUMN spectra_oa.oa_supply_item.specification IS '规格型号';
COMMENT ON COLUMN spectra_oa.oa_supply_item.unit IS '计量单位';
COMMENT ON COLUMN spectra_oa.oa_supply_item.current_stock IS '当前库存数量';
COMMENT ON COLUMN spectra_oa.oa_supply_item.min_stock IS '最低库存数量';
COMMENT ON COLUMN spectra_oa.oa_supply_item.status IS '用品状态';
-- __V1_APPEND_39__
COMMENT ON COLUMN spectra_oa.oa_supply_item.supplier IS '供应商';
COMMENT ON COLUMN spectra_oa.oa_supply_item.location IS '存放位置';
COMMENT ON COLUMN spectra_oa.oa_supply_item.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_supply_item.remark IS '备注';
COMMENT ON COLUMN spectra_oa.oa_supply_item.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_supply_item.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_supply_item.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_supply_item.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_supply_item.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_supply_item.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_supply_operation IS '办公用品库存变动流水表';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.supply_id IS '办公用品ID';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.operation_type IS '操作类型（入库/领用/退库/调整）';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.quantity IS '变动数量';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.before_stock IS '变动前库存';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.after_stock IS '变动后库存';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.user_id IS '操作用户ID';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.location IS '库存位置';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.operation_date IS '操作时间';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.reason IS '变动原因';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.source_purchase_id IS '来源采购申请ID';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.source_receipt_id IS '来源收货单ID';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.source_purchase_item_id IS '来源采购明细ID';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.status IS '操作状态';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_supply_operation.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_purchase IS '采购申请执行表';
COMMENT ON COLUMN spectra_oa.oa_purchase.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_purchase.application_id IS '通用申请ID';
COMMENT ON COLUMN spectra_oa.oa_purchase.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_purchase.purpose IS '采购用途';
COMMENT ON COLUMN spectra_oa.oa_purchase.expected_date IS '预计采购日期';
COMMENT ON COLUMN spectra_oa.oa_purchase.budget_amount IS '预算金额';
COMMENT ON COLUMN spectra_oa.oa_purchase.currency IS '币种';
COMMENT ON COLUMN spectra_oa.oa_purchase.suggested_supplier IS '建议供应商';
COMMENT ON COLUMN spectra_oa.oa_purchase.execution_status IS '执行状态（NOT_STARTED/ORDERED/PARTIAL_RECEIVED/RECEIVED/CANCELLED）';
COMMENT ON COLUMN spectra_oa.oa_purchase.purchaser_id IS '采购经办人ID';
COMMENT ON COLUMN spectra_oa.oa_purchase.order_no IS '订单编号';
COMMENT ON COLUMN spectra_oa.oa_purchase.ordered_at IS '下单时间';
COMMENT ON COLUMN spectra_oa.oa_purchase.completed_at IS '执行完成时间';
COMMENT ON COLUMN spectra_oa.oa_purchase.execution_remark IS '执行备注';
COMMENT ON COLUMN spectra_oa.oa_purchase.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_purchase.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_purchase.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_purchase.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_purchase.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_purchase.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_purchase_item IS '采购申请明细表';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.purchase_id IS '采购申请ID';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.department_id IS '所属部门ID';
-- __V1_APPEND_40__
COMMENT ON COLUMN spectra_oa.oa_purchase_item.item_type IS '采购项类型';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.item_name IS '采购项名称';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.specification IS '规格型号';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.quantity IS '采购数量';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.estimated_unit_price IS '预计单价';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.estimated_amount IS '预计金额';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.purpose IS '明细用途';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.received_quantity IS '已收货数量';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_purchase_item.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_purchase_receipt IS '采购收货批次表';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.purchase_id IS '采购申请ID';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.receipt_no IS '收货单编号';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.received_date IS '收货时间';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.receiver_id IS '收货人ID';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.status IS '收货状态';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.remark IS '备注';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_purchase_receipt_item IS '采购收货明细表';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.receipt_id IS '收货批次ID';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.purchase_item_id IS '采购明细ID';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.quantity IS '本次收货数量';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.accepted IS '是否验收通过';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.difference_reason IS '验收差异原因';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_purchase_receipt_item.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_reimbursement IS '费用报销主表';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.application_id IS '通用申请ID';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.purpose IS '报销用途';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.expense_start IS '费用开始时间';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.expense_end IS '费用结束时间';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.total_amount IS '报销总金额';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.currency IS '币种';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.payee_name IS '收款人姓名';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.payee_account IS '收款账户';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.payment_status IS '付款状态';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.payment_at IS '付款时间';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.payment_remark IS '付款备注';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.created_at IS '创建时间';
-- __V1_APPEND_41__
COMMENT ON COLUMN spectra_oa.oa_reimbursement.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_reimbursement.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_oa.oa_reimbursement_item IS '费用报销明细表';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.id IS '主键ID';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.reimbursement_id IS '报销单ID';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.department_id IS '所属部门ID';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.expense_date IS '费用发生时间';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.category IS '费用类别';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.description IS '费用说明';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.amount IS '费用金额';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.tax_amount IS '税额';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.invoice_no IS '发票号码';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.created_by IS '创建人';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.created_at IS '创建时间';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_oa.oa_reimbursement_item.version IS '乐观锁版本号';
-- __V1_APPEND_42__
-- ============================================
-- spectra_ai schema 建表语句
-- RAG 向量存储（需要 pgvector 扩展）
-- ============================================

CREATE SCHEMA IF NOT EXISTS spectra_ai;

-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA spectra_ai;

-- RAG 知识分块表
CREATE TABLE spectra_ai.ai_knowledge_chunks (
    embedding_id  UUID PRIMARY KEY,
    embedding     spectra_ai.vector(1536),
    text          TEXT,
    metadata      JSONB
);
COMMENT ON TABLE spectra_ai.ai_knowledge_chunks IS 'RAG知识分块表';
COMMENT ON COLUMN spectra_ai.ai_knowledge_chunks.embedding_id IS '主键，向量ID';
COMMENT ON COLUMN spectra_ai.ai_knowledge_chunks.embedding IS '1536维向量（OpenAI text-embedding-3-small）';
COMMENT ON COLUMN spectra_ai.ai_knowledge_chunks.text IS '原始文本内容';
COMMENT ON COLUMN spectra_ai.ai_knowledge_chunks.metadata IS '元数据（来源、章节等）';

-- 向量索引（余弦相似度）
CREATE INDEX ai_knowledge_chunks_embedding_idx
    ON spectra_ai.ai_knowledge_chunks
    USING ivfflat (embedding spectra_ai.vector_cosine_ops)
    WITH (lists = '100');

-- 文本转向量函数
CREATE FUNCTION spectra_ai.cast_text_to_vector(text)
    RETURNS spectra_ai.vector
    LANGUAGE sql IMMUTABLE STRICT
    AS $$
        SELECT $1::spectra_ai.vector;
    $$;
-- __V1_APPEND_43__
-- ============================================
-- spectra_workflow 工作流模块建表语句
-- 共 2 张表（自定义表单）
-- ============================================

CREATE SCHEMA IF NOT EXISTS spectra_workflow;

-- 工作流-表单定义主表
-- 存储表单的元数据信息，每个表单一条记录
CREATE TABLE spectra_workflow.wf_form_definition (
    id              UUID PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    code            VARCHAR(100) NOT NULL UNIQUE,
    current_version INTEGER DEFAULT 1,
    active          BOOLEAN DEFAULT TRUE,
    description     TEXT,
    created_by      UUID,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by      UUID,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted         TIMESTAMP(6) WITH TIME ZONE,
    version         BIGINT DEFAULT 0
);

COMMENT ON TABLE spectra_workflow.wf_form_definition IS '表单定义表';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.id IS '主键ID';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.name IS '表单名称';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.code IS '表单编码（唯一，用于程序引用）';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.current_version IS '当前版本号';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.active IS '是否启用';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.description IS '描述';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.created_by IS '创建人';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.created_at IS '创建时间';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.updated_by IS '最后修改人';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.updated_at IS '最后修改时间';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.deleted IS '是否删除（null=未删除）';
COMMENT ON COLUMN spectra_workflow.wf_form_definition.version IS '乐观锁版本号';

-- 工作流-表单版本表
-- 每次保存表单设计器内容时生成新版本，旧版本保留用于历史追溯
CREATE TABLE spectra_workflow.wf_form_version (
    id                 UUID PRIMARY KEY,
    form_definition_id UUID NOT NULL,
    form_version       INTEGER NOT NULL,
    rule_json          TEXT,
    options_json       TEXT,
    form_json          TEXT,
    created_by         UUID,
    created_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by         UUID,
    updated_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted            TIMESTAMP(6) WITH TIME ZONE,
    version            BIGINT DEFAULT 0,
    UNIQUE(form_definition_id, form_version)
);

COMMENT ON TABLE spectra_workflow.wf_form_version IS '表单版本表';
COMMENT ON COLUMN spectra_workflow.wf_form_version.id IS '主键ID';
COMMENT ON COLUMN spectra_workflow.wf_form_version.form_definition_id IS '关联表单定义ID';
COMMENT ON COLUMN spectra_workflow.wf_form_version.form_version IS '版本号（同一表单下唯一）';
-- __V1_APPEND_44__
COMMENT ON COLUMN spectra_workflow.wf_form_version.rule_json IS 'form-create规则JSON（组件定义）';
COMMENT ON COLUMN spectra_workflow.wf_form_version.options_json IS 'form-create配置JSON（表单属性）';
COMMENT ON COLUMN spectra_workflow.wf_form_version.form_json IS 'form-create getJson()完整输出';
COMMENT ON COLUMN spectra_workflow.wf_form_version.created_by IS '创建人';
COMMENT ON COLUMN spectra_workflow.wf_form_version.created_at IS '创建时间';
COMMENT ON COLUMN spectra_workflow.wf_form_version.updated_by IS '最后修改人';
COMMENT ON COLUMN spectra_workflow.wf_form_version.updated_at IS '最后修改时间';
COMMENT ON COLUMN spectra_workflow.wf_form_version.deleted IS '是否删除（null=未删除）';
COMMENT ON COLUMN spectra_workflow.wf_form_version.version IS '乐观锁版本号';
-- __V1_APPEND_45__
-- 统一通知模块最终表结构。
-- 约束：通知表只允许引用通知域内部表；用户、部门和业务对象均为逻辑弱引用。

CREATE SCHEMA IF NOT EXISTS spectra_notification;

CREATE TABLE IF NOT EXISTS spectra_notification.ntf_template (
    id                    UUID NOT NULL,
    template_group_code   VARCHAR(100) NOT NULL,
    channel               VARCHAR(16) NOT NULL,
    purpose               VARCHAR(50) NOT NULL,
    version_no            INTEGER NOT NULL,
    title_template        TEXT,
    content_template      TEXT NOT NULL,
    html_template         TEXT,
    parameter_schema      JSONB NOT NULL DEFAULT '{}'::jsonb,
    provider_template_code VARCHAR(200),
    enabled               BOOLEAN NOT NULL DEFAULT TRUE,
    created_by            UUID,
    created_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by            UUID,
    updated_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted               TIMESTAMP(6) WITH TIME ZONE,
    version               BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT "PK_NTF_TEMPLATE" PRIMARY KEY (id),
    CONSTRAINT "CK_NTF_TEMPLATE_CHANNEL" CHECK (channel IN ('IN_APP', 'SMS', 'EMAIL'))
);

CREATE TABLE IF NOT EXISTS spectra_notification.ntf_request (
    id                              UUID NOT NULL,
    external_request_id             VARCHAR(100) NOT NULL,
    idempotency_key                 VARCHAR(200) NOT NULL,
    purpose                         VARCHAR(50) NOT NULL,
    template_group_code             VARCHAR(100) NOT NULL,
    source_module                   VARCHAR(50) NOT NULL,
    business_type                   VARCHAR(100) NOT NULL,
    business_id                     VARCHAR(100) NOT NULL,
    initiator_type                  VARCHAR(20) NOT NULL,
    initiator_user_id               UUID,
    source_department_id            UUID,
    parameters                      JSONB NOT NULL DEFAULT '{}'::jsonb,
    sensitive_parameters_ciphertext TEXT,
    encryption_key_id               VARCHAR(50),
    status                          VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED',
    recipient_count                 INTEGER NOT NULL DEFAULT 0,
    task_count                      INTEGER NOT NULL DEFAULT 0,
    scheduled_at                    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at                      TIMESTAMP(6) WITH TIME ZONE,
    priority                        SMALLINT NOT NULL DEFAULT 0,
    trace_id                        VARCHAR(100),
    created_by                      UUID,
    created_at                      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                      UUID,
    updated_at                      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                         TIMESTAMP(6) WITH TIME ZONE,
    version                         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT "PK_NTF_REQUEST" PRIMARY KEY (id),
    CONSTRAINT "CK_NTF_REQUEST_STATUS" CHECK (status IN ('ACCEPTED', 'DISPATCHING', 'SUCCEEDED', 'PARTIAL', 'FAILED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT "CK_NTF_REQUEST_COUNTS" CHECK (recipient_count >= 0 AND task_count >= 0)
);

-- __V1_APPEND_46__
CREATE TABLE IF NOT EXISTS spectra_notification.ntf_task (
    id                              UUID NOT NULL,
    notification_request_id         UUID NOT NULL,
    channel                         VARCHAR(16) NOT NULL,
    receiver_user_id                UUID,
    recipient_key_hash              VARCHAR(128) NOT NULL,
    recipient_masked                VARCHAR(200),
    recipient_ciphertext            TEXT,
    template_id                     UUID,
    purpose                         VARCHAR(50) NOT NULL,
    title                           TEXT NOT NULL,
    content                         TEXT NOT NULL,
    link                            VARCHAR(500),
    extra                           JSONB NOT NULL DEFAULT '{}'::jsonb,
    sensitive_parameters_ciphertext TEXT,
    priority                        SMALLINT NOT NULL DEFAULT 0,
    attempt_count                   INTEGER NOT NULL DEFAULT 0,
    max_attempts                    INTEGER NOT NULL DEFAULT 3,
    scheduled_at                    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    next_retry_at                   TIMESTAMP(6) WITH TIME ZONE,
    expires_at                      TIMESTAMP(6) WITH TIME ZONE,
    locked_by                       VARCHAR(100),
    locked_at                       TIMESTAMP(6) WITH TIME ZONE,
    status                          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_error_code                 VARCHAR(100),
    created_by                      UUID,
    created_at                      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                      UUID,
    updated_at                      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                         TIMESTAMP(6) WITH TIME ZONE,
    version                         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT "PK_NTF_TASK" PRIMARY KEY (id),
    CONSTRAINT "FK_NTF_TASK_REQUEST" FOREIGN KEY (notification_request_id)
        REFERENCES spectra_notification.ntf_request (id),
    CONSTRAINT "FK_NTF_TASK_TEMPLATE" FOREIGN KEY (template_id)
        REFERENCES spectra_notification.ntf_template (id),
    CONSTRAINT "CK_NTF_TASK_CHANNEL" CHECK (channel IN ('IN_APP', 'SMS', 'EMAIL')),
    CONSTRAINT "CK_NTF_TASK_STATUS" CHECK (status IN ('PENDING', 'PROCESSING', 'RETRYING', 'SENT', 'FAILED', 'BLOCKED', 'UNKNOWN', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT "CK_NTF_TASK_ATTEMPTS" CHECK (attempt_count >= 0 AND max_attempts > 0),
    CONSTRAINT "CK_NTF_TASK_RECEIVER" CHECK (channel <> 'IN_APP' OR receiver_user_id IS NOT NULL),
    CONSTRAINT "CK_NTF_TASK_ADDRESS" CHECK (channel = 'IN_APP' OR (recipient_masked IS NOT NULL AND recipient_ciphertext IS NOT NULL))
);

CREATE TABLE IF NOT EXISTS spectra_notification.ntf_delivery (
    id                      UUID NOT NULL,
    notification_task_id    UUID NOT NULL,
    attempt_no              INTEGER NOT NULL,
    provider                VARCHAR(50) NOT NULL,
    provider_message_id     VARCHAR(200),
    started_at              TIMESTAMP(6) WITH TIME ZONE,
    completed_at            TIMESTAMP(6) WITH TIME ZONE,
    result_status           VARCHAR(20) NOT NULL,
    error_code              VARCHAR(100),
    error_message_sanitized TEXT,
    duration_ms             BIGINT,
    response_summary        JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_by              UUID,
    created_at              TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              UUID,
    updated_at              TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
-- __V1_APPEND_47__
    deleted                 TIMESTAMP(6) WITH TIME ZONE,
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT "PK_NTF_DELIVERY" PRIMARY KEY (id),
    CONSTRAINT "FK_NTF_DELIVERY_TASK" FOREIGN KEY (notification_task_id)
        REFERENCES spectra_notification.ntf_task (id),
    CONSTRAINT "CK_NTF_DELIVERY_ATTEMPT" CHECK (attempt_no > 0),
    CONSTRAINT "CK_NTF_DELIVERY_STATUS" CHECK (result_status IN ('ACCEPTED', 'SENT', 'FAILED', 'BLOCKED', 'UNKNOWN'))
);

CREATE TABLE IF NOT EXISTS spectra_notification.ntf_inbox_message (
    id                       UUID NOT NULL,
    notification_task_id     UUID,
    notification_request_id  UUID,
    receiver_user_id         UUID NOT NULL,
    purpose                  VARCHAR(50) NOT NULL,
    title                    VARCHAR(255) NOT NULL,
    content                  TEXT NOT NULL,
    sender_user_id           UUID,
    sender_name              VARCHAR(100),
    link                     VARCHAR(500),
    is_read                  BOOLEAN NOT NULL DEFAULT FALSE,
    read_at                  TIMESTAMP(6) WITH TIME ZONE,
    extra                    JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_by               UUID,
    created_at               TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by               UUID,
    updated_at               TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                  TIMESTAMP(6) WITH TIME ZONE,
    version                  BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT "PK_NTF_INBOX_MESSAGE" PRIMARY KEY (id),
    CONSTRAINT "FK_NTF_INBOX_TASK" FOREIGN KEY (notification_task_id)
        REFERENCES spectra_notification.ntf_task (id),
    CONSTRAINT "FK_NTF_INBOX_REQUEST" FOREIGN KEY (notification_request_id)
        REFERENCES spectra_notification.ntf_request (id),
    CONSTRAINT "CK_NTF_INBOX_READ" CHECK ((is_read AND read_at IS NOT NULL) OR (NOT is_read AND read_at IS NULL))
);

CREATE TABLE IF NOT EXISTS spectra_notification.ntf_user_preference (
    id                    UUID NOT NULL,
    user_id               UUID NOT NULL,
    purpose               VARCHAR(50) NOT NULL,
    channel               VARCHAR(16) NOT NULL,
    enabled               BOOLEAN NOT NULL DEFAULT TRUE,
    do_not_disturb        BOOLEAN NOT NULL DEFAULT FALSE,
    do_not_disturb_start TIMESTAMP(6) WITH TIME ZONE,
    do_not_disturb_end   TIMESTAMP(6) WITH TIME ZONE,
    created_by            UUID,
    created_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by            UUID,
    updated_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted               TIMESTAMP(6) WITH TIME ZONE,
    version               BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT "PK_NTF_USER_PREFERENCE" PRIMARY KEY (id),
    CONSTRAINT "CK_NTF_USER_PREFERENCE_CHANNEL" CHECK (channel IN ('IN_APP', 'SMS', 'EMAIL'))
);

CREATE UNIQUE INDEX IF NOT EXISTS "UK_NTF_TEMPLATE_VERSION"
    ON spectra_notification.ntf_template (template_group_code, channel, version_no)
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "UK_NTF_TEMPLATE_ENABLED"
-- __V1_APPEND_48__
    ON spectra_notification.ntf_template (template_group_code, channel)
    WHERE enabled = TRUE AND deleted IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "UK_NTF_REQUEST_EXTERNAL_ID"
    ON spectra_notification.ntf_request (external_request_id)
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "UK_NTF_REQUEST_IDEMPOTENCY"
    ON spectra_notification.ntf_request (idempotency_key)
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "UK_NTF_TASK_RECIPIENT_CHANNEL"
    ON spectra_notification.ntf_task (notification_request_id, recipient_key_hash, channel)
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "UK_NTF_DELIVERY_ATTEMPT"
    ON spectra_notification.ntf_delivery (notification_task_id, attempt_no)
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "UK_NTF_INBOX_TASK"
    ON spectra_notification.ntf_inbox_message (notification_task_id)
    WHERE notification_task_id IS NOT NULL AND deleted IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "UK_NTF_USER_PREFERENCE"
    ON spectra_notification.ntf_user_preference (user_id, purpose, channel)
    WHERE deleted IS NULL;

CREATE INDEX IF NOT EXISTS "IDX_NTF_TASK_PENDING"
    ON spectra_notification.ntf_task (status, next_retry_at, priority DESC, created_at)
    WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS "IDX_NTF_TASK_EXPIRES_AT"
    ON spectra_notification.ntf_task (expires_at)
    WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS "IDX_NTF_TASK_RECEIVER_CREATED"
    ON spectra_notification.ntf_task (receiver_user_id, created_at DESC)
    WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS "IDX_NTF_DELIVERY_TASK_CREATED"
    ON spectra_notification.ntf_delivery (notification_task_id, created_at DESC)
    WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS "IDX_NTF_INBOX_RECEIVER_CREATED"
    ON spectra_notification.ntf_inbox_message (receiver_user_id, created_at DESC)
    WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS "IDX_NTF_INBOX_RECEIVER_UNREAD"
    ON spectra_notification.ntf_inbox_message (receiver_user_id, created_at DESC)
    WHERE deleted IS NULL AND is_read = FALSE;
CREATE INDEX IF NOT EXISTS "IDX_NTF_USER_PREFERENCE_USER"
    ON spectra_notification.ntf_user_preference (user_id)
    WHERE deleted IS NULL;

COMMENT ON TABLE spectra_notification.ntf_template IS '通知模板版本表';
COMMENT ON TABLE spectra_notification.ntf_request IS '逻辑通知请求表';
COMMENT ON TABLE spectra_notification.ntf_task IS '接收人和渠道维度的通知任务表';
COMMENT ON TABLE spectra_notification.ntf_delivery IS '通知渠道单次投递尝试审计表';
COMMENT ON TABLE spectra_notification.ntf_inbox_message IS '当前用户站内信收件箱表';
COMMENT ON TABLE spectra_notification.ntf_user_preference IS '用户用途和渠道偏好表';

COMMENT ON COLUMN spectra_notification.ntf_template.id IS '主键ID';
COMMENT ON COLUMN spectra_notification.ntf_template.template_group_code IS '逻辑模板组编码';
COMMENT ON COLUMN spectra_notification.ntf_template.channel IS '投递渠道：IN_APP、SMS或EMAIL';
COMMENT ON COLUMN spectra_notification.ntf_template.purpose IS '通知用途';
COMMENT ON COLUMN spectra_notification.ntf_template.version_no IS '模板版本号';
COMMENT ON COLUMN spectra_notification.ntf_template.title_template IS '标题模板；无需标题时可为空';
COMMENT ON COLUMN spectra_notification.ntf_template.content_template IS '正文模板';
COMMENT ON COLUMN spectra_notification.ntf_template.html_template IS 'HTML正文模板；非HTML渠道可为空';
COMMENT ON COLUMN spectra_notification.ntf_template.parameter_schema IS '模板参数JSON Schema';
COMMENT ON COLUMN spectra_notification.ntf_template.provider_template_code IS '渠道供应商模板编码';
-- __V1_APPEND_49__
COMMENT ON COLUMN spectra_notification.ntf_template.enabled IS '是否启用';
COMMENT ON COLUMN spectra_notification.ntf_template.created_by IS '创建人ID';
COMMENT ON COLUMN spectra_notification.ntf_template.created_at IS '创建时间';
COMMENT ON COLUMN spectra_notification.ntf_template.updated_by IS '最后更新人ID';
COMMENT ON COLUMN spectra_notification.ntf_template.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_notification.ntf_template.deleted IS '删除时间；NULL表示未删除';
COMMENT ON COLUMN spectra_notification.ntf_template.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_notification.ntf_request.id IS '主键ID';
COMMENT ON COLUMN spectra_notification.ntf_request.external_request_id IS '调用方生成的外部请求ID';
COMMENT ON COLUMN spectra_notification.ntf_request.idempotency_key IS '业务幂等键；通知域内唯一';
COMMENT ON COLUMN spectra_notification.ntf_request.purpose IS '通知用途';
COMMENT ON COLUMN spectra_notification.ntf_request.template_group_code IS '逻辑模板组编码';
COMMENT ON COLUMN spectra_notification.ntf_request.source_module IS '发起请求的业务模块编码';
COMMENT ON COLUMN spectra_notification.ntf_request.business_type IS '业务对象类型；与business_id构成弱引用';
COMMENT ON COLUMN spectra_notification.ntf_request.business_id IS '业务对象ID；不建立跨模块外键';
COMMENT ON COLUMN spectra_notification.ntf_request.initiator_type IS '发起方类型：用户或系统';
COMMENT ON COLUMN spectra_notification.ntf_request.initiator_user_id IS '发起用户ID；系统发起时可为空';
COMMENT ON COLUMN spectra_notification.ntf_request.source_department_id IS '发起请求时的来源部门ID';
COMMENT ON COLUMN spectra_notification.ntf_request.parameters IS '可记录和持久化的非敏感模板参数';
COMMENT ON COLUMN spectra_notification.ntf_request.sensitive_parameters_ciphertext IS '加密后的敏感模板参数';
COMMENT ON COLUMN spectra_notification.ntf_request.encryption_key_id IS '敏感参数使用的加密密钥标识';
COMMENT ON COLUMN spectra_notification.ntf_request.status IS '逻辑通知请求状态';
COMMENT ON COLUMN spectra_notification.ntf_request.recipient_count IS '请求展开后的接收人数';
COMMENT ON COLUMN spectra_notification.ntf_request.task_count IS '请求展开后的投递任务数';
COMMENT ON COLUMN spectra_notification.ntf_request.scheduled_at IS '计划开始投递时间';
COMMENT ON COLUMN spectra_notification.ntf_request.expires_at IS '投递截止时间；为空表示不过期';
COMMENT ON COLUMN spectra_notification.ntf_request.priority IS '任务优先级；数值越大越优先';
COMMENT ON COLUMN spectra_notification.ntf_request.trace_id IS '调用链追踪ID';
COMMENT ON COLUMN spectra_notification.ntf_request.created_by IS '创建人ID';
COMMENT ON COLUMN spectra_notification.ntf_request.created_at IS '创建时间';
COMMENT ON COLUMN spectra_notification.ntf_request.updated_by IS '最后更新人ID';
COMMENT ON COLUMN spectra_notification.ntf_request.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_notification.ntf_request.deleted IS '删除时间；NULL表示未删除';
COMMENT ON COLUMN spectra_notification.ntf_request.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_notification.ntf_task.id IS '主键ID';
COMMENT ON COLUMN spectra_notification.ntf_task.notification_request_id IS '所属逻辑通知请求ID';
COMMENT ON COLUMN spectra_notification.ntf_task.channel IS '投递渠道：IN_APP、SMS或EMAIL';
COMMENT ON COLUMN spectra_notification.ntf_task.receiver_user_id IS '接收用户ID；直接地址投递时可为空';
COMMENT ON COLUMN spectra_notification.ntf_task.recipient_key_hash IS '接收人稳定哈希；用于任务幂等';
COMMENT ON COLUMN spectra_notification.ntf_task.recipient_masked IS '脱敏后的外部接收地址';
COMMENT ON COLUMN spectra_notification.ntf_task.recipient_ciphertext IS '加密后的外部接收地址';
COMMENT ON COLUMN spectra_notification.ntf_task.template_id IS '锁定的通知模板版本ID';
COMMENT ON COLUMN spectra_notification.ntf_task.purpose IS '通知用途';
COMMENT ON COLUMN spectra_notification.ntf_task.title IS '渲染后的通知标题快照';
COMMENT ON COLUMN spectra_notification.ntf_task.content IS '渲染后的通知正文快照';
COMMENT ON COLUMN spectra_notification.ntf_task.link IS '客户端站内跳转路径';
COMMENT ON COLUMN spectra_notification.ntf_task.extra IS '非敏感扩展参数';
COMMENT ON COLUMN spectra_notification.ntf_task.sensitive_parameters_ciphertext IS '加密后的敏感渲染载荷';
COMMENT ON COLUMN spectra_notification.ntf_task.priority IS '任务优先级；数值越大越优先';
COMMENT ON COLUMN spectra_notification.ntf_task.attempt_count IS '已尝试投递次数';
COMMENT ON COLUMN spectra_notification.ntf_task.max_attempts IS '最大允许投递次数';
COMMENT ON COLUMN spectra_notification.ntf_task.scheduled_at IS '计划投递时间';
COMMENT ON COLUMN spectra_notification.ntf_task.next_retry_at IS '下次重试时间';
COMMENT ON COLUMN spectra_notification.ntf_task.expires_at IS '任务过期时间；为空表示不过期';
COMMENT ON COLUMN spectra_notification.ntf_task.locked_by IS '领取任务的Worker标识';
COMMENT ON COLUMN spectra_notification.ntf_task.locked_at IS 'Worker领取任务的时间';
COMMENT ON COLUMN spectra_notification.ntf_task.status IS '通知任务状态';
COMMENT ON COLUMN spectra_notification.ntf_task.last_error_code IS '最后一次投递错误码';
-- __V1_APPEND_50__
COMMENT ON COLUMN spectra_notification.ntf_task.created_by IS '创建人ID';
COMMENT ON COLUMN spectra_notification.ntf_task.created_at IS '创建时间';
COMMENT ON COLUMN spectra_notification.ntf_task.updated_by IS '最后更新人ID';
COMMENT ON COLUMN spectra_notification.ntf_task.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_notification.ntf_task.deleted IS '删除时间；NULL表示未删除';
COMMENT ON COLUMN spectra_notification.ntf_task.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_notification.ntf_delivery.id IS '主键ID';
COMMENT ON COLUMN spectra_notification.ntf_delivery.notification_task_id IS '所属通知任务ID';
COMMENT ON COLUMN spectra_notification.ntf_delivery.attempt_no IS '当前任务的投递尝试序号';
COMMENT ON COLUMN spectra_notification.ntf_delivery.provider IS '执行投递的渠道供应商编码';
COMMENT ON COLUMN spectra_notification.ntf_delivery.provider_message_id IS '供应商返回的消息ID';
COMMENT ON COLUMN spectra_notification.ntf_delivery.started_at IS '本次投递开始时间';
COMMENT ON COLUMN spectra_notification.ntf_delivery.completed_at IS '本次投递完成时间';
COMMENT ON COLUMN spectra_notification.ntf_delivery.result_status IS '标准化投递结果状态';
COMMENT ON COLUMN spectra_notification.ntf_delivery.error_code IS '供应商或模块返回的错误码';
COMMENT ON COLUMN spectra_notification.ntf_delivery.error_message_sanitized IS '脱敏后的错误信息';
COMMENT ON COLUMN spectra_notification.ntf_delivery.duration_ms IS '本次投递耗时；单位为毫秒';
COMMENT ON COLUMN spectra_notification.ntf_delivery.response_summary IS '可安全持久化的脱敏响应摘要';
COMMENT ON COLUMN spectra_notification.ntf_delivery.created_by IS '创建人ID';
COMMENT ON COLUMN spectra_notification.ntf_delivery.created_at IS '创建时间';
COMMENT ON COLUMN spectra_notification.ntf_delivery.updated_by IS '最后更新人ID';
COMMENT ON COLUMN spectra_notification.ntf_delivery.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_notification.ntf_delivery.deleted IS '删除时间；NULL表示未删除';
COMMENT ON COLUMN spectra_notification.ntf_delivery.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_notification.ntf_inbox_message.id IS '主键ID';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.notification_task_id IS '对应的站内信通知任务ID';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.notification_request_id IS '对应的逻辑通知请求ID';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.receiver_user_id IS '消息接收用户ID';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.purpose IS '通知用途';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.title IS '消息标题';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.content IS '消息正文';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.sender_user_id IS '消息发送用户ID；系统发送时可为空';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.sender_name IS '消息发送人名称快照';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.link IS '客户端站内跳转路径';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.is_read IS '是否已读';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.read_at IS '阅读时间；未读时为空';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.extra IS '白名单扩展信息';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.created_by IS '创建人ID';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.created_at IS '创建时间';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.updated_by IS '最后更新人ID';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.deleted IS '删除时间；NULL表示未删除';
COMMENT ON COLUMN spectra_notification.ntf_inbox_message.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_notification.ntf_user_preference.id IS '主键ID';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.user_id IS '用户ID';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.purpose IS '通知用途';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.channel IS '投递渠道：IN_APP、SMS或EMAIL';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.enabled IS '是否允许该用途通过该渠道投递';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.do_not_disturb IS '是否启用免打扰';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.do_not_disturb_start IS '免打扰开始时间';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.do_not_disturb_end IS '免打扰结束时间';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.created_by IS '创建人ID';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.created_at IS '创建时间';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.updated_by IS '最后更新人ID';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.deleted IS '删除时间；NULL表示未删除';
COMMENT ON COLUMN spectra_notification.ntf_user_preference.version IS '乐观锁版本号';
-- __V1_APPEND_51__
-- End of immutable target V1. All subsequent physical changes must use V2+.
