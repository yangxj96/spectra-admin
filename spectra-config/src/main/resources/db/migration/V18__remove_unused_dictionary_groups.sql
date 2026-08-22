-- 流程分类、OA相关、系统配置和水印类型没有业务引用，移除对应字典数据。
-- sys_common_state 仍被字典管理页面用于字典组和字典项状态，继续保留。
DELETE FROM spectra_core.sys_dict_item
WHERE gid IN (
    SELECT id
    FROM spectra_core.sys_dict_group
    WHERE code IN ('dict_workflow_type', 'oa', 'sys', 'sys_watermark')
);

DELETE FROM spectra_core.sys_dict_group
WHERE code IN ('dict_workflow_type', 'oa', 'sys', 'sys_watermark');
