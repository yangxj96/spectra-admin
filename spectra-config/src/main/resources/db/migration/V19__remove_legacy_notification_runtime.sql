/*
 * 统一通知模块已完成运行时切换。旧通知表只允许作为本迁移之前的离线迁移来源，
 * 不再保留在运行库中，避免新代码继续依赖旧模型。
 */
DROP TABLE IF EXISTS spectra_core.sys_notification_setting;
DROP TABLE IF EXISTS spectra_core.sys_notification;

/* 部门删除入口已冻结，旧权限目录项不能继续被当作可用能力授予。 */
UPDATE spectra_security.sec_permission
SET state = 'DEPRECATED'
WHERE code = 'department:disable';
