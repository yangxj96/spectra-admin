-- ============================================================
-- V20260726: 按模块拆分 PostgreSQL Schema
-- 将 spectra_core 中的业务表按模块迁移到独立 schema
-- ============================================================

BEGIN;

-- 1. 新建 schema
CREATE SCHEMA IF NOT EXISTS spectra_oa;
CREATE SCHEMA IF NOT EXISTS spectra_upload;
CREATE SCHEMA IF NOT EXISTS spectra_ai;

-- 2. pgvector 扩展从 spectra_rag 迁移到 public
ALTER EXTENSION vector SET SCHEMA public;

-- 3. 删除依赖 vector 操作符类的 ivfflat 索引
DROP INDEX IF EXISTS spectra_rag.ai_knowledge_chunks_embedding_idx;

-- 4. ai_knowledge_chunks 从 spectra_rag 迁移到 spectra_ai
ALTER TABLE spectra_rag.ai_knowledge_chunks SET SCHEMA spectra_ai;

-- 5. 重建 ivfflat 索引（操作符类现在在 public 下）
CREATE INDEX ai_knowledge_chunks_embedding_idx
    ON spectra_ai.ai_knowledge_chunks
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- 6. 删除空的 spectra_rag schema
DROP SCHEMA IF EXISTS spectra_rag CASCADE;

-- 7. OA 表迁移（11 张）→ spectra_oa
ALTER TABLE spectra_core.oa_asset SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_attendance SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_calendar SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_contact SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_contract SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_document SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_meeting SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_meeting_participant SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_meeting_record SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_notice SET SCHEMA spectra_oa;
ALTER TABLE spectra_core.oa_report SET SCHEMA spectra_oa;

-- 8. 文件表迁移（4 张）→ spectra_upload
ALTER TABLE spectra_core.file_info SET SCHEMA spectra_upload;
ALTER TABLE spectra_core.file_type SET SCHEMA spectra_upload;
ALTER TABLE spectra_core.file_upload_task SET SCHEMA spectra_upload;
ALTER TABLE spectra_core.file_upload_chunk SET SCHEMA spectra_upload;

-- 9. 工作流业务表迁移（2 张）→ spectra_workflow
ALTER TABLE spectra_core.wf_form_definition SET SCHEMA spectra_workflow;
ALTER TABLE spectra_core.wf_form_version SET SCHEMA spectra_workflow;

-- 10. AI 表迁移（1 张）→ spectra_ai
ALTER TABLE spectra_core.ai_session SET SCHEMA spectra_ai;

COMMIT;
