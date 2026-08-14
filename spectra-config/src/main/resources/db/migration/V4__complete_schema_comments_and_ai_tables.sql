-- Complete comments for the target security/core schema and restore the two
-- AI persistence tables that are used by the AI module but were omitted from
-- the initial target-schema migration.

-- ============================================================================
-- spectra_ai
-- ============================================================================

CREATE TABLE spectra_ai.ai_conversation (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL,
    title       VARCHAR(200) NOT NULL DEFAULT '新对话',
    status      VARCHAR(20) NOT NULL DEFAULT 'active',
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT DEFAULT 0
);

CREATE INDEX idx_ai_conversation_user
    ON spectra_ai.ai_conversation (user_id)
    WHERE deleted IS NULL;

COMMENT ON TABLE spectra_ai.ai_conversation IS 'AI 会话元数据';
COMMENT ON COLUMN spectra_ai.ai_conversation.id IS '主键ID';
COMMENT ON COLUMN spectra_ai.ai_conversation.user_id IS '所属用户 ID';
COMMENT ON COLUMN spectra_ai.ai_conversation.title IS '会话标题（取首条消息前 30 字）';
COMMENT ON COLUMN spectra_ai.ai_conversation.status IS '状态：active / archived';
COMMENT ON COLUMN spectra_ai.ai_conversation.created_by IS '创建人';
COMMENT ON COLUMN spectra_ai.ai_conversation.created_at IS '创建时间';
COMMENT ON COLUMN spectra_ai.ai_conversation.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_ai.ai_conversation.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_ai.ai_conversation.deleted IS '删除标识';
COMMENT ON COLUMN spectra_ai.ai_conversation.version IS '乐观锁';

