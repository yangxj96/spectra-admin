/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

-- 文件上传-主表
DROP TABLE IF EXISTS "spectra_core"."file_info";
CREATE TABLE "spectra_core"."file_info"
(
    ------------- 主键字段
    "id"            uuid           NOT NULL,
    ------------- 业务字段
    "filename"      VARCHAR(255)   NOT NULL,
    "original_name" VARCHAR(255),
    "content_type"  VARCHAR(100),
    "size"          int8           NOT NULL,
    "hash"          VARCHAR(64)    NOT NULL,
    "storage_type"  VARCHAR(20)    NOT NULL,
    "status"        VARCHAR(20)    NOT NULL,
    "ref_count"     int4 DEFAULT 1,
    ------------- 审计字段
    "created_by"    uuid,
    "created_at"    timestamptz(6) NOT NULL,
    "updated_by"    uuid,
    "updated_at"    timestamptz(6) NOT NULL,
    "deleted"       timestamptz(6),
    "version"       int8 DEFAULT 0
);
COMMENT ON TABLE "spectra_core"."file_info" IS '文件上传-主表';
------------- 约束
ALTER TABLE "spectra_core"."file_info"
    ADD CONSTRAINT "file_file_info_pkey" PRIMARY KEY ("id");
------------- 主键字段
COMMENT ON COLUMN "spectra_core"."file_info"."id" IS '主键ID';
------------- 审计字段
COMMENT ON COLUMN "spectra_core"."file_info"."created_by" IS '创建人';
COMMENT ON COLUMN "spectra_core"."file_info"."created_at" IS '创建时间';
COMMENT ON COLUMN "spectra_core"."file_info"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."file_info"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."file_info"."deleted" IS '删除标识';
COMMENT ON COLUMN "spectra_core"."file_info"."version" IS '乐观锁';
------------- 业务字段
COMMENT ON COLUMN "spectra_core"."file_info"."filename" IS '存储文件名(系统生成)';
COMMENT ON COLUMN "spectra_core"."file_info"."original_name" IS '原始文件名';
COMMENT ON COLUMN "spectra_core"."file_info"."content_type" IS '文件类型(MIME)';
COMMENT ON COLUMN "spectra_core"."file_info"."size" IS '文件大小(字节)';
COMMENT ON COLUMN "spectra_core"."file_info"."hash" IS '文件哈希(MD5/SHA256，用于秒传)';
COMMENT ON COLUMN "spectra_core"."file_info"."storage_type" IS '存储类型(LOCAL/S3/OSS)';
COMMENT ON COLUMN "spectra_core"."file_info"."status" IS '文件状态(ACTIVE/DELETED)';
COMMENT ON COLUMN "spectra_core"."file_info"."ref_count" IS '引用计数(用于秒传共享文件)';

-- 文件上传-上传任务表
DROP TABLE IF EXISTS "spectra_core"."file_upload_task";
CREATE TABLE "spectra_core"."file_upload_task"
(
    ------------- 主键
    "id"           uuid           NOT NULL,
    ------------- 业务字段
    "upload_id"    VARCHAR(64)    NOT NULL, -- 给前端的ID
    "filename"     VARCHAR(255),
    "hash"         VARCHAR(64),
    "size"         int8,
    "chunk_size"   int8,
    "total_chunks" int4,
    "storage_type" VARCHAR(20)    NOT NULL,
    "status"       VARCHAR(20)    NOT NULL,
    "eid"          VARCHAR(255),
    "file_id"      uuid,
    ------------- 审计字段
    "created_by"   uuid,
    "created_at"   timestamptz(6) NOT NULL,
    "updated_by"   uuid,
    "updated_at"   timestamptz(6) NOT NULL,
    "deleted"      timestamptz(6),
    "version"      int8 DEFAULT 0
);
COMMENT ON TABLE "spectra_core"."file_upload_task" IS '文件上传-上传任务表';
------------- 约束
ALTER TABLE "spectra_core"."file_upload_task"
    ADD CONSTRAINT "file_upload_task_pkey" PRIMARY KEY ("id");
CREATE UNIQUE INDEX "uk_upload_id"
    ON "spectra_core"."file_upload_task" ("upload_id");
------------- 主键字段
COMMENT ON COLUMN "spectra_core"."file_upload_task"."id" IS '主键ID';
------------- 审计字段
COMMENT ON COLUMN "spectra_core"."file_upload_task"."created_by" IS '创建人';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."created_at" IS '创建时间';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."deleted" IS '删除标识';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."version" IS '乐观锁';
------------- 业务字段
COMMENT ON COLUMN "spectra_core"."file_upload_task"."upload_id" IS '上传任务ID(前端使用的唯一标识)';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."filename" IS '文件名';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."hash" IS '文件哈希(用于秒传判断)';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."size" IS '文件总大小(字节)';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."chunk_size" IS '分片大小(字节)';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."total_chunks" IS '总分片数';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."storage_type" IS '存储类型(LOCAL/S3/OSS)';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."status" IS '上传状态(INIT(初始化)/UPLOADING(上传中)/MERGING(合并中)/DONE(完成)/FAILED(失败))';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."eid" IS 's3协议相关ID';
COMMENT ON COLUMN "spectra_core"."file_upload_task"."file_id" IS '关联文件ID(上传完成后生成)';
------------- 索引说明
COMMENT ON INDEX "spectra_core"."uk_upload_id" IS '上传任务ID唯一索引';

