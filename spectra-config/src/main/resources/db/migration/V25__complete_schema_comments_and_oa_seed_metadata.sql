-- V25：补齐列重建后遗失的表/字段注释，并规范 OA 固定种子元数据。
--
-- 业务种子统一使用零 UUID 作为创建人/更新人，固定时间统一为
-- 1996-10-15 00:00:00（Asia/Shanghai）。只按固定种子的技术主键更新，
-- 不影响运行期新增或修改的业务数据。

DO $$
DECLARE
    seed_actor CONSTANT UUID := '00000000-0000-0000-0000-000000000000';
    seed_time CONSTANT TIMESTAMPTZ := '1996-10-15 00:00:00+08:00';
BEGIN
    UPDATE spectra_oa.oa_application_type
       SET created_by = seed_actor,
           created_at = seed_time,
           updated_by = seed_actor,
           updated_at = seed_time
     WHERE id IN (
         '00000000-0000-0000-0000-000000000001',
         '00000000-0000-0000-0000-000000000002',
         '00000000-0000-0000-0000-000000000003'
     );

    UPDATE spectra_oa.oa_asset_category
       SET created_by = seed_actor,
           created_at = seed_time,
           updated_by = seed_actor,
           updated_at = seed_time
     WHERE id IN (
         '00000000-0000-0000-0000-000000000101',
         '00000000-0000-0000-0000-000000000102',
         '00000000-0000-0000-0000-000000000103',
         '00000000-0000-0000-0000-000000000104'
     );

    UPDATE spectra_oa.oa_leave_type
       SET created_by = seed_actor,
           created_at = seed_time,
           updated_by = seed_actor,
           updated_at = seed_time
     WHERE id IN (
         '00000000-0000-0000-0000-000000000011',
         '00000000-0000-0000-0000-000000000012',
         '00000000-0000-0000-0000-000000000013'
     );
END
$$;

COMMENT ON TABLE spectra_core.sys_user_department_membership IS '用户与部门成员关系表';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_core.sys_department_closure IS '部门层级闭包关系表';
COMMENT ON COLUMN spectra_core.sys_department_closure.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_department_closure.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_department_closure.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_department_closure.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_department_closure.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_department_closure.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_core.sys_department_closure.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_core.sys_organization_version IS '组织结构版本单例表';
COMMENT ON COLUMN spectra_core.sys_organization_version.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_organization_version.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_organization_version.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_organization_version.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_organization_version.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_organization_version.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_core.sys_organization_version.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_core.sys_system_state IS '系统初始化状态单例表';
COMMENT ON COLUMN spectra_core.sys_system_state.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_system_state.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_system_state.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_system_state.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_system_state.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_system_state.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_core.sys_system_state.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_security.sec_permission.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_role.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_role_permission.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_role_permission.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_role_permission.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_role_permission.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_role_permission.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_role_permission.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_security.sec_role_grantable_permission.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_role_grantable_permission.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_role_grantable_permission.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_role_grantable_permission.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_role_grantable_permission.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_role_grantable_permission.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_security.sec_role_assignment.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_role_assignment.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_role_assignment.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_role_assignment.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_role_assignment.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_authorization_scope.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_authorization_scope.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_authorization_scope.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_authorization_scope.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_authorization_scope.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_security.sec_assignment_permission_boundary.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_assignment_permission_boundary.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_assignment_permission_boundary.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_assignment_permission_boundary.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_assignment_permission_boundary.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_assignment_permission_boundary.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_assignment_grant_boundary.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_assignment_grant_boundary.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_assignment_grant_boundary.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_assignment_grant_boundary.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_assignment_grant_boundary.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_assignment_grant_boundary.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_scope_rule.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_scope_rule.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_scope_rule.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_scope_rule.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_scope_rule.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_security.sec_authentication_identity.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_authentication_identity.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_authentication_identity.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_authentication_method.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_authentication_method.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_authentication_method.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_client_auth_method.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_client_auth_method.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_client_auth_method.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_client_auth_method.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_client_auth_method.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_client_auth_method.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_client_auth_method.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_security.sec_password_credential.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_password_credential.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_password_credential.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_password_credential.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_password_credential.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_password_credential.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_security_client.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_security_client.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_security_client.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_session_policy.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_session_policy.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_session_policy.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_session_policy.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_session_policy.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_session_policy.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_password_policy.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_password_policy.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_password_policy.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_password_policy.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_password_policy.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_password_policy.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_mfa_enrollment.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_mfa_enrollment.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_mfa_enrollment.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_mfa_enrollment.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_totp_credential.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_totp_credential.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_totp_credential.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_totp_credential.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_totp_credential.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_totp_credential.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_security.sec_recovery_code.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_recovery_code.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_recovery_code.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_recovery_code.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_recovery_code.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_root_policy.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_root_policy.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_root_policy.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_root_policy.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON TABLE spectra_security.sec_security_audit_archive_manifest IS '安全审计分区归档、校验、恢复清单，不代表可删除审计事实';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.manifest_id IS '归档清单业务ID';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.partition_name IS '审计分区名称';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.range_start IS '分区范围开始时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.range_end IS '分区范围结束时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.object_uri IS '归档对象 URI';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.content_sha256 IS '归档对象完整性校验摘要';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.row_count IS '归档行数';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.state IS '归档状态';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.archived_at IS '归档完成时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.verified_at IS '归档校验完成时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.last_error IS '最近一次归档或恢复错误';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_security.sec_security_audit_event_default IS '安全审计默认分区';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.id IS '技术主键ID';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.event_id IS '事件ID';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.event_type IS '事件类型';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.operator_id IS '操作人ID';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.target_id IS '目标主体ID';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.client IS '客户端类型';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.ip IS '来源IP';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.user_agent IS '客户端 User-Agent';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.before_snapshot IS '变更前快照，敏感字段已脱敏';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.after_snapshot IS '变更后快照，敏感字段已脱敏';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.reason IS '操作原因';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.occurred_at IS '发生时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.result IS '结果：STARTED/SUCCEEDED/FAILED/DENIED';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.correlation_id IS '关联请求ID';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_security_audit_event_default.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_security.sec_security_audit_retention_policy IS '安全审计热存/总保留策略，只读展示，变更需审计运维流程';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.policy_key IS '策略键';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.hot_retention_months IS '热数据保留月数';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.total_retention_years IS '数据总保留年数';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.archive_backend IS '归档后端';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.state IS '策略状态';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_security_audit_retention_policy.version IS '乐观锁版本号';

COMMENT ON COLUMN spectra_security.sec_security_change_outbox.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_security_change_outbox.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_security_change_outbox.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_security_change_outbox.deleted IS '删除时间（NULL表示未删除）';

COMMENT ON COLUMN spectra_security.sec_role_menu.id IS '主键ID';
COMMENT ON COLUMN spectra_security.sec_role_menu.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_role_menu.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_role_menu.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_role_menu.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_security.sec_role_menu.version IS '乐观锁版本号';
