CREATE SCHEMA IF NOT EXISTS db_system;

-- 账号信息
CREATE TABLE IF NOT EXISTS db_system.t_account
(
    id         BIGINT PRIMARY KEY,

    username   VARCHAR(20) UNIQUE,
    password   VARCHAR(128),
    enable     BOOLEAN,

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_system.t_account IS '账号信息';
COMMENT ON COLUMN db_system.t_account.id IS '主键ID';
COMMENT ON COLUMN db_system.t_account.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_account.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_account.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_account.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_account.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_account.username IS '用户名';
COMMENT ON COLUMN db_system.t_account.password IS '密码';
COMMENT ON COLUMN db_system.t_account.enable IS '是否启用';

-- 用户信息
CREATE TABLE IF NOT EXISTS db_system.t_user
(
    id         BIGINT PRIMARY KEY,

    name       VARCHAR(100),
    tel        VARCHAR(100),
    email      VARCHAR(100),
    avatar     VARCHAR(100),

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_system.t_user IS '用户信息';
COMMENT ON COLUMN db_system.t_user.id IS '主键ID';
COMMENT ON COLUMN db_system.t_user.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_user.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_user.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_user.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_user.deleted IS '是否删除';

COMMENT ON COLUMN db_system.t_user.name IS '姓名';
COMMENT ON COLUMN db_system.t_user.tel IS '电话';
COMMENT ON COLUMN db_system.t_user.email IS '邮箱';
COMMENT ON COLUMN db_system.t_user.avatar IS '头像';

-- 角色表
CREATE TABLE IF NOT EXISTS db_system.t_role
(
    id         BIGINT PRIMARY KEY,

    name       VARCHAR(100),

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_system.t_role IS '角色表';
COMMENT ON COLUMN db_system.t_role.id IS '主键ID';
COMMENT ON COLUMN db_system.t_role.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_role.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_role.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_role.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_role.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_role.name IS '角色名称';

-- 角色表<->账户
CREATE TABLE IF NOT EXISTS db_system.t_account_role_map
(
    id         BIGINT PRIMARY KEY,

    account_id BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_system.t_account_role_map IS '角色表<->账户';
COMMENT ON COLUMN db_system.t_account_role_map.id IS '主键ID';
COMMENT ON COLUMN db_system.t_account_role_map.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_account_role_map.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_account_role_map.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_account_role_map.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_account_role_map.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_account_role_map.account_id IS '账号ID';
COMMENT ON COLUMN db_system.t_account_role_map.role_id IS '角色ID';

-- 权限表
CREATE TABLE IF NOT EXISTS db_system.t_authority
(
    id         BIGINT PRIMARY KEY,

    name       VARCHAR(100),

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_system.t_authority IS '权限表';
COMMENT ON COLUMN db_system.t_authority.id IS '主键ID';
COMMENT ON COLUMN db_system.t_authority.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_authority.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_authority.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_authority.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_authority.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_authority.name IS '权限名称';

-- 权限表<->角色
CREATE TABLE IF NOT EXISTS db_system.t_role_authority_map
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
COMMENT ON TABLE db_system.t_role_authority_map IS '权限表<->角色';
COMMENT ON COLUMN db_system.t_role_authority_map.id IS '主键ID';
COMMENT ON COLUMN db_system.t_role_authority_map.created_by IS '创建人';
COMMENT ON COLUMN db_system.t_role_authority_map.created_at IS '创建时间';
COMMENT ON COLUMN db_system.t_role_authority_map.updated_by IS '最后更新人';
COMMENT ON COLUMN db_system.t_role_authority_map.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_system.t_role_authority_map.deleted IS '是否删除';
COMMENT ON COLUMN db_system.t_role_authority_map.role_id IS '角色ID';
COMMENT ON COLUMN db_system.t_role_authority_map.authority_id IS '权限ID';

-- 菜单表
CREATE TABLE IF NOT EXISTS db_system.t_menu
(
    id         BIGINT PRIMARY KEY,

    pid        BIGINT,
    icon       VARCHAR(100),
    name       VARCHAR(100) NOT NULL,
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