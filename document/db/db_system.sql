CREATE SCHEMA IF NOT EXISTS db_auth;
CREATE SCHEMA IF NOT EXISTS db_system;

-- 账号信息
DROP TABLE IF EXISTS db_auth.t_account;
CREATE TABLE IF NOT EXISTS db_auth.t_account
(
    id         BIGINT PRIMARY KEY,

    username   VARCHAR(20) UNIQUE,
    password   VARCHAR(128),
    user_id    BIGINT,
    type       int2,

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_auth.t_account IS '账号信息';
COMMENT ON COLUMN db_auth.t_account.id IS '主键ID';
COMMENT ON COLUMN db_auth.t_account.created_by IS '创建人';
COMMENT ON COLUMN db_auth.t_account.created_at IS '创建时间';
COMMENT ON COLUMN db_auth.t_account.updated_by IS '最后更新人';
COMMENT ON COLUMN db_auth.t_account.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_auth.t_account.deleted IS '是否删除';
COMMENT ON COLUMN db_auth.t_account.username IS '用户名';
COMMENT ON COLUMN db_auth.t_account.password IS '密码';
COMMENT ON COLUMN db_auth.t_account.user_id IS '用户ID';
COMMENT ON COLUMN db_auth.t_account.type IS '账号类型 0-账号密码';

-- 用户信息
DROP TABLE IF EXISTS db_auth.t_user;
CREATE TABLE IF NOT EXISTS db_auth.t_user
(
    id         BIGINT PRIMARY KEY,

    name       VARCHAR(100),
    email      VARCHAR(100) UNIQUE,
    avatar     VARCHAR(100),
    state      int2,

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_auth.t_user IS '用户信息';
COMMENT ON COLUMN db_auth.t_user.id IS '主键ID';
COMMENT ON COLUMN db_auth.t_user.created_by IS '创建人';
COMMENT ON COLUMN db_auth.t_user.created_at IS '创建时间';
COMMENT ON COLUMN db_auth.t_user.updated_by IS '最后更新人';
COMMENT ON COLUMN db_auth.t_user.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_auth.t_user.deleted IS '是否删除';

COMMENT ON COLUMN db_auth.t_user.name IS '姓名';
COMMENT ON COLUMN db_auth.t_user.email IS '邮箱';
COMMENT ON COLUMN db_auth.t_user.avatar IS '头像';
COMMENT ON COLUMN db_auth.t_user.state IS '状态';

-- 角色表
DROP TABLE IF EXISTS db_auth.t_role;
CREATE TABLE IF NOT EXISTS db_auth.t_role
(
    id         BIGINT PRIMARY KEY,

    name       VARCHAR(100),
    code       VARCHAR(100) UNIQUE,
    state      BOOLEAN DEFAULT TRUE,
    scope      int2,
    remark     VARCHAR(255),

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_auth.t_role IS '角色表';
COMMENT ON COLUMN db_auth.t_role.id IS '主键ID';
COMMENT ON COLUMN db_auth.t_role.created_by IS '创建人';
COMMENT ON COLUMN db_auth.t_role.created_at IS '创建时间';
COMMENT ON COLUMN db_auth.t_role.updated_by IS '最后更新人';
COMMENT ON COLUMN db_auth.t_role.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_auth.t_role.deleted IS '是否删除';
COMMENT ON COLUMN db_auth.t_role.name IS '名称';
COMMENT ON COLUMN db_auth.t_role.code IS '编码';
COMMENT ON COLUMN db_auth.t_role.state IS '状态';
COMMENT ON COLUMN db_auth.t_role.scope IS '范围';
COMMENT ON COLUMN db_auth.t_role.remark IS '备注';

-- 角色表<->用户
DROP TABLE IF EXISTS db_auth.t_user_role_map;
CREATE TABLE IF NOT EXISTS db_auth.t_user_role_map
(
    id         BIGINT PRIMARY KEY,

    user_id    BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_auth.t_user_role_map IS '角色表<->账户';
COMMENT ON COLUMN db_auth.t_user_role_map.id IS '主键ID';
COMMENT ON COLUMN db_auth.t_user_role_map.created_by IS '创建人';
COMMENT ON COLUMN db_auth.t_user_role_map.created_at IS '创建时间';
COMMENT ON COLUMN db_auth.t_user_role_map.updated_by IS '最后更新人';
COMMENT ON COLUMN db_auth.t_user_role_map.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_auth.t_user_role_map.deleted IS '是否删除';
COMMENT ON COLUMN db_auth.t_user_role_map.user_id IS '用户ID';
COMMENT ON COLUMN db_auth.t_user_role_map.role_id IS '角色ID';

-- 权限表
DROP TABLE IF EXISTS db_auth.t_authority;
CREATE TABLE IF NOT EXISTS db_auth.t_authority
(
    id         BIGINT PRIMARY KEY,

    pid        BIGINT,
    name       VARCHAR(100),
    code       VARCHAR(100),

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_auth.t_authority IS '权限表';
COMMENT ON COLUMN db_auth.t_authority.id IS '主键ID';
COMMENT ON COLUMN db_auth.t_authority.created_by IS '创建人';
COMMENT ON COLUMN db_auth.t_authority.created_at IS '创建时间';
COMMENT ON COLUMN db_auth.t_authority.updated_by IS '最后更新人';
COMMENT ON COLUMN db_auth.t_authority.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_auth.t_authority.deleted IS '是否删除';
COMMENT ON COLUMN db_auth.t_authority.pid IS '父级ID,用于构建树形结构';
COMMENT ON COLUMN db_auth.t_authority.name IS '权限名称';
COMMENT ON COLUMN db_auth.t_authority.code IS '权限编码';

-- 权限表<->角色
DROP TABLE IF EXISTS db_auth.t_role_authority_map;
CREATE TABLE IF NOT EXISTS db_auth.t_role_authority_map
(
    id           BIGINT PRIMARY KEY,

    role_id      BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,

    created_by   BIGINT,
    created_at   TIMESTAMP,
    updated_by   BIGINT,
    updated_at   TIMESTAMP,
    deleted      TIMESTAMP
);
COMMENT ON TABLE db_auth.t_role_authority_map IS '权限表<->角色';
COMMENT ON COLUMN db_auth.t_role_authority_map.id IS '主键ID';
COMMENT ON COLUMN db_auth.t_role_authority_map.created_by IS '创建人';
COMMENT ON COLUMN db_auth.t_role_authority_map.created_at IS '创建时间';
COMMENT ON COLUMN db_auth.t_role_authority_map.updated_by IS '最后更新人';
COMMENT ON COLUMN db_auth.t_role_authority_map.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_auth.t_role_authority_map.deleted IS '是否删除';
COMMENT ON COLUMN db_auth.t_role_authority_map.role_id IS '角色ID';
COMMENT ON COLUMN db_auth.t_role_authority_map.authority_id IS '权限ID';

-- 菜单表
DROP TABLE IF EXISTS db_system.t_menu;
CREATE TABLE IF NOT EXISTS db_system.t_menu
(
    id         BIGINT PRIMARY KEY,

    name       VARCHAR(100) NOT NULL,
    pid        BIGINT,
    icon       VARCHAR(100),
    path       VARCHAR(255) NOT NULL,
    component  VARCHAR(100) NOT NULL,
    layout     VARCHAR(100),
    sort       INT DEFAULT 0,

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_system.t_menu IS '菜单表';
COMMENT ON COLUMN db_system.t_menu.id IS '主键ID';
COMMENT ON COLUMN db_system.t_menu.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_menu.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_menu.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_menu.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_menu.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_menu.pid IS '父级ID';
COMMENT ON COLUMN db_system.t_menu.icon IS '图标';
COMMENT ON COLUMN db_system.t_menu.name IS '名称';
COMMENT ON COLUMN db_system.t_menu.path IS '请求路径';
COMMENT ON COLUMN db_system.t_menu.component IS '组件路径,为空则使用布局组件';
COMMENT ON COLUMN db_system.t_menu.layout IS '布局';
COMMENT ON COLUMN db_system.t_menu.sort IS '排序';

-- 操作日志
DROP TABLE IF EXISTS db_system.t_operation_log;
CREATE TABLE IF NOT EXISTS db_system.t_operation_log
(
    id         BIGINT PRIMARY KEY,

    explain    TEXT,
    status     int2,
    ip         VARCHAR(15),
    method     VARCHAR(255),
    url        VARCHAR(255),
    args       JSON,
    result     json,
    time_cost  BIGINT,

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_system.t_operation_log IS '操作日志表';
COMMENT ON COLUMN db_system.t_operation_log.id IS '主键ID';
COMMENT ON COLUMN db_system.t_operation_log.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_operation_log.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_operation_log.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_operation_log.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_operation_log.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_operation_log.explain IS '日志说明';
COMMENT ON COLUMN db_system.t_operation_log.status IS '请求状态';
COMMENT ON COLUMN db_system.t_operation_log.ip IS '来源IP';
COMMENT ON COLUMN db_system.t_operation_log.method IS '请求方法';
COMMENT ON COLUMN db_system.t_operation_log.url IS '请求URL';
COMMENT ON COLUMN db_system.t_operation_log.args IS '请求参数';
COMMENT ON COLUMN db_system.t_operation_log.result IS '请求响应';
COMMENT ON COLUMN db_system.t_operation_log.time_cost IS '耗时';

-- 组织机构
DROP TABLE IF EXISTS db_system.t_organization;
CREATE TABLE IF NOT EXISTS db_system.t_organization
(
    id         BIGINT PRIMARY KEY,

    pid        BIGINT,
    name       VARCHAR(100) NOT NULL,
    code       VARCHAR(100) NOT NULL,
    type       int2,
    address    VARCHAR(255) NOT NULL,
    manager_id BIGINT,
    remark     VARCHAR(255),

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
-- 索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_t_organization_name_unique ON db_system.t_organization (name);
CREATE UNIQUE INDEX IF NOT EXISTS idx_t_organization_code_unique ON db_system.t_organization (code);
COMMENT ON TABLE db_system.t_organization IS '组织机构';
COMMENT ON COLUMN db_system.t_organization.id IS '主键ID';
COMMENT ON COLUMN db_system.t_organization.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_organization.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_organization.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_organization.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_organization.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_organization.pid IS '上级ID';
COMMENT ON COLUMN db_system.t_organization.name IS '名称';
COMMENT ON COLUMN db_system.t_organization.code IS '编码';
COMMENT ON COLUMN db_system.t_organization.type IS '公司类型';
COMMENT ON COLUMN db_system.t_organization.address IS '注册地址';
COMMENT ON COLUMN db_system.t_organization.manager_id IS '负责人ID';
COMMENT ON COLUMN db_system.t_organization.remark IS '备注';


-- 数据字典(字典组)
DROP TABLE IF EXISTS db_system.t_dict_group;
CREATE TABLE IF NOT EXISTS db_system.t_dict_group
(
    id         BIGINT PRIMARY KEY,

    pid        BIGINT,
    name       VARCHAR(100) NOT NULL,
    code       VARCHAR(100) NOT NULL,
    state      int2         NOT NULL,
    remark     VARCHAR(255),
    builtin    BOOLEAN      NOT NULL DEFAULT FALSE,
    hide       BOOLEAN      NOT NULL DEFAULT FALSE,


    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_system.t_dict_group IS '数据字典(字典类型)';
COMMENT ON COLUMN db_system.t_dict_group.id IS '主键ID';
COMMENT ON COLUMN db_system.t_dict_group.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_dict_group.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_dict_group.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_dict_group.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_dict_group.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_dict_group.pid IS '父级ID';
COMMENT ON COLUMN db_system.t_dict_group.name IS '字典名称';
COMMENT ON COLUMN db_system.t_dict_group.code IS '字典编码';
COMMENT ON COLUMN db_system.t_dict_group.state IS '字典状态';
COMMENT ON COLUMN db_system.t_dict_group.remark IS '备注';
COMMENT ON COLUMN db_system.t_dict_group.builtin IS '是否内置字段,为true则不允许他进行修改删除操作';
COMMENT ON COLUMN db_system.t_dict_group.hide IS '是否隐藏,为true则前端不可直接进行修改删除等操作';

-- 数据字典(字典值)
DROP TABLE IF EXISTS db_system.t_dict_data;
CREATE TABLE IF NOT EXISTS db_system.t_dict_data
(
    id         BIGINT PRIMARY KEY,

    gid        BIGINT       NOT NULL,
    label      VARCHAR(100) NOT NULL,
    value      VARCHAR(100) NOT NULL,
    sort       int2         NOT NULL DEFAULT 0,
    state      int2         NOT NULL,
    remark     VARCHAR(255),

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_system.t_dict_data IS '数据字典(字典值)';
COMMENT ON COLUMN db_system.t_dict_data.id IS '主键ID';
COMMENT ON COLUMN db_system.t_dict_data.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_dict_data.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_dict_data.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_dict_data.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_dict_data.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_dict_data.gid IS '字典类型ID';
COMMENT ON COLUMN db_system.t_dict_data.label IS '标签';
COMMENT ON COLUMN db_system.t_dict_data.value IS '值';
COMMENT ON COLUMN db_system.t_dict_data.sort IS '排序';
COMMENT ON COLUMN db_system.t_dict_data.state IS '状态';
COMMENT ON COLUMN db_system.t_dict_data.remark IS '备注';
