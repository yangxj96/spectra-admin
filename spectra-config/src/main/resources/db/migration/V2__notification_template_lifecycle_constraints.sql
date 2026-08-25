/*
 * 通知模板生命周期约束。
 *
 * 数据状态调整由当前数据库直接处理；本脚本只维护结构性唯一约束。
 */

DROP INDEX IF EXISTS spectra_notification."UK_NTF_TEMPLATE_PUBLISHED";

CREATE UNIQUE INDEX "UK_NTF_TEMPLATE_RELEASED"
    ON spectra_notification.ntf_template (template_group_code, channel)
    WHERE state IN ('PUBLISHED', 'DISABLED')
      AND deleted IS NULL;

CREATE UNIQUE INDEX "UK_NTF_TEMPLATE_DRAFT"
    ON spectra_notification.ntf_template (template_group_code, channel)
    WHERE state = 'DRAFT'
      AND deleted IS NULL;
