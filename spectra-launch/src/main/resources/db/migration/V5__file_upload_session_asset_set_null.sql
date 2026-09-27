-- 保留已完成的上传会话记录；清理孤儿文件资产时解除可选资产关联。
ALTER TABLE spectra_core.file_upload_session
    DROP CONSTRAINT file_upload_session_asset_fk;

ALTER TABLE spectra_core.file_upload_session
    ADD CONSTRAINT file_upload_session_asset_fk
    FOREIGN KEY (file_asset_id)
    REFERENCES spectra_core.file_asset (id)
    ON DELETE SET NULL;
