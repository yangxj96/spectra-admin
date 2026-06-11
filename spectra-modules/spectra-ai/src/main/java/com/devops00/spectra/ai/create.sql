-- 会仪表-主表
DROP TABLE IF EXISTS "spectra_core"."ai_session";
CREATE TABLE "spectra_core"."ai_session"
(
    ------------- 主键字段
    "id"         uuid           NOT NULL,
    ------------- 业务字段
    session_id   VARCHAR(255)   NOT NULL,
    state_key    VARCHAR(255)   NOT NULL,
    item_index   INT            NOT NULL DEFAULT 0,
    state_data   TEXT           NOT NULL,
    ------------- 审计字段
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8                    DEFAULT 0
);
COMMENT ON TABLE "spectra_core"."ai_session" IS 'AI-Agent会话状态存储表';
------------- 主键字段
COMMENT ON COLUMN "spectra_core"."ai_session"."id" IS '主键ID';
------------- 审计字段
COMMENT ON COLUMN "spectra_core"."ai_session"."created_by" IS '创建人';
COMMENT ON COLUMN "spectra_core"."ai_session"."created_at" IS '创建时间';
COMMENT ON COLUMN "spectra_core"."ai_session"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."ai_session"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."ai_session"."deleted" IS '删除标识';
COMMENT ON COLUMN "spectra_core"."ai_session"."version" IS '乐观锁';
------------- 业务字段
COMMENT ON COLUMN "spectra_core"."ai_session"."session_id" IS 'session id';
COMMENT ON COLUMN "spectra_core"."ai_session"."state_key" IS 'state key';
COMMENT ON COLUMN "spectra_core"."ai_session"."item_index" IS 'item_index';
COMMENT ON COLUMN "spectra_core"."ai_session"."state_data" IS 'state_data';
------------- 约束
ALTER TABLE "spectra_core"."ai_session"
    ADD CONSTRAINT "ai_session_pkey" PRIMARY KEY ("id");
------------- 联合主键约束 (session_id, state_key, item_index)
ALTER TABLE "spectra_core"."ai_session"
    ADD CONSTRAINT "ai_session_uk_key"
        UNIQUE ("session_id", "state_key", "item_index");
-- session_id 索引
CREATE INDEX "idx_ai_session_sid" ON "spectra_core"."ai_session" ("session_id");