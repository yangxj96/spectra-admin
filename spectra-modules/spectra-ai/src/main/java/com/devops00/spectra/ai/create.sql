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

-- AI 会话元数据表
DROP TABLE IF EXISTS spectra_ai.ai_conversation;
CREATE TABLE spectra_ai.ai_conversation
(
    ------------- 主键字段
    "id"         uuid           NOT NULL,
    ------------- 业务字段
    "user_id"    uuid           NOT NULL,
    "title"      VARCHAR(200)   NOT NULL DEFAULT '新对话',
    "status"     VARCHAR(20)    NOT NULL DEFAULT 'active',
    ------------- 审计字段
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8                    DEFAULT 0
);
COMMENT ON TABLE spectra_ai.ai_conversation IS 'AI 会话元数据';
COMMENT ON COLUMN spectra_ai.ai_conversation."id" IS '主键ID';
COMMENT ON COLUMN spectra_ai.ai_conversation."user_id" IS '所属用户 ID';
COMMENT ON COLUMN spectra_ai.ai_conversation."title" IS '会话标题（取首条消息前 30 字）';
COMMENT ON COLUMN spectra_ai.ai_conversation."status" IS '状态：active / archived';
COMMENT ON COLUMN spectra_ai.ai_conversation."created_by" IS '创建人';
COMMENT ON COLUMN spectra_ai.ai_conversation."created_at" IS '创建时间';
COMMENT ON COLUMN spectra_ai.ai_conversation."updated_by" IS '最后更新人';
COMMENT ON COLUMN spectra_ai.ai_conversation."updated_at" IS '最后更新时间';
COMMENT ON COLUMN spectra_ai.ai_conversation."deleted" IS '删除标识';
COMMENT ON COLUMN spectra_ai.ai_conversation."version" IS '乐观锁';
------------- 约束
ALTER TABLE spectra_ai.ai_conversation
    ADD CONSTRAINT "ai_conversation_pkey" PRIMARY KEY ("id");
------------- 索引
CREATE INDEX "idx_ai_conversation_user" ON spectra_ai.ai_conversation ("user_id") WHERE deleted IS NULL;

-- AI 对话消息持久化存储表
DROP TABLE IF EXISTS spectra_ai.ai_chat_memory;
CREATE TABLE spectra_ai.ai_chat_memory
(
    "memory_id"  VARCHAR(64)    NOT NULL,
    "messages"   TEXT           NOT NULL DEFAULT '[]',
    "created_at" timestamptz(6) NOT NULL DEFAULT now(),
    "updated_at" timestamptz(6) NOT NULL DEFAULT now()
);
COMMENT ON TABLE spectra_ai.ai_chat_memory IS 'AI 对话消息持久化存储';
COMMENT ON COLUMN spectra_ai.ai_chat_memory."memory_id" IS '会话 ID（= ai_conversation.id::text）';
COMMENT ON COLUMN spectra_ai.ai_chat_memory."messages" IS 'ChatMessageSerializer.messagesToJson() 序列化的 JSON';
COMMENT ON COLUMN spectra_ai.ai_chat_memory."created_at" IS '创建时间';
COMMENT ON COLUMN spectra_ai.ai_chat_memory."updated_at" IS '更新时间';
------------- 约束
ALTER TABLE spectra_ai.ai_chat_memory
    ADD CONSTRAINT "ai_chat_memory_pkey" PRIMARY KEY ("memory_id");

-- 删除废弃的 ai_session 表
DROP TABLE IF EXISTS spectra_ai.ai_session;
DROP TABLE IF EXISTS spectra_core.ai_session;
