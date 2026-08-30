-- Repair the unified file upload metadata for databases that already applied V7.
-- This migration does not delete files, sessions, parts, assets, or references.

UPDATE spectra_core.file_type
SET allowed_extensions = '[".xlsx"]'::jsonb,
    allowed_content_types = '["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"]'::jsonb,
    upload_enabled = true,
    enabled = true,
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'XLSX'
  AND deleted IS NULL;

COMMENT ON TABLE spectra_core.file_type IS '文件类型策略';
COMMENT ON TABLE spectra_core.file_asset IS '已完成文件资产';
COMMENT ON TABLE spectra_core.file_upload_session IS '时间受限文件上传会话';
COMMENT ON TABLE spectra_core.file_upload_part IS '文件上传分片';
COMMENT ON TABLE spectra_core.file_reference IS '文件业务引用';

COMMENT ON COLUMN spectra_core.file_type.id IS '文件类型策略主键';
COMMENT ON COLUMN spectra_core.file_type.code IS '文件类型编码';
COMMENT ON COLUMN spectra_core.file_type.display_name IS '文件类型显示名称';
COMMENT ON COLUMN spectra_core.file_type.allowed_extensions IS '允许的文件扩展名 JSON 数组';
COMMENT ON COLUMN spectra_core.file_type.allowed_content_types IS '允许的声明 Content-Type JSON 数组';
COMMENT ON COLUMN spectra_core.file_type.magic_rules IS '文件魔数校验规则 JSON';
COMMENT ON COLUMN spectra_core.file_type.max_size IS '单文件最大大小（字节）';
COMMENT ON COLUMN spectra_core.file_type.preview_enabled IS '是否允许文件预览';
COMMENT ON COLUMN spectra_core.file_type.download_enabled IS '是否允许文件下载';
COMMENT ON COLUMN spectra_core.file_type.upload_enabled IS '是否允许文件上传';
COMMENT ON COLUMN spectra_core.file_type.dangerous IS '是否为危险文件类型';
COMMENT ON COLUMN spectra_core.file_type.enabled IS '是否启用文件类型策略';
COMMENT ON COLUMN spectra_core.file_type.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.file_type.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.file_type.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.file_type.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.file_type.deleted IS '逻辑删除时间';
COMMENT ON COLUMN spectra_core.file_type.version IS '乐观锁版本';

COMMENT ON COLUMN spectra_core.file_asset.id IS '文件资产主键';
COMMENT ON COLUMN spectra_core.file_asset.file_type_id IS '文件类型策略 ID';
COMMENT ON COLUMN spectra_core.file_asset.original_name IS '原始文件名';
COMMENT ON COLUMN spectra_core.file_asset.content_sha256 IS '服务端最终复核的 SHA-256 十六进制摘要';
COMMENT ON COLUMN spectra_core.file_asset.size IS '文件大小（字节）';
COMMENT ON COLUMN spectra_core.file_asset.content_type IS '文件 Content-Type';
COMMENT ON COLUMN spectra_core.file_asset.storage_provider IS '存储提供方';
COMMENT ON COLUMN spectra_core.file_asset.storage_container IS '存储容器或桶名称';
COMMENT ON COLUMN spectra_core.file_asset.storage_key IS '存储对象键';
COMMENT ON COLUMN spectra_core.file_asset.status IS '文件资产状态';
COMMENT ON COLUMN spectra_core.file_asset.completed_at IS '文件资产完成时间';
COMMENT ON COLUMN spectra_core.file_asset.orphaned_at IS '文件资产成为孤儿的时间';
COMMENT ON COLUMN spectra_core.file_asset.cleanup_attempts IS '清理尝试次数';
COMMENT ON COLUMN spectra_core.file_asset.next_cleanup_at IS '下次清理时间';
COMMENT ON COLUMN spectra_core.file_asset.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.file_asset.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.file_asset.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.file_asset.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.file_asset.deleted IS '逻辑删除时间';
COMMENT ON COLUMN spectra_core.file_asset.version IS '乐观锁版本';

COMMENT ON COLUMN spectra_core.file_upload_session.id IS '上传会话主键';
COMMENT ON COLUMN spectra_core.file_upload_session.owner_user_id IS '上传任务拥有者用户 ID';
COMMENT ON COLUMN spectra_core.file_upload_session.original_name IS '原始文件名';
COMMENT ON COLUMN spectra_core.file_upload_session.declared_content_type IS '客户端声明的 Content-Type';
COMMENT ON COLUMN spectra_core.file_upload_session.size IS '文件大小（字节）';
COMMENT ON COLUMN spectra_core.file_upload_session.content_sha256 IS '客户端声明的 SHA-256 十六进制摘要';
COMMENT ON COLUMN spectra_core.file_upload_session.chunk_size IS '分片大小（字节）';
COMMENT ON COLUMN spectra_core.file_upload_session.total_parts IS '分片总数';
COMMENT ON COLUMN spectra_core.file_upload_session.storage_provider IS '存储提供方';
COMMENT ON COLUMN spectra_core.file_upload_session.transport_mode IS '分片传输模式';
COMMENT ON COLUMN spectra_core.file_upload_session.storage_container IS '存储容器或桶名称';
COMMENT ON COLUMN spectra_core.file_upload_session.staging_key IS '暂存对象键';
COMMENT ON COLUMN spectra_core.file_upload_session.provider_upload_id IS '对象存储 Multipart Upload ID';
COMMENT ON COLUMN spectra_core.file_upload_session.file_asset_id IS '关联的文件资产 ID';
COMMENT ON COLUMN spectra_core.file_upload_session.status IS '上传会话状态';
COMMENT ON COLUMN spectra_core.file_upload_session.expires_at IS '上传会话绝对过期时间';
COMMENT ON COLUMN spectra_core.file_upload_session.last_activity_at IS '仅成功接收或确认分片时更新';
COMMENT ON COLUMN spectra_core.file_upload_session.completed_at IS '上传会话完成时间';
COMMENT ON COLUMN spectra_core.file_upload_session.verify_started_at IS '服务端最终复核开始时间';
COMMENT ON COLUMN spectra_core.file_upload_session.verify_finished_at IS '服务端最终复核完成时间';
COMMENT ON COLUMN spectra_core.file_upload_session.verify_processed_bytes IS '服务端最终复核已处理字节数';
COMMENT ON COLUMN spectra_core.file_upload_session.verify_total_bytes IS '服务端最终复核总字节数';
COMMENT ON COLUMN spectra_core.file_upload_session.failure_code IS '上传会话失败错误码';
COMMENT ON COLUMN spectra_core.file_upload_session.cleanup_attempts IS '清理尝试次数';
COMMENT ON COLUMN spectra_core.file_upload_session.next_cleanup_at IS '下次清理时间';
COMMENT ON COLUMN spectra_core.file_upload_session.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.file_upload_session.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.file_upload_session.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.file_upload_session.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.file_upload_session.deleted IS '逻辑删除时间';
COMMENT ON COLUMN spectra_core.file_upload_session.version IS '乐观锁版本';

COMMENT ON COLUMN spectra_core.file_upload_part.id IS '上传分片主键';
COMMENT ON COLUMN spectra_core.file_upload_part.upload_session_id IS '上传会话 ID';
COMMENT ON COLUMN spectra_core.file_upload_part.part_number IS '分片序号';
COMMENT ON COLUMN spectra_core.file_upload_part.expected_size IS '分片预期大小（字节）';
COMMENT ON COLUMN spectra_core.file_upload_part.expected_sha256 IS '分片预期 SHA-256 十六进制摘要';
COMMENT ON COLUMN spectra_core.file_upload_part.uploaded_size IS '分片实际上传大小（字节）';
COMMENT ON COLUMN spectra_core.file_upload_part.actual_sha256 IS '分片实际 SHA-256 十六进制摘要';
COMMENT ON COLUMN spectra_core.file_upload_part.provider_etag IS '存储提供方返回的 ETag';
COMMENT ON COLUMN spectra_core.file_upload_part.status IS '上传分片状态';
COMMENT ON COLUMN spectra_core.file_upload_part.upload_attempt IS '分片上传尝试次数';
COMMENT ON COLUMN spectra_core.file_upload_part.uploaded_at IS '分片上传完成时间';
COMMENT ON COLUMN spectra_core.file_upload_part.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.file_upload_part.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.file_upload_part.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.file_upload_part.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.file_upload_part.deleted IS '逻辑删除时间';
COMMENT ON COLUMN spectra_core.file_upload_part.version IS '乐观锁版本';

COMMENT ON COLUMN spectra_core.file_reference.id IS '文件业务引用主键';
COMMENT ON COLUMN spectra_core.file_reference.file_asset_id IS 'READY 文件资产 ID';
COMMENT ON COLUMN spectra_core.file_reference.reference_type IS '业务引用类型';
COMMENT ON COLUMN spectra_core.file_reference.reference_id IS '业务引用记录 ID';
COMMENT ON COLUMN spectra_core.file_reference.purpose IS '业务引用用途';
COMMENT ON COLUMN spectra_core.file_reference.display_name IS '业务侧显示名称';
COMMENT ON COLUMN spectra_core.file_reference.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.file_reference.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.file_reference.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.file_reference.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.file_reference.deleted IS '逻辑删除时间';
COMMENT ON COLUMN spectra_core.file_reference.version IS '乐观锁版本';
