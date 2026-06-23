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

-- 会仪表-主表
DROP TABLE IF EXISTS "spectra_core"."oa_meeting";
CREATE TABLE "spectra_core"."oa_meeting"
(
    ------------- 主键字段
    "id"                  uuid           NOT NULL,
    ------------- 业务字段
    "title"               VARCHAR(255)   NOT NULL,
    "initiator_id"        uuid           NOT NULL,
    "start_time"          timestamptz(6) NOT NULL,
    "end_time"            timestamptz(6) NOT NULL,
    "location"            VARCHAR(255),
    "content"             TEXT,
    "status"              VARCHAR(32)    NOT NULL DEFAULT 'draft',
    "process_instance_id" VARCHAR(64),
    "approval_status"     VARCHAR(32)    NOT NULL DEFAULT 'draft',
    ------------- 审计字段
    "created_by"          uuid,
    "created_at"          timestamptz(6) NOT NULL,
    "updated_by"          uuid,
    "updated_at"          timestamptz(6) NOT NULL,
    "deleted"             timestamptz(6),
    "version"             int8                    DEFAULT 0
);
COMMENT ON TABLE "spectra_core"."oa_meeting" IS 'OA-会议-主表';
------------- 主键字段
COMMENT ON COLUMN "spectra_core"."oa_meeting"."id" IS '主键ID';
------------- 审计字段
COMMENT ON COLUMN "spectra_core"."oa_meeting"."created_by" IS '创建人';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."created_at" IS '创建时间';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."deleted" IS '删除标识';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."version" IS '乐观锁';
------------- 业务字段
COMMENT ON COLUMN "spectra_core"."oa_meeting"."title" IS '会议标题';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."initiator_id" IS '发起人';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."start_time" IS '开始时间';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."end_time" IS '结束时间';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."location" IS '会议地点';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."content" IS '会议内容/议题';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."status" IS '会议业务状态';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."process_instance_id" IS '工作流审核实例ID';
COMMENT ON COLUMN "spectra_core"."oa_meeting"."approval_status" IS '工作流审核状态';
------------- 约束
ALTER TABLE "spectra_core"."oa_meeting"
    ADD CONSTRAINT "oa_meeting_pkey" PRIMARY KEY ("id");


-- 会仪表-参会人表
DROP TABLE IF EXISTS "spectra_core"."oa_meeting_participant";
CREATE TABLE "spectra_core"."oa_meeting_participant"
(
    ------------- 主键字段
    "id"          uuid           NOT NULL,
    ------------- 业务字段
    "meeting_id"  uuid           NOT NULL,
    "user_id"     uuid           NOT NULL,
    "role"        VARCHAR(32) DEFAULT 'attendee',
    "status"      VARCHAR(32) DEFAULT 'pending',
    "check_in_at" timestamptz(6),
    ------------- 审计字段
    "created_by"  uuid,
    "created_at"  timestamptz(6) NOT NULL,
    "updated_by"  uuid,
    "updated_at"  timestamptz(6) NOT NULL,
    "deleted"     timestamptz(6),
    "version"     int8        DEFAULT 0
);
COMMENT ON TABLE "spectra_core"."oa_meeting_participant" IS 'OA-会议-参会人表';
------------- 主键字段
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."id" IS '主键ID';
------------- 审计字段
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."created_by" IS '创建人';
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."created_at" IS '创建时间';
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."deleted" IS '删除标识';
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."version" IS '乐观锁';
------------- 业务字段
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."meeting_id" IS '会议ID';
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."user_id" IS '参会人ID';
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."role" IS '角色,host-主持人;attendee-参会人;optional-可选参会人';
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."status" IS '状态,pending-未响应;accepted-已接受;declined-已拒绝;checked_in-已签到';
COMMENT ON COLUMN "spectra_core"."oa_meeting_participant"."check_in_at" IS '是否确认/签到';

------------- 约束
ALTER TABLE "spectra_core"."oa_meeting_participant"
    ADD CONSTRAINT "oa_meeting_participant_pkey" PRIMARY KEY ("id");


-- 会仪表-会议纪要
DROP TABLE IF EXISTS "spectra_core"."oa_meeting_record";
CREATE TABLE "spectra_core"."oa_meeting_record"
(
    ------------- 主键字段
    "id"         uuid           NOT NULL,
    ------------- 业务字段
    "meeting_id" uuid           NOT NULL,
    "content"    TEXT,
    ------------- 审计字段
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
);
COMMENT ON TABLE "spectra_core"."oa_meeting_record" IS 'OA-会议-会议纪要';
------------- 主键字段
COMMENT ON COLUMN "spectra_core"."oa_meeting_record"."id" IS '主键ID';
------------- 审计字段
COMMENT ON COLUMN "spectra_core"."oa_meeting_record"."created_by" IS '创建人';
COMMENT ON COLUMN "spectra_core"."oa_meeting_record"."created_at" IS '创建时间';
COMMENT ON COLUMN "spectra_core"."oa_meeting_record"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "spectra_core"."oa_meeting_record"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "spectra_core"."oa_meeting_record"."deleted" IS '删除标识';
COMMENT ON COLUMN "spectra_core"."oa_meeting_record"."version" IS '乐观锁';
------------- 业务字段
COMMENT ON COLUMN "spectra_core"."oa_meeting_record"."meeting_id" IS '会议ID';
COMMENT ON COLUMN "spectra_core"."oa_meeting_record"."content" IS '会议纪要';

------------- 约束
ALTER TABLE "spectra_core"."oa_meeting_record"
    ADD CONSTRAINT "oa_meeting_record_pkey" PRIMARY KEY ("id");