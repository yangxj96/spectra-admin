-- 账号表
DROP TABLE IF EXISTS DB_AUTH."sys_account" CASCADE;
CREATE TABLE DB_AUTH."sys_account"
(
    "id"         BIGINT      NOT NULL COMMENT '主键ID',
    "username"   VARCHAR(20) NOT NULL COMMENT '用户名',
    "password"   VARCHAR(60) NOT NULL COMMENT '密码',
    "user_id"    BIGINT      NOT NULL COMMENT '用户ID',
    "type"       INT         NOT NULL COMMENT '账号类型',
    "created_by" BIGINT COMMENT '创建人',
    "created_at" TIMESTAMP   NOT NULL COMMENT '创建时间',
    "updated_by" BIGINT COMMENT '最后更新人',
    "updated_at" TIMESTAMP   NOT NULL COMMENT '最后更新时间',
    "deleted"    TIMESTAMP COMMENT '删除时间',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_AUTH."sys_account" IS '账号表';

INSERT INTO DB_AUTH."sys_account" VALUES (1927290201865945090, 'yangxj96@gmail.com', '$2a$10$ALzuYNgOSYLlJg/XsxUY7O4BKeqECHf5J7bY8eGPaQK.3VSlkFTaO', 1934276682383138817, 0, NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000', NULL);
INSERT INTO DB_AUTH."sys_account" VALUES (1937354421099782146, 'sysadmin@1.com'    , '$2a$10$zQSrfeQHvHw022UFUOoJwe5oHdOAWcaZr8d2owbbCwAgWqOSjVFVa', 1937354420709711873, 0, NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000', NULL);
INSERT INTO DB_AUTH."sys_account" VALUES (1937706587203239938, 'ceshi@1.com'       , '$2a$10$UhexxdMYdvPFokOuBO2va.hG4mxzjvXRGufSmnLtRYJrirY8Bw4km', 1937706586813169665, 0, NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000', NULL);

-- 权限表
DROP TABLE IF EXISTS DB_AUTH."sys_authority";
CREATE TABLE DB_AUTH."sys_authority"
(
    "id"         BIGINT       NOT NULL COMMENT '主键ID',
    "pid"        BIGINT COMMENT '父级ID,用于构建树形结构',
    "name"       VARCHAR(100) NOT NULL COMMENT '权限名称',
    "code"       VARCHAR(100) NOT NULL COMMENT '权限编码',
    "created_by" BIGINT COMMENT '创建人',
    "created_at" TIMESTAMP    NOT NULL COMMENT '创建时间',
    "updated_by" BIGINT COMMENT '最后更新人',
    "updated_at" TIMESTAMP    NOT NULL COMMENT '最后更新时间',
    "deleted"    TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_AUTH."sys_authority" IS '权限表';

INSERT INTO DB_AUTH."sys_authority"VALUES (1943513441539891202, NULL               , '字典权限', 'MENU:*'     , NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000', NULL);
INSERT INTO DB_AUTH."sys_authority"VALUES (1943513442269700097, 1943513441539891202, '字典新增', 'MENU:INSERT', NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000',NULL);
INSERT INTO DB_AUTH."sys_authority"VALUES (1943513442269700098, 1943513441539891202, '字典修改', 'MENU:UPDATE', NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000',NULL);
INSERT INTO DB_AUTH."sys_authority"VALUES (1943513442269700099, 1943513441539891202, '字典删除', 'MENU:DELETE', NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000',NULL);

-- 角色表
DROP TABLE IF EXISTS DB_AUTH."sys_role";
CREATE TABLE DB_AUTH."sys_role"
(
    "id"         BIGINT    NOT NULL COMMENT '主键ID',
    "name"       VARCHAR(100) COMMENT '名称',
    "code"       VARCHAR(100) COMMENT '编码',
    "state"      BIT DEFAULT 1 COMMENT '状态',
    "scope"      INT COMMENT '范围',
    "remark"     VARCHAR(255) COMMENT '备注',
    "created_by" BIGINT COMMENT '创建人',
    "created_at" TIMESTAMP NOT NULL COMMENT '创建时间',
    "updated_by" BIGINT COMMENT '最后更新人',
    "updated_at" TIMESTAMP NOT NULL COMMENT '最后更新时间',
    "deleted"    TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_AUTH."sys_role" IS '角色表';

INSERT INTO DB_AUTH."sys_role" VALUES (1932682189593350146,'运维管理员','DEV_ADMIN',1,2,'运维人员使用,全局范围,拥有所有权限',NULL,'2025-01-01 00:00:00.000000',NULL,'2025-01-01 00:00:00.000000',NULL);
INSERT INTO DB_AUTH."sys_role" VALUES (1932687324356775938,'小组长','GROUP_LEADER',0,1,'测试禁用状态',NULL,'2025-01-01 00:00:00.000000',NULL,'2025-01-01 00:00:00.000000',NULL);
INSERT INTO DB_AUTH."sys_role" VALUES (1932685785802162178,'系统管理员','SYS_ADMIN',1,0,'系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容',NULL,'2025-01-01 00:00:00.000000',NULL,'2025-01-01 00:00:00.000000',NULL);


-- 权限表<->角色
DROP TABLE IF EXISTS DB_AUTH."sys_role_authority_map";
CREATE TABLE DB_AUTH."sys_role_authority_map"
(
    "id"           BIGINT NOT NULL COMMENT '主键ID',
    "role_id"      BIGINT NOT NULL COMMENT '角色ID',
    "authority_id" BIGINT NOT NULL COMMENT '权限ID',
    "created_by"   BIGINT COMMENT '创建人',
    "created_at"   TIMESTAMP COMMENT '创建时间',
    "updated_by"   BIGINT COMMENT '最后更新人',
    "updated_at"   TIMESTAMP COMMENT '最后更新时间',
    "deleted"      TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_AUTH."sys_role_authority_map" IS '权限表<->角色';

-- 用户信息
DROP TABLE IF EXISTS DB_AUTH."sys_user";
CREATE TABLE DB_AUTH."sys_user"
(
    "id"              BIGINT NOT NULL COMMENT '主键ID',
    "name"            VARCHAR(100) COMMENT '姓名',
    "email"           VARCHAR(100) COMMENT '邮箱',
    "avatar"          VARCHAR(100) COMMENT '头像',
    "state"           BIT DEFAULT 0 COMMENT '状态',
    "organization_id" BIGINT COMMENT '所属组织机构ID',
    "created_by"      BIGINT COMMENT '创建人',
    "created_at"      TIMESTAMP COMMENT '创建时间',
    "updated_by"      BIGINT COMMENT '最后更新人',
    "updated_at"      TIMESTAMP COMMENT '最后更新时间',
    "deleted"         TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_AUTH."sys_user" IS '用户信息';

INSERT INTO DB_AUTH."sys_user" VALUES (1937354420709711873, '超级管理员', 'sysadmin@1.com', NULL, 0,NULL, 1927290201865945090, '2025-01-01 00:00:00.000000', 1927290201865945090, '2025-01-01 00:00:00.000000', NULL);
INSERT INTO DB_AUTH."sys_user" VALUES (1937706586813169665, '测试用户', 'ceshi@1.com', NULL, 0,NULL, 1927290201865945090, '2025-01-01 00:00:00.000000', 1927290201865945090, '2025-01-01 00:00:00.000000', NULL);
INSERT INTO DB_AUTH."sys_user" VALUES (1934276682383138817, '平台管理员', 'yangxj96@gmail.com', NULL, 0,1944679712507760641, NULL, '2025-01-01 00:00:00.000000', 1927290201865945090, '2025-01-01 00:00:00.000000', NULL);


-- 角色表<->账户
DROP TABLE IF EXISTS DB_AUTH."sys_user_role_map";
CREATE TABLE DB_AUTH."sys_user_role_map"
(
    "id"         BIGINT NOT NULL COMMENT '主键ID',
    "user_id"    BIGINT NOT NULL COMMENT '用户ID',
    "role_id"    BIGINT NOT NULL COMMENT '角色ID',
    "created_by" BIGINT COMMENT '创建人',
    "created_at" TIMESTAMP COMMENT '创建时间',
    "updated_by" BIGINT COMMENT '最后更新人',
    "updated_at" TIMESTAMP COMMENT '最后更新时间',
    "deleted"    TIMESTAMP COMMENT '是否删除',
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_AUTH."sys_user_role_map" IS '角色表<->账户';

INSERT INTO DB_AUTH."sys_user_role_map" VALUES (1934292480493473793, 1934276682383138817, 1932682189593350146, NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000', NULL);
INSERT INTO DB_AUTH."sys_user_role_map" VALUES (1937706587291320321, 1937706586813169665, 1932687324356775938, NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000', NULL);
INSERT INTO DB_AUTH."sys_user_role_map" VALUES (1937354421099782147, 1937354420709711873, 1932685785802162178, NULL, '2025-01-01 00:00:00.000000', NULL, '2025-01-01 00:00:00.000000', NULL);
