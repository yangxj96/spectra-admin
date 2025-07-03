/*
 Navicat Premium Dump SQL

 Source Server         : PostgreSQL@localhost
 Source Server Type    : PostgreSQL
 Source Server Version : 170004 (170004)
 Source Host           : localhost:5432
 Source Catalog        : spectra_framework_db
 Source Schema         : db_auth

 Target Server Type    : PostgreSQL
 Target Server Version : 170004 (170004)
 File Encoding         : 65001

 Date: 03/07/2025 15:36:48
*/


-- ----------------------------
-- Table structure for t_account
-- ----------------------------
DROP TABLE IF EXISTS "db_auth"."t_account";
CREATE TABLE "db_auth"."t_account" (
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
COMMENT ON COLUMN "db_auth"."t_account"."id" IS '主键ID';
COMMENT ON COLUMN "db_auth"."t_account"."username" IS '用户名';
COMMENT ON COLUMN "db_auth"."t_account"."password" IS '密码';
COMMENT ON COLUMN "db_auth"."t_account"."user_id" IS '用户ID';
COMMENT ON COLUMN "db_auth"."t_account"."type" IS '账号类型 0-账号密码';
COMMENT ON COLUMN "db_auth"."t_account"."created_by" IS '创建人';
COMMENT ON COLUMN "db_auth"."t_account"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_auth"."t_account"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_auth"."t_account"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_auth"."t_account"."deleted" IS '是否删除';
COMMENT ON TABLE "db_auth"."t_account" IS '账号信息';

-- ----------------------------
-- Records of t_account
-- ----------------------------
INSERT INTO "db_auth"."t_account" VALUES (1927290201865945090, 'yangxj96@gmail.com', '$2a$10$ALzuYNgOSYLlJg/XsxUY7O4BKeqECHf5J7bY8eGPaQK.3VSlkFTaO', 1934276682383138817, 0, NULL, NULL, 1927290201865945090, '2025-06-24 11:37:14.243354', NULL);
INSERT INTO "db_auth"."t_account" VALUES (1937354421099782146, 'sysadmin@1.com', '$2a$10$zQSrfeQHvHw022UFUOoJwe5oHdOAWcaZr8d2owbbCwAgWqOSjVFVa', 1937354420709711873, 0, 1927290201865945090, '2025-06-24 11:37:42.957695', 1927290201865945090, '2025-06-24 11:37:42.957695', NULL);
INSERT INTO "db_auth"."t_account" VALUES (1937706587203239938, 'ceshi@1.com', '$2a$10$UhexxdMYdvPFokOuBO2va.hG4mxzjvXRGufSmnLtRYJrirY8Bw4km', 1937706586813169665, 0, 1927290201865945090, '2025-06-25 10:57:05.907345', 1927290201865945090, '2025-06-25 10:57:05.911344', NULL);

-- ----------------------------
-- Table structure for t_authority
-- ----------------------------
DROP TABLE IF EXISTS "db_auth"."t_authority";
CREATE TABLE "db_auth"."t_authority" (
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
COMMENT ON COLUMN "db_auth"."t_authority"."id" IS '主键ID';
COMMENT ON COLUMN "db_auth"."t_authority"."name" IS '权限名称';
COMMENT ON COLUMN "db_auth"."t_authority"."code" IS '权限编码';
COMMENT ON COLUMN "db_auth"."t_authority"."created_by" IS '创建人';
COMMENT ON COLUMN "db_auth"."t_authority"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_auth"."t_authority"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_auth"."t_authority"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_auth"."t_authority"."deleted" IS '是否删除';
COMMENT ON TABLE "db_auth"."t_authority" IS '权限表';

-- ----------------------------
-- Records of t_authority
-- ----------------------------

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS "db_auth"."t_role";
CREATE TABLE "db_auth"."t_role" (
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
COMMENT ON COLUMN "db_auth"."t_role"."id" IS '主键ID';
COMMENT ON COLUMN "db_auth"."t_role"."name" IS '名称';
COMMENT ON COLUMN "db_auth"."t_role"."code" IS '编码';
COMMENT ON COLUMN "db_auth"."t_role"."state" IS '状态';
COMMENT ON COLUMN "db_auth"."t_role"."scope" IS '范围';
COMMENT ON COLUMN "db_auth"."t_role"."remark" IS '备注';
COMMENT ON COLUMN "db_auth"."t_role"."created_by" IS '创建人';
COMMENT ON COLUMN "db_auth"."t_role"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_auth"."t_role"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_auth"."t_role"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_auth"."t_role"."deleted" IS '是否删除';
COMMENT ON TABLE "db_auth"."t_role" IS '角色表';

-- ----------------------------
-- Records of t_role
-- ----------------------------
INSERT INTO "db_auth"."t_role" VALUES (1932682189593350146, '运维管理员', 'DEV_ADMIN', 't', 2, '运维人员使用,全局范围,拥有所有权限', 1927290201865945090, '2025-06-11 14:11:56.208812', 1927290201865945090, '2025-06-11 14:33:59.593709', NULL);
INSERT INTO "db_auth"."t_role" VALUES (1932687324356775938, '小组长', 'GROUP_LEADER', 'f', 1, '测试禁用状态', 1927290201865945090, '2025-06-11 14:32:20.385948', 1927290201865945090, '2025-06-12 17:15:18.034439', NULL);
INSERT INTO "db_auth"."t_role" VALUES (1932685785802162178, '系统管理员', 'SYS_ADMIN', 't', 0, '系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容', 1927290201865945090, '2025-06-11 14:26:13.572692', 1927290201865945090, '2025-06-11 14:26:13.572692', NULL);

-- ----------------------------
-- Table structure for t_role_authority_map
-- ----------------------------
DROP TABLE IF EXISTS "db_auth"."t_role_authority_map";
CREATE TABLE "db_auth"."t_role_authority_map" (
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
COMMENT ON COLUMN "db_auth"."t_role_authority_map"."id" IS '主键ID';
COMMENT ON COLUMN "db_auth"."t_role_authority_map"."role_id" IS '角色ID';
COMMENT ON COLUMN "db_auth"."t_role_authority_map"."authority_id" IS '权限ID';
COMMENT ON COLUMN "db_auth"."t_role_authority_map"."created_by" IS '创建人';
COMMENT ON COLUMN "db_auth"."t_role_authority_map"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_auth"."t_role_authority_map"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_auth"."t_role_authority_map"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_auth"."t_role_authority_map"."deleted" IS '是否删除';
COMMENT ON TABLE "db_auth"."t_role_authority_map" IS '权限表<->角色';

-- ----------------------------
-- Records of t_role_authority_map
-- ----------------------------

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS "db_auth"."t_user";
CREATE TABLE "db_auth"."t_user" (
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
COMMENT ON COLUMN "db_auth"."t_user"."id" IS '主键ID';
COMMENT ON COLUMN "db_auth"."t_user"."name" IS '姓名';
COMMENT ON COLUMN "db_auth"."t_user"."email" IS '邮箱';
COMMENT ON COLUMN "db_auth"."t_user"."avatar" IS '头像';
COMMENT ON COLUMN "db_auth"."t_user"."state" IS '状态';
COMMENT ON COLUMN "db_auth"."t_user"."created_by" IS '创建人';
COMMENT ON COLUMN "db_auth"."t_user"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_auth"."t_user"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_auth"."t_user"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_auth"."t_user"."deleted" IS '是否删除';
COMMENT ON TABLE "db_auth"."t_user" IS '用户信息';

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO "db_auth"."t_user" VALUES (1937354420709711873, '超级管理员', 'sysadmin@1.com', NULL, 0, 1927290201865945090, '2025-06-24 11:37:42.866695', 1927290201865945090, '2025-06-24 11:41:34.424012', NULL);
INSERT INTO "db_auth"."t_user" VALUES (1937706586813169665, '测试用户', 'ceshi@1.com', NULL, 0, 1927290201865945090, '2025-06-25 10:57:05.810342', 1927290201865945090, '2025-06-25 10:57:05.811358', NULL);
INSERT INTO "db_auth"."t_user" VALUES (1934276682383138817, '平台管理员', 'yangxj96@gmail.com', NULL, 0, NULL, '2025-06-15 23:47:52.86429', 1927290201865945090, '2025-06-24 11:37:14.216352', NULL);

-- ----------------------------
-- Table structure for t_user_role_map
-- ----------------------------
DROP TABLE IF EXISTS "db_auth"."t_user_role_map";
CREATE TABLE "db_auth"."t_user_role_map" (
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
COMMENT ON COLUMN "db_auth"."t_user_role_map"."id" IS '主键ID';
COMMENT ON COLUMN "db_auth"."t_user_role_map"."user_id" IS '用户ID';
COMMENT ON COLUMN "db_auth"."t_user_role_map"."role_id" IS '角色ID';
COMMENT ON COLUMN "db_auth"."t_user_role_map"."created_by" IS '创建人';
COMMENT ON COLUMN "db_auth"."t_user_role_map"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_auth"."t_user_role_map"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_auth"."t_user_role_map"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_auth"."t_user_role_map"."deleted" IS '是否删除';
COMMENT ON TABLE "db_auth"."t_user_role_map" IS '角色表<->账户';

-- ----------------------------
-- Records of t_user_role_map
-- ----------------------------
INSERT INTO "db_auth"."t_user_role_map" VALUES (1934292480493473793, 1934276682383138817, 1932682189593350146, NULL, '2025-06-16 00:50:39.420317', NULL, '2025-06-16 00:50:39.420317', NULL);
INSERT INTO "db_auth"."t_user_role_map" VALUES (1937706587291320321, 1937706586813169665, 1932687324356775938, NULL, '2025-06-25 10:57:05.812216', NULL, '2025-06-25 10:57:05.812216', NULL);
INSERT INTO "db_auth"."t_user_role_map" VALUES (1937354421099782147, 1937354420709711873, 1932685785802162178, NULL, '2025-06-24 11:37:42.867752', NULL, '2025-06-24 11:37:42.867752', NULL);

-- ----------------------------
-- Uniques structure for table t_account
-- ----------------------------
ALTER TABLE "db_auth"."t_account" ADD CONSTRAINT "t_account_username_key" UNIQUE ("username");

-- ----------------------------
-- Primary Key structure for table t_account
-- ----------------------------
ALTER TABLE "db_auth"."t_account" ADD CONSTRAINT "t_account_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_authority
-- ----------------------------
ALTER TABLE "db_auth"."t_authority" ADD CONSTRAINT "t_authority_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table t_role
-- ----------------------------
ALTER TABLE "db_auth"."t_role" ADD CONSTRAINT "t_role_code_key" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table t_role
-- ----------------------------
ALTER TABLE "db_auth"."t_role" ADD CONSTRAINT "t_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_role_authority_map
-- ----------------------------
ALTER TABLE "db_auth"."t_role_authority_map" ADD CONSTRAINT "t_role_authority_map_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table t_user
-- ----------------------------
ALTER TABLE "db_auth"."t_user" ADD CONSTRAINT "t_user_email_key" UNIQUE ("email");

-- ----------------------------
-- Primary Key structure for table t_user
-- ----------------------------
ALTER TABLE "db_auth"."t_user" ADD CONSTRAINT "t_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_user_role_map
-- ----------------------------
ALTER TABLE "db_auth"."t_user_role_map" ADD CONSTRAINT "t_user_role_map_pkey" PRIMARY KEY ("id");
