CREATE SCHEMA IF NOT EXISTS db_kernel;

-- 菜单表
CREATE TABLE IF NOT EXISTS db_kernel.t_menu
(
    id         BIGINT PRIMARY KEY,

    pid        BIGINT,
    icon       VARCHAR(100),
    name       VARCHAR(100) NOT NULL,
    path       VARCHAR(255) NOT NULL,
    component  VARCHAR(100) NOT NULL,
    layout     VARCHAR(100) NOT NULL DEFAULT 'layout',
    sort       INT                   DEFAULT 0,

    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted    TIMESTAMP
);
COMMENT ON TABLE db_kernel.t_menu IS '菜单表';
COMMENT ON COLUMN db_kernel.t_menu.id IS '主键ID';
COMMENT ON COLUMN db_kernel.t_menu.created_by IS '创建人';
COMMENT ON COLUMN db_kernel.t_menu.created_at IS '创建时间';
COMMENT ON COLUMN db_kernel.t_menu.updated_by IS '最后更新人';
COMMENT ON COLUMN db_kernel.t_menu.updated_at IS '最后更新时间';
COMMENT ON COLUMN db_kernel.t_menu.deleted IS '是否删除';

COMMENT ON COLUMN db_kernel.t_menu.pid IS '父级ID';
COMMENT ON COLUMN db_kernel.t_menu.icon IS '图标';
COMMENT ON COLUMN db_kernel.t_menu.name IS '名称';
COMMENT ON COLUMN db_kernel.t_menu.path IS '请求路径';
COMMENT ON COLUMN db_kernel.t_menu.component IS '组件路径,为空则使用布局组件';
COMMENT ON COLUMN db_kernel.t_menu.layout IS '布局';
COMMENT ON COLUMN db_kernel.t_menu.sort IS '排序';