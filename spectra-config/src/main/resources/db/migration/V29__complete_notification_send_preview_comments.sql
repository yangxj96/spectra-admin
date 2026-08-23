-- 补齐受控发送 Preview 表的列注释，使运行库与通知建表文档保持一致。
COMMENT ON COLUMN spectra_notification.ntf_send_preview.id IS '主键ID';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.operator_user_id IS '受控发送操作人ID';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.request_hash IS 'Preview 请求 SHA-256 摘要';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.preview_token_hash IS '一次性 Preview token 的 SHA-256 摘要';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.resolution_hash IS '受众、数据范围和渠道状态解析摘要';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.request_snapshot IS '非敏感受控发送请求快照，过期后物理删除';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.expires_at IS 'Preview 过期时间';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.consumed_at IS 'Apply 消费时间';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.applied_request_id IS 'Apply 创建的逻辑通知请求ID';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.status IS 'Preview 状态：PREVIEWED、APPLYING、APPLIED 或 EXPIRED';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.created_by IS '创建人ID';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.created_at IS '创建时间';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.updated_by IS '最后更新人ID';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.deleted IS '删除时间；NULL表示未删除';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.version IS '乐观锁版本号';