-- 文件上传-上传分片表
DROP TABLE IF EXISTS "spectra_core"."file_upload_chunk";
CREATE TABLE "spectra_core"."file_upload_chunk"
(
    ------------- 主键字段
    "id"           uuid           NOT NULL,
    ------------- 业务字段
    "upload_id"    VARCHAR(64)    NOT NULL,
    "chunk_number" int4           NOT NULL,
    "etag"         VARCHAR(128), -- S3 / OSS 必须
    "size"         int8,
    ------------- 审计字段
    "created_by"   uuid,
    "created_at"   timestamptz(6) NOT NULL,
    "updated_by"   uuid,
    "updated_at"   timestamptz(6) NOT NULL,
    "deleted"      timestamptz(6),
    "version"      int8 DEFAULT 0
);
COMMENT ON TABLE "spectra_core"."file_upload_chunk" IS '文件上传-上传分片表';
------------- 约束
ALTER TABLE "spectra_core"."file_upload_chunk"
    ADD CONSTRAINT "file_upload_chunk_pkey" PRIMARY KEY ("id");
CREATE UNIQUE INDEX "uk_upload_chunk"
    ON "spectra_core"."file_upload_chunk" ("upload_id", "chunk_number");
------------- 主键字段
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."id" IS '主键ID';
------------- 审计字段
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."created_by" IS '创建人';
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."created_at" IS '创建时间';
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."deleted" IS '删除标识';
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."version" IS '乐观锁';
------------- 业务字段
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."upload_id" IS '上传任务ID';
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."chunk_number" IS '分片序号(从1开始)';
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."etag" IS '分片标识(用于S3/OSS合并)';
COMMENT ON COLUMN "spectra_core"."file_upload_chunk"."size" IS '分片大小(字节)';
------------- 索引说明
COMMENT ON INDEX "spectra_core"."uk_upload_chunk" IS '上传任务ID+分片序号唯一索引(保证幂等)';

-- 文件类型表
DROP TABLE IF EXISTS "spectra_core"."file_type";
CREATE TABLE "spectra_core"."file_type"
(
    id             UUID PRIMARY KEY            NOT NULL,               -- 主键ID
    name           CHARACTER VARYING(100)      NOT NULL,               -- 文件类型名称
    extension      jsonb                       NOT NULL,               -- 文件后缀(带.)
    mime           jsonb                       NOT NULL,               -- 文件mime
    magic_rules    jsonb,                                              -- 文件魔数规则
    max_size       BIGINT                      NOT NULL,               -- 最大文件大小(单位:bytes)
    previewable    BOOLEAN                     NOT NULL DEFAULT FALSE, -- 是否允许预览
    allowed_upload BOOLEAN                     NOT NULL DEFAULT TRUE,  -- 允许上传
    dangerous      BOOLEAN                     NOT NULL DEFAULT FALSE, -- 是否危险类型
    remark         TEXT,                                               -- 备注
    created_by     UUID,                                               -- 创建人
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,               -- 创建时间
    updated_by     UUID,                                               -- 最后更新人
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,               -- 最后更新时间
    deleted        TIMESTAMP(6) WITH TIME ZONE,                        -- 删除标识
    version        BIGINT                               DEFAULT 0      -- 乐观锁
);
CREATE INDEX "idx_file_type_extension" ON "spectra_core"."file_type" USING GIN (extension);
CREATE INDEX "idx_file_type_mime" ON "spectra_core"."file_type" USING GIN (mime);
CREATE INDEX "idx_file_type_not_deleted" ON "spectra_core"."file_type" USING BTREE (deleted) WHERE (deleted IS NULL);
COMMENT ON TABLE "spectra_core"."file_type" IS '文件-文件类型';
COMMENT ON COLUMN "spectra_core"."file_type".id IS '主键ID';
COMMENT ON COLUMN "spectra_core"."file_type".name IS '文件类型名称';
COMMENT ON COLUMN "spectra_core"."file_type".extension IS '文件后缀(带.)';
COMMENT ON COLUMN "spectra_core"."file_type".mime IS '文件mime';
COMMENT ON COLUMN "spectra_core"."file_type".magic_rules IS '文件魔数规则';
COMMENT ON COLUMN "spectra_core"."file_type".max_size IS '最大文件大小(单位:bytes)';
COMMENT ON COLUMN "spectra_core"."file_type".previewable IS '是否允许预览';
COMMENT ON COLUMN "spectra_core"."file_type".allowed_upload IS '允许上传';
COMMENT ON COLUMN "spectra_core"."file_type".dangerous IS '是否危险类型';
COMMENT ON COLUMN "spectra_core"."file_type".remark IS '备注';
COMMENT ON COLUMN "spectra_core"."file_type".created_by IS '创建人';
COMMENT ON COLUMN "spectra_core"."file_type".created_at IS '创建时间';
COMMENT ON COLUMN "spectra_core"."file_type".updated_by IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."file_type".updated_at IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."file_type".deleted IS '删除标识';
COMMENT ON COLUMN "spectra_core"."file_type".version IS '乐观锁';
