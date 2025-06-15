/*
 Navicat Premium Dump SQL

 Source Server         : 本地链接@PostgreSQL
 Source Server Type    : PostgreSQL
 Source Server Version : 170004 (170004)
 Source Host           : localhost:5432
 Source Catalog        : spectra_framework_db
 Source Schema         : db_system

 Target Server Type    : PostgreSQL
 Target Server Version : 170004 (170004)
 File Encoding         : 65001

 Date: 16/06/2025 01:03:06
*/


-- ----------------------------
-- Table structure for t_account
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_account";
CREATE TABLE "db_system"."t_account" (
  "id" int8 NOT NULL,
  "username" varchar(20) COLLATE "pg_catalog"."default",
  "password" varchar(128) COLLATE "pg_catalog"."default",
  "user_id" int8,
  "type" int2,
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_account"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_account"."username" IS '用户名';
COMMENT ON COLUMN "db_system"."t_account"."password" IS '密码';
COMMENT ON COLUMN "db_system"."t_account"."user_id" IS '用户ID';
COMMENT ON COLUMN "db_system"."t_account"."type" IS '账号类型 0-账号密码';
COMMENT ON COLUMN "db_system"."t_account"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_account"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_account"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_account"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_account"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_account" IS '账号信息';

-- ----------------------------
-- Records of t_account
-- ----------------------------
INSERT INTO "db_system"."t_account" VALUES (1927290201865945090, 'sysadmin', '$2a$10$ALzuYNgOSYLlJg/XsxUY7O4BKeqECHf5J7bY8eGPaQK.3VSlkFTaO', 1934276682383138817, 0, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for t_authority
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_authority";
CREATE TABLE "db_system"."t_authority" (
  "id" int8 NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default",
  "code" varchar(100) COLLATE "pg_catalog"."default",
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_authority"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_authority"."name" IS '权限名称';
COMMENT ON COLUMN "db_system"."t_authority"."code" IS '权限编码';
COMMENT ON COLUMN "db_system"."t_authority"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_authority"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_authority"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_authority"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_authority"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_authority" IS '权限表';

-- ----------------------------
-- Records of t_authority
-- ----------------------------

-- ----------------------------
-- Table structure for t_menu
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_menu";
CREATE TABLE "db_system"."t_menu" (
  "id" int8 NOT NULL,
  "pid" int8,
  "icon" varchar(100) COLLATE "pg_catalog"."default",
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "path" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "component" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "layout" varchar(100) COLLATE "pg_catalog"."default",
  "sort" int4 DEFAULT 0,
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_menu"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_menu"."pid" IS '父级ID';
COMMENT ON COLUMN "db_system"."t_menu"."icon" IS '图标';
COMMENT ON COLUMN "db_system"."t_menu"."name" IS '名称';
COMMENT ON COLUMN "db_system"."t_menu"."path" IS '请求路径';
COMMENT ON COLUMN "db_system"."t_menu"."component" IS '组件路径,为空则使用布局组件';
COMMENT ON COLUMN "db_system"."t_menu"."layout" IS '布局';
COMMENT ON COLUMN "db_system"."t_menu"."sort" IS '排序';
COMMENT ON COLUMN "db_system"."t_menu"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_menu"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_menu"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_menu"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_menu"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_menu" IS '菜单表';

-- ----------------------------
-- Records of t_menu
-- ----------------------------
INSERT INTO "db_system"."t_menu" VALUES (1929929620816441347, 1929928379667386370, 'icon-module', '列表示例', 'table', '/Example/Table/index', NULL, 1, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-11 09:47:10.002749', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620816441348, 1929928379667386370, 'icon-module', '表单示例', 'form', '/Example/Form/index', NULL, 2, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-11 09:47:13.831965', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620816441346, 1929928379667386370, 'icon-module', '图表示例', 'echarts', '/Example/Echarts/index', NULL, 3, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-11 09:47:18.286463', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526785, 1929928379575111682, 'icon-module', '访问控制', 'RBAC', '/System/RBAC/index', NULL, 2, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:38.885015', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526789, 1929928379575111682, 'icon-module', '菜单管理', 'menu', '/System/Menu/index', NULL, 3, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:42.708315', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526787, 1929928379575111682, 'icon-module', '字典管理', 'dict', '/System/Dict/index', NULL, 4, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:45.588571', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526790, 1929928379575111682, 'icon-module', '文件存储', 'storage', '/System/Storage/index', NULL, 5, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:48.846378', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526788, 1929928379575111682, 'icon-module', '定时任务', 'task', '/System/Task/index', NULL, 6, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:52.30893', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1932983846772363266, 1929928379575111682, 'icon-module', '部门管理', 'dept', '/System/Dept/index', NULL, 1, 1927290201865945090, '2025-06-12 10:10:36.840451', 1927290201865945090, '2025-06-12 10:10:36.840451', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620715778049, 1929928379575111682, 'icon-module', '用户管理', 'user', '/System/User/index', NULL, 0, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-13 15:32:51.681846', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929928379667386370, 0, 'icon-setting', '组件示例', '/example', 'layout', 'layout', 1, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-05 11:37:45.582763', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929928379575111682, 0, 'icon-setting', '系统管理', '/system', 'layout', 'layout', 0, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-05 11:37:45.582763', NULL);

-- ----------------------------
-- Table structure for t_operation_log
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_operation_log";
CREATE TABLE "db_system"."t_operation_log" (
  "id" int8 NOT NULL,
  "explain" text COLLATE "pg_catalog"."default",
  "status" int2,
  "ip" varchar(15) COLLATE "pg_catalog"."default",
  "method" varchar(255) COLLATE "pg_catalog"."default",
  "url" varchar(255) COLLATE "pg_catalog"."default",
  "args" json,
  "result" json,
  "time_cost" int8,
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_operation_log"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_operation_log"."explain" IS '日志说明';
COMMENT ON COLUMN "db_system"."t_operation_log"."status" IS '请求状态';
COMMENT ON COLUMN "db_system"."t_operation_log"."ip" IS '来源IP';
COMMENT ON COLUMN "db_system"."t_operation_log"."method" IS '请求方法';
COMMENT ON COLUMN "db_system"."t_operation_log"."url" IS '请求URL';
COMMENT ON COLUMN "db_system"."t_operation_log"."args" IS '请求参数';
COMMENT ON COLUMN "db_system"."t_operation_log"."result" IS '请求响应';
COMMENT ON COLUMN "db_system"."t_operation_log"."time_cost" IS '耗时';
COMMENT ON COLUMN "db_system"."t_operation_log"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_operation_log"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_operation_log"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_operation_log"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_operation_log"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_operation_log" IS '操作日志';

-- ----------------------------
-- Table structure for t_organization
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_organization";
CREATE TABLE "db_system"."t_organization" (
  "id" int8 NOT NULL,
  "pid" int8 NOT NULL DEFAULT 0,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int2 NOT NULL,
  "remark" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_organization"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_organization"."pid" IS '上级ID';
COMMENT ON COLUMN "db_system"."t_organization"."name" IS '名称';
COMMENT ON COLUMN "db_system"."t_organization"."code" IS '编码';
COMMENT ON COLUMN "db_system"."t_organization"."type" IS '类型';
COMMENT ON COLUMN "db_system"."t_organization"."remark" IS '备注';
COMMENT ON COLUMN "db_system"."t_organization"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_organization"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_organization"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_organization"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_organization"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_organization" IS '组织机构';

-- ----------------------------
-- Records of t_organization
-- ----------------------------

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_role";
CREATE TABLE "db_system"."t_role" (
  "id" int8 NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default",
  "code" varchar(100) COLLATE "pg_catalog"."default",
  "state" bool DEFAULT true,
  "scope" int2,
  "remark" varchar(255) COLLATE "pg_catalog"."default",
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_role"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_role"."name" IS '名称';
COMMENT ON COLUMN "db_system"."t_role"."code" IS '编码';
COMMENT ON COLUMN "db_system"."t_role"."state" IS '状态';
COMMENT ON COLUMN "db_system"."t_role"."scope" IS '范围';
COMMENT ON COLUMN "db_system"."t_role"."remark" IS '备注';
COMMENT ON COLUMN "db_system"."t_role"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_role"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_role"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_role"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_role"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_role" IS '角色表';

-- ----------------------------
-- Records of t_role
-- ----------------------------
INSERT INTO "db_system"."t_role" VALUES (1932682189593350146, '运维管理员', 'DEV_ADMIN', 't', 2, '运维人员使用,全局范围,拥有所有权限', 1927290201865945090, '2025-06-11 14:11:56.208812', 1927290201865945090, '2025-06-11 14:33:59.593709', NULL);
INSERT INTO "db_system"."t_role" VALUES (1932687324356775938, '小组长', 'GROUP_LEADER', 'f', 1, '测试禁用状态', 1927290201865945090, '2025-06-11 14:32:20.385948', 1927290201865945090, '2025-06-12 17:15:18.034439', NULL);
INSERT INTO "db_system"."t_role" VALUES (1932685785802162178, '系统管理员', 'SYS_ADMIN', 't', 0, '系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容', 1927290201865945090, '2025-06-11 14:26:13.572692', 1927290201865945090, '2025-06-11 14:26:13.572692', NULL);

-- ----------------------------
-- Table structure for t_role_authority_map
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_role_authority_map";
CREATE TABLE "db_system"."t_role_authority_map" (
  "id" int8 NOT NULL,
  "role_id" int8 NOT NULL,
  "authority_id" int8 NOT NULL,
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_role_authority_map"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_role_authority_map"."role_id" IS '角色ID';
COMMENT ON COLUMN "db_system"."t_role_authority_map"."authority_id" IS '权限ID';
COMMENT ON COLUMN "db_system"."t_role_authority_map"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_role_authority_map"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_role_authority_map"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_role_authority_map"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_role_authority_map"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_role_authority_map" IS '权限表<->角色';

-- ----------------------------
-- Records of t_role_authority_map
-- ----------------------------

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_user";
CREATE TABLE "db_system"."t_user" (
  "id" int8 NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default",
  "email" varchar(100) COLLATE "pg_catalog"."default",
  "avatar" varchar(100) COLLATE "pg_catalog"."default",
  "state" int2,
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_user"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_user"."name" IS '姓名';
COMMENT ON COLUMN "db_system"."t_user"."email" IS '邮箱';
COMMENT ON COLUMN "db_system"."t_user"."avatar" IS '头像';
COMMENT ON COLUMN "db_system"."t_user"."state" IS '状态';
COMMENT ON COLUMN "db_system"."t_user"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_user"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_user"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_user"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_user"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_user" IS '用户信息';

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO "db_system"."t_user" VALUES (1934276682383138817, '平台管理员', 'sysadmin@pt.com', NULL, 0, 0, '2025-06-15 23:47:52.86429', 0, '2025-06-15 23:47:52.86429', NULL);

-- ----------------------------
-- Table structure for t_user_role_map
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_user_role_map";
CREATE TABLE "db_system"."t_user_role_map" (
  "id" int8 NOT NULL,
  "user_id" int8 NOT NULL,
  "role_id" int8 NOT NULL,
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_user_role_map"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_user_role_map"."user_id" IS '用户ID';
COMMENT ON COLUMN "db_system"."t_user_role_map"."role_id" IS '角色ID';
COMMENT ON COLUMN "db_system"."t_user_role_map"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_user_role_map"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_user_role_map"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_user_role_map"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_user_role_map"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_user_role_map" IS '角色表<->账户';

-- ----------------------------
-- Records of t_user_role_map
-- ----------------------------
INSERT INTO "db_system"."t_user_role_map" VALUES (1934292480493473793, 1934276682383138817, 1932682189593350146, 1927290201865945090, '2025-06-16 00:50:39.420317', 1927290201865945090, '2025-06-16 00:50:39.420317', NULL);

-- ----------------------------
-- Uniques structure for table t_account
-- ----------------------------
ALTER TABLE "db_system"."t_account" ADD CONSTRAINT "t_account_username_key" UNIQUE ("username");

-- ----------------------------
-- Primary Key structure for table t_account
-- ----------------------------
ALTER TABLE "db_system"."t_account" ADD CONSTRAINT "t_account_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_authority
-- ----------------------------
ALTER TABLE "db_system"."t_authority" ADD CONSTRAINT "t_authority_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_menu
-- ----------------------------
ALTER TABLE "db_system"."t_menu" ADD CONSTRAINT "t_menu_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_operation_log
-- ----------------------------
ALTER TABLE "db_system"."t_operation_log" ADD CONSTRAINT "t_operation_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_organization
-- ----------------------------
ALTER TABLE "db_system"."t_organization" ADD CONSTRAINT "t_organization_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_role
-- ----------------------------
ALTER TABLE "db_system"."t_role" ADD CONSTRAINT "t_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_role_authority_map
-- ----------------------------
ALTER TABLE "db_system"."t_role_authority_map" ADD CONSTRAINT "t_role_authority_map_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_user
-- ----------------------------
ALTER TABLE "db_system"."t_user" ADD CONSTRAINT "t_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_user_role_map
-- ----------------------------
ALTER TABLE "db_system"."t_user_role_map" ADD CONSTRAINT "t_user_role_map_pkey" PRIMARY KEY ("id");