CREATE TABLE spectra_ai.ai_chat_memory (
    memory_id  VARCHAR(64) PRIMARY KEY,
    messages   TEXT NOT NULL DEFAULT '[]',
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE spectra_ai.ai_chat_memory IS 'AI 对话消息持久化存储';
COMMENT ON COLUMN spectra_ai.ai_chat_memory.memory_id IS '会话 ID（= ai_conversation.id::text）';
COMMENT ON COLUMN spectra_ai.ai_chat_memory.messages IS 'ChatMessageSerializer.messagesToJson() 序列化的 JSON';
COMMENT ON COLUMN spectra_ai.ai_chat_memory.created_at IS '创建时间';
COMMENT ON COLUMN spectra_ai.ai_chat_memory.updated_at IS '更新时间';

-- ============================================================================
-- spectra_core: comments omitted from the initial target schema
-- ============================================================================

COMMENT ON TABLE spectra_core.ai_session IS 'AI-Agent会话状态存储表';
COMMENT ON COLUMN spectra_core.ai_session.id IS '主键ID';
COMMENT ON COLUMN spectra_core.ai_session.session_id IS '会话标识';
COMMENT ON COLUMN spectra_core.ai_session.state_key IS '状态键';
COMMENT ON COLUMN spectra_core.ai_session.item_index IS '状态项序号';
COMMENT ON COLUMN spectra_core.ai_session.state_data IS '状态数据';
COMMENT ON COLUMN spectra_core.ai_session.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.ai_session.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.ai_session.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.ai_session.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.ai_session.deleted IS '删除标识';
COMMENT ON COLUMN spectra_core.ai_session.version IS '乐观锁';

COMMENT ON TABLE spectra_core.sys_user IS '系统用户表';
COMMENT ON COLUMN spectra_core.sys_user.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_user.username IS '登录用户名';
COMMENT ON COLUMN spectra_core.sys_user.avatar IS '头像地址';
COMMENT ON COLUMN spectra_core.sys_user.status IS '账号状态：ACTIVE-正常，LOCKED-锁定，DISABLED-禁用，DEPARTED-离职';
COMMENT ON COLUMN spectra_core.sys_user.status_reason IS '状态变更原因';
COMMENT ON COLUMN spectra_core.sys_user.locked_until IS '锁定截止时间';
COMMENT ON COLUMN spectra_core.sys_user.departed_at IS '离职时间';
COMMENT ON COLUMN spectra_core.sys_user.real_name IS '真实姓名';
COMMENT ON COLUMN spectra_core.sys_user.gender IS '性别';
COMMENT ON COLUMN spectra_core.sys_user.birthday IS '出生日期';
COMMENT ON COLUMN spectra_core.sys_user.phone IS '手机号码';
COMMENT ON COLUMN spectra_core.sys_user.email IS '电子邮箱';
COMMENT ON COLUMN spectra_core.sys_user.country IS '国家或地区';
COMMENT ON COLUMN spectra_core.sys_user.city IS '城市';
COMMENT ON COLUMN spectra_core.sys_user.language IS '语言偏好';
COMMENT ON COLUMN spectra_core.sys_user.timezone IS '时区';
COMMENT ON COLUMN spectra_core.sys_user.primary_department_id IS '主部门ID';
COMMENT ON COLUMN spectra_core.sys_user.security_version IS '安全版本号，用于权限变更和会话失效';
COMMENT ON COLUMN spectra_core.sys_user.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_user.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_user.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_user.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_user.deleted IS '删除标识';
COMMENT ON COLUMN spectra_core.sys_user.version IS '乐观锁';

COMMENT ON TABLE spectra_core.sys_user_department_membership IS '用户组织成员关系表';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.user_id IS '用户ID';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.department_id IS '部门ID';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.membership_type IS '成员关系：PRIMARY-主部门，ASSOCIATED-关联部门';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.created_at IS '加入时间';

COMMENT ON TABLE spectra_core.sys_department_closure IS '部门层级闭包关系表';
COMMENT ON COLUMN spectra_core.sys_department_closure.ancestor_id IS '祖先部门ID';
COMMENT ON COLUMN spectra_core.sys_department_closure.descendant_id IS '后代部门ID';
COMMENT ON COLUMN spectra_core.sys_department_closure.depth IS '层级深度，0表示部门自身';

COMMENT ON TABLE spectra_core.sys_organization_version IS '组织结构版本表';
COMMENT ON COLUMN spectra_core.sys_organization_version.singleton_key IS '单例键，固定为 SYSTEM';
COMMENT ON COLUMN spectra_core.sys_organization_version.version IS '组织结构版本号';
COMMENT ON COLUMN spectra_core.sys_organization_version.changed_at IS '最近变更时间';

-- ============================================================================
-- spectra_security: table and column comments
-- ============================================================================

COMMENT ON TABLE spectra_security.permission IS '权限定义表';
COMMENT ON COLUMN spectra_security.permission.id IS '主键ID';
COMMENT ON COLUMN spectra_security.permission.code IS '稳定权限编码，格式为 resource:action';
COMMENT ON COLUMN spectra_security.permission.name IS '权限名称';
COMMENT ON COLUMN spectra_security.permission.resource_code IS '资源编码';
COMMENT ON COLUMN spectra_security.permission.action_code IS '动作编码';
COMMENT ON COLUMN spectra_security.permission.allowed_scope_modes IS '允许的数据范围模式：NONE/ALL/SELF/RULES';
COMMENT ON COLUMN spectra_security.permission.state IS '权限状态：ACTIVE-启用，DEPRECATED-废弃';
COMMENT ON COLUMN spectra_security.permission.system_managed IS '是否系统维护权限';
COMMENT ON COLUMN spectra_security.permission.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.permission.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.permission.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.permission.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.permission.version IS '乐观锁';

COMMENT ON TABLE spectra_security.role IS '角色能力模板表';
COMMENT ON COLUMN spectra_security.role.id IS '主键ID';
COMMENT ON COLUMN spectra_security.role.code IS '角色编码，格式为 ROLE_*';
COMMENT ON COLUMN spectra_security.role.name IS '角色名称';
COMMENT ON COLUMN spectra_security.role.authority_level IS '管理边界等级，不用于推导业务权限';
COMMENT ON COLUMN spectra_security.role.state IS '角色状态：ACTIVE-启用，DISABLED-禁用';
COMMENT ON COLUMN spectra_security.role.role_kind IS '角色类型：BUSINESS/SYSTEM_ADMIN/AUDITOR/DEV_OPS';
COMMENT ON COLUMN spectra_security.role.system_managed IS '是否系统维护角色';
COMMENT ON COLUMN spectra_security.role.remark IS '备注';
COMMENT ON COLUMN spectra_security.role.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.role.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.role.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.role.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.role.version IS '乐观锁';

COMMENT ON TABLE spectra_security.role_permission IS '角色与权限关系表';
COMMENT ON COLUMN spectra_security.role_permission.role_id IS '角色ID';
COMMENT ON COLUMN spectra_security.role_permission.permission_id IS '权限ID';
COMMENT ON COLUMN spectra_security.role_permission.created_at IS '创建时间';

COMMENT ON TABLE spectra_security.role_grantable_permission IS '角色可授予权限关系表';
COMMENT ON COLUMN spectra_security.role_grantable_permission.role_id IS '角色ID';
COMMENT ON COLUMN spectra_security.role_grantable_permission.permission_id IS '可授予的权限ID';
COMMENT ON COLUMN spectra_security.role_grantable_permission.created_at IS '创建时间';

COMMENT ON TABLE spectra_security.role_assignment IS '用户角色授权实例表';
COMMENT ON COLUMN spectra_security.role_assignment.id IS '主键ID';
COMMENT ON COLUMN spectra_security.role_assignment.user_id IS '被授权用户ID';
COMMENT ON COLUMN spectra_security.role_assignment.role_id IS '角色ID';
COMMENT ON COLUMN spectra_security.role_assignment.state IS '授权状态：ACTIVE/REVOKED/EXPIRED';
COMMENT ON COLUMN spectra_security.role_assignment.valid_from IS '生效时间';
COMMENT ON COLUMN spectra_security.role_assignment.valid_until IS '失效时间';
COMMENT ON COLUMN spectra_security.role_assignment.assigned_by IS '授权操作人';
COMMENT ON COLUMN spectra_security.role_assignment.assigned_at IS '授权时间';
COMMENT ON COLUMN spectra_security.role_assignment.revoked_by IS '撤销操作人';
COMMENT ON COLUMN spectra_security.role_assignment.revoked_at IS '撤销时间';
COMMENT ON COLUMN spectra_security.role_assignment.version IS '乐观锁';

COMMENT ON TABLE spectra_security.authorization_scope IS '授权范围定义表';
COMMENT ON COLUMN spectra_security.authorization_scope.id IS '主键ID';
COMMENT ON COLUMN spectra_security.authorization_scope.scope_mode IS '范围模式：NONE/ALL/SELF/RULES';
COMMENT ON COLUMN spectra_security.authorization_scope.resource_code IS '范围对应资源编码';
COMMENT ON COLUMN spectra_security.authorization_scope.created_at IS '创建时间';

COMMENT ON TABLE spectra_security.assignment_permission_boundary IS '角色授权实例的访问边界表';
COMMENT ON COLUMN spectra_security.assignment_permission_boundary.assignment_id IS '角色授权实例ID';
COMMENT ON COLUMN spectra_security.assignment_permission_boundary.permission_id IS '权限ID';
COMMENT ON COLUMN spectra_security.assignment_permission_boundary.scope_id IS '该权限对应的数据范围ID';
COMMENT ON COLUMN spectra_security.assignment_permission_boundary.version IS '乐观锁';

COMMENT ON TABLE spectra_security.assignment_grant_boundary IS '角色授权实例的授予边界表';
COMMENT ON COLUMN spectra_security.assignment_grant_boundary.assignment_id IS '角色授权实例ID';
COMMENT ON COLUMN spectra_security.assignment_grant_boundary.permission_id IS '可授予权限ID';
COMMENT ON COLUMN spectra_security.assignment_grant_boundary.scope_id IS '该授予权限对应的管理范围ID';
COMMENT ON COLUMN spectra_security.assignment_grant_boundary.version IS '乐观锁';

COMMENT ON TABLE spectra_security.scope_rule IS '授权范围规则表';
COMMENT ON COLUMN spectra_security.scope_rule.id IS '主键ID';
COMMENT ON COLUMN spectra_security.scope_rule.scope_id IS '授权范围ID';
COMMENT ON COLUMN spectra_security.scope_rule.rule_type IS '规则类型：DEPARTMENT/RESOURCE_RULE';
COMMENT ON COLUMN spectra_security.scope_rule.department_id IS '部门ID';
COMMENT ON COLUMN spectra_security.scope_rule.include_descendants IS '是否包含下级部门';
COMMENT ON COLUMN spectra_security.scope_rule.rule_payload IS '资源规则参数';
COMMENT ON COLUMN spectra_security.scope_rule.created_at IS '创建时间';

COMMENT ON TABLE spectra_security.authentication_identity IS '认证身份标识表';
COMMENT ON COLUMN spectra_security.authentication_identity.id IS '主键ID';
COMMENT ON COLUMN spectra_security.authentication_identity.user_id IS '用户ID';
COMMENT ON COLUMN spectra_security.authentication_identity.method_code IS '认证方式编码';
COMMENT ON COLUMN spectra_security.authentication_identity.provider_code IS '认证提供方编码';
COMMENT ON COLUMN spectra_security.authentication_identity.identifier_hash IS '身份标识哈希';
COMMENT ON COLUMN spectra_security.authentication_identity.state IS '身份状态：ACTIVE/DISABLED/REVOKED';
COMMENT ON COLUMN spectra_security.authentication_identity.verified_at IS '验证时间';
COMMENT ON COLUMN spectra_security.authentication_identity.last_used_at IS '最后使用时间';
COMMENT ON COLUMN spectra_security.authentication_identity.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.authentication_identity.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.authentication_identity.version IS '乐观锁';

COMMENT ON TABLE spectra_security.password_credential IS '密码凭证表';
COMMENT ON COLUMN spectra_security.password_credential.user_id IS '用户ID';
COMMENT ON COLUMN spectra_security.password_credential.password_hash IS '密码哈希，不存储明文密码';
COMMENT ON COLUMN spectra_security.password_credential.changed_at IS '密码变更时间';
COMMENT ON COLUMN spectra_security.password_credential.expires_at IS '密码过期时间';
COMMENT ON COLUMN spectra_security.password_credential.must_change IS '是否必须修改密码';
COMMENT ON COLUMN spectra_security.password_credential.failed_attempts IS '连续认证失败次数';
COMMENT ON COLUMN spectra_security.password_credential.locked_until IS '凭证锁定截止时间';
COMMENT ON COLUMN spectra_security.password_credential.version IS '乐观锁';

COMMENT ON TABLE spectra_security.security_client IS '登录客户端定义表';
COMMENT ON COLUMN spectra_security.security_client.id IS '主键ID';
COMMENT ON COLUMN spectra_security.security_client.code IS '客户端编码，如 WEB/APP';
COMMENT ON COLUMN spectra_security.security_client.name IS '客户端名称';
COMMENT ON COLUMN spectra_security.security_client.state IS '客户端状态：ACTIVE/DISABLED';
COMMENT ON COLUMN spectra_security.security_client.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.security_client.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.security_client.version IS '乐观锁';

COMMENT ON TABLE spectra_security.authentication_method IS '认证方式定义表';
COMMENT ON COLUMN spectra_security.authentication_method.id IS '主键ID';
COMMENT ON COLUMN spectra_security.authentication_method.code IS '认证方式编码，如 PASSWORD/TOTP';
COMMENT ON COLUMN spectra_security.authentication_method.name IS '认证方式名称';
COMMENT ON COLUMN spectra_security.authentication_method.state IS '认证方式状态：ACTIVE/DISABLED';
COMMENT ON COLUMN spectra_security.authentication_method.secret_ref IS '外部密钥引用，不保存密钥明文';
COMMENT ON COLUMN spectra_security.authentication_method.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.authentication_method.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.authentication_method.version IS '乐观锁';

COMMENT ON TABLE spectra_security.client_auth_method IS '客户端允许的认证方式关系表';
COMMENT ON COLUMN spectra_security.client_auth_method.client_id IS '客户端ID';
COMMENT ON COLUMN spectra_security.client_auth_method.authentication_method_id IS '认证方式ID';

COMMENT ON TABLE spectra_security.session_policy IS '客户端会话策略表';
COMMENT ON COLUMN spectra_security.session_policy.client_id IS '客户端ID';
COMMENT ON COLUMN spectra_security.session_policy.concurrency_mode IS '并发策略：ALLOW/KICK_OLD/REJECT_NEW';
COMMENT ON COLUMN spectra_security.session_policy.allow_concurrent IS '是否允许并发会话';
COMMENT ON COLUMN spectra_security.session_policy.max_sessions IS '最大会话数';
COMMENT ON COLUMN spectra_security.session_policy.access_ttl_seconds IS '访问令牌有效期（秒）';
COMMENT ON COLUMN spectra_security.session_policy.refresh_ttl_seconds IS '刷新令牌有效期（秒）';
COMMENT ON COLUMN spectra_security.session_policy.absolute_ttl_seconds IS '会话绝对有效期（秒）';
COMMENT ON COLUMN spectra_security.session_policy.idle_ttl_seconds IS '会话空闲有效期（秒）';
COMMENT ON COLUMN spectra_security.session_policy.version IS '乐观锁';

COMMENT ON TABLE spectra_security.password_policy IS '密码安全策略表';
COMMENT ON COLUMN spectra_security.password_policy.policy_key IS '策略键，固定为 SYSTEM';
COMMENT ON COLUMN spectra_security.password_policy.min_length IS '密码最小长度';
COMMENT ON COLUMN spectra_security.password_policy.require_uppercase IS '是否要求大写字母';
COMMENT ON COLUMN spectra_security.password_policy.require_lowercase IS '是否要求小写字母';
COMMENT ON COLUMN spectra_security.password_policy.require_digit IS '是否要求数字';
COMMENT ON COLUMN spectra_security.password_policy.require_special IS '是否要求特殊字符';
COMMENT ON COLUMN spectra_security.password_policy.max_age_days IS '密码最长有效天数';
COMMENT ON COLUMN spectra_security.password_policy.version IS '乐观锁';

COMMENT ON TABLE spectra_security.mfa_enrollment IS '多因素认证绑定表';
COMMENT ON COLUMN spectra_security.mfa_enrollment.id IS '主键ID';
COMMENT ON COLUMN spectra_security.mfa_enrollment.user_id IS '用户ID';
COMMENT ON COLUMN spectra_security.mfa_enrollment.factor_type IS '因子类型：TOTP/WEBAUTHN/PASSKEY';
COMMENT ON COLUMN spectra_security.mfa_enrollment.state IS '绑定状态：PENDING/ACTIVE/REVOKED';
COMMENT ON COLUMN spectra_security.mfa_enrollment.enrolled_at IS '启用时间';
COMMENT ON COLUMN spectra_security.mfa_enrollment.revoked_at IS '撤销时间';
COMMENT ON COLUMN spectra_security.mfa_enrollment.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.mfa_enrollment.version IS '乐观锁';

COMMENT ON TABLE spectra_security.totp_credential IS 'TOTP 凭证表';
COMMENT ON COLUMN spectra_security.totp_credential.enrollment_id IS 'MFA 绑定ID';
COMMENT ON COLUMN spectra_security.totp_credential.encrypted_secret IS '加密后的 TOTP 密钥';
COMMENT ON COLUMN spectra_security.totp_credential.key_version IS '密钥版本';
COMMENT ON COLUMN spectra_security.totp_credential.created_at IS '创建时间';

COMMENT ON TABLE spectra_security.recovery_code IS 'MFA 恢复码表';
COMMENT ON COLUMN spectra_security.recovery_code.id IS '主键ID';
COMMENT ON COLUMN spectra_security.recovery_code.enrollment_id IS 'MFA 绑定ID';
COMMENT ON COLUMN spectra_security.recovery_code.code_hash IS '恢复码哈希，不存储明文';
COMMENT ON COLUMN spectra_security.recovery_code.used_at IS '使用时间，单次使用';
COMMENT ON COLUMN spectra_security.recovery_code.version IS '乐观锁';

COMMENT ON TABLE spectra_security.root_policy IS 'DEV_OPS 根策略表';
COMMENT ON COLUMN spectra_security.root_policy.policy_key IS '策略键，固定为 SYSTEM';
COMMENT ON COLUMN spectra_security.root_policy.min_effective_dev_ops_users IS '最少有效 DEV_OPS 数量，保护最后一个 Root';
COMMENT ON COLUMN spectra_security.root_policy.max_dev_ops_users IS '最多 DEV_OPS 数量';
COMMENT ON COLUMN spectra_security.root_policy.version IS '乐观锁';
COMMENT ON COLUMN spectra_security.root_policy.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.root_policy.updated_at IS '最后更新时间';

COMMENT ON TABLE spectra_security.security_audit_event IS '不可删除的安全审计事件表';
COMMENT ON COLUMN spectra_security.security_audit_event.event_id IS '事件ID';
COMMENT ON COLUMN spectra_security.security_audit_event.event_type IS '事件类型';
COMMENT ON COLUMN spectra_security.security_audit_event.operator_id IS '操作人ID';
COMMENT ON COLUMN spectra_security.security_audit_event.target_id IS '目标主体ID';
COMMENT ON COLUMN spectra_security.security_audit_event.client IS '客户端类型';
COMMENT ON COLUMN spectra_security.security_audit_event.ip IS '来源IP';
COMMENT ON COLUMN spectra_security.security_audit_event.user_agent IS '客户端 User-Agent';
COMMENT ON COLUMN spectra_security.security_audit_event.before_snapshot IS '变更前快照，敏感字段已脱敏';
COMMENT ON COLUMN spectra_security.security_audit_event.after_snapshot IS '变更后快照，敏感字段已脱敏';
COMMENT ON COLUMN spectra_security.security_audit_event.reason IS '操作原因';
COMMENT ON COLUMN spectra_security.security_audit_event.occurred_at IS '发生时间';
COMMENT ON COLUMN spectra_security.security_audit_event.result IS '结果：STARTED/SUCCEEDED/FAILED/DENIED';
COMMENT ON COLUMN spectra_security.security_audit_event.correlation_id IS '关联请求ID';

COMMENT ON TABLE spectra_security.security_audit_event_default IS '安全审计默认分区';
COMMENT ON COLUMN spectra_security.security_audit_event_default.event_id IS '事件ID';
COMMENT ON COLUMN spectra_security.security_audit_event_default.event_type IS '事件类型';
COMMENT ON COLUMN spectra_security.security_audit_event_default.operator_id IS '操作人ID';
COMMENT ON COLUMN spectra_security.security_audit_event_default.target_id IS '目标主体ID';
COMMENT ON COLUMN spectra_security.security_audit_event_default.client IS '客户端类型';
COMMENT ON COLUMN spectra_security.security_audit_event_default.ip IS '来源IP';
COMMENT ON COLUMN spectra_security.security_audit_event_default.user_agent IS '客户端 User-Agent';
COMMENT ON COLUMN spectra_security.security_audit_event_default.before_snapshot IS '变更前快照，敏感字段已脱敏';
COMMENT ON COLUMN spectra_security.security_audit_event_default.after_snapshot IS '变更后快照，敏感字段已脱敏';
COMMENT ON COLUMN spectra_security.security_audit_event_default.reason IS '操作原因';
COMMENT ON COLUMN spectra_security.security_audit_event_default.occurred_at IS '发生时间';
COMMENT ON COLUMN spectra_security.security_audit_event_default.result IS '结果：STARTED/SUCCEEDED/FAILED/DENIED';
COMMENT ON COLUMN spectra_security.security_audit_event_default.correlation_id IS '关联请求ID';

COMMENT ON TABLE spectra_security.security_change_outbox IS '安全变更事件发件箱表';
COMMENT ON COLUMN spectra_security.security_change_outbox.id IS '主键ID';
COMMENT ON COLUMN spectra_security.security_change_outbox.event_type IS '变更事件类型';
COMMENT ON COLUMN spectra_security.security_change_outbox.aggregate_type IS '聚合类型';
COMMENT ON COLUMN spectra_security.security_change_outbox.aggregate_id IS '聚合ID';
COMMENT ON COLUMN spectra_security.security_change_outbox.payload IS '事件载荷';
COMMENT ON COLUMN spectra_security.security_change_outbox.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.security_change_outbox.processed_at IS '处理完成时间';
COMMENT ON COLUMN spectra_security.security_change_outbox.attempts IS '处理尝试次数';
COMMENT ON COLUMN spectra_security.security_change_outbox.last_error IS '最后一次处理错误';
COMMENT ON COLUMN spectra_security.security_change_outbox.version IS '乐观锁';

COMMENT ON TABLE spectra_security.role_menu IS '角色与菜单关系表';
COMMENT ON COLUMN spectra_security.role_menu.role_id IS '角色ID';
COMMENT ON COLUMN spectra_security.role_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN spectra_security.role_menu.created_at IS '创建时间';
