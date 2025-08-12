-- 字典项
DROP TABLE IF EXISTS DB_SYS."sys_dict_data";
CREATE TABLE DB_SYS."sys_dict_data"
(
    "id"         BIGINT       NOT NULL COMMENT '主键ID',
    "gid"        BIGINT       NOT NULL COMMENT '字典组ID',
    "label"      VARCHAR(100) NOT NULL COMMENT '标签',
    "value"      VARCHAR(100) NOT NULL COMMENT '值',
    "sort"       SMALLINT     NOT NULL DEFAULT 0 COMMENT '排序',
    "state"      SMALLINT     NOT NULL COMMENT '状态',
    "remark"     VARCHAR(255) COMMENT '备注',
    "created_by" BIGINT COMMENT '创建人',
    "created_at" TIMESTAMP    NOT NULL COMMENT '创建时间',
    "updated_by" BIGINT COMMENT '最后更新人',
    "updated_at" TIMESTAMP    NOT NULL COMMENT '最后更新时间',
    "deleted"    TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_SYS."sys_dict_data" IS '数据字典(字典值)';


-- 字典组
DROP TABLE IF EXISTS DB_SYS."sys_dict_group";
CREATE TABLE DB_SYS."sys_dict_group"
(
    "id"         BIGINT       NOT NULL COMMENT '主键ID',
    "pid"        BIGINT COMMENT '父级ID',
    "name"       VARCHAR(100) NOT NULL COMMENT '字典名称',
    "code"       VARCHAR(100) NOT NULL COMMENT '字典编码',
    "state"      SMALLINT     NOT NULL COMMENT '字典状态',
    "remark"     VARCHAR(255) COMMENT '备注',
    "builtin"    BIT          NOT NULL DEFAULT 0 COMMENT '是否内置字段,为true则不允许他进行修改删除操作',
    "hide"       BIT          NOT NULL DEFAULT 0 COMMENT '是否隐藏,为true则前端不可直接进行修改删除等操作',
    "created_by" BIGINT COMMENT '创建人',
    "created_at" TIMESTAMP    NOT NULL COMMENT '创建时间',
    "updated_by" BIGINT COMMENT '最后更新人',
    "updated_at" TIMESTAMP    NOT NULL COMMENT '最后更新时间',
    "deleted"    TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_SYS."sys_dict_group" IS '数据字典(字典类型)';

-- 菜单
DROP TABLE IF EXISTS DB_SYS."sys_menu";
CREATE TABLE DB_SYS."sys_menu"
(
    "id"         BIGINT       NOT NULL COMMENT '主键ID',
    "name"       VARCHAR(100) NOT NULL COMMENT '名称',
    "pid"        BIGINT COMMENT '父级ID',
    "icon"       VARCHAR(100) COMMENT '图标',
    "path"       VARCHAR(255) NOT NULL COMMENT '请求路径',
    "component"  VARCHAR(100) NOT NULL COMMENT '组件路径,为空则使用布局组件',
    "layout"     VARCHAR(100) COMMENT '布局',
    "sort"       INTEGER DEFAULT 0 COMMENT '排序',
    "created_by" BIGINT COMMENT '创建人',
    "created_at" TIMESTAMP    NOT NULL COMMENT '创建时间',
    "updated_by" BIGINT COMMENT '最后更新人',
    "updated_at" TIMESTAMP    NOT NULL COMMENT '最后更新时间',
    "deleted"    TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_SYS."sys_menu" IS '菜单表';

-- 操作日志
DROP TABLE IF EXISTS DB_SYS."sys_operation_log";
CREATE TABLE DB_SYS."sys_operation_log"
(
    "id"         BIGINT    NOT NULL COMMENT '主键ID',
    "explain"    TEXT COMMENT '日志说明',
    "status"     SMALLINT COMMENT '请求状态',
    "ip"         VARCHAR(15) COMMENT '来源IP',
    "method"     VARCHAR(255) COMMENT '请求方法',
    "url"        VARCHAR(255) COMMENT '请求URL',
    "args"       JSON COMMENT '请求参数',
    "result"     JSON COMMENT '请求响应',
    "time_cost"  BIGINT COMMENT '耗时',
    "created_by" BIGINT COMMENT '创建人',
    "created_at" TIMESTAMP NOT NULL COMMENT '创建时间',
    "updated_by" BIGINT COMMENT '最后更新人',
    "updated_at" TIMESTAMP NOT NULL COMMENT '最后更新时间',
    "deleted"    TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_SYS."sys_operation_log" IS '操作日志表';

-- 组织机构
DROP TABLE IF EXISTS DB_SYS."sys_organization";
CREATE TABLE DB_SYS."sys_organization"
(
    "id"         BIGINT       NOT NULL COMMENT '主键ID',
    "pid"        BIGINT COMMENT '上级ID',
    "name"       VARCHAR(100) NOT NULL COMMENT '名称',
    "code"       VARCHAR(100) NOT NULL COMMENT '编码',
    "type"       SMALLINT COMMENT '公司类型',
    "address"    VARCHAR(255) NOT NULL COMMENT '注册地址',
    "manager_id" BIGINT COMMENT '负责人ID',
    "remark"     VARCHAR(255) COMMENT '备注',
    "created_by" BIGINT COMMENT '创建人',
    "created_at" TIMESTAMP    NOT NULL COMMENT '创建时间',
    "updated_by" BIGINT COMMENT '最后更新人',
    "updated_at" TIMESTAMP    NOT NULL COMMENT '最后更新时间',
    "deleted"    TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_SYS."sys_organization" IS '组织机构';
