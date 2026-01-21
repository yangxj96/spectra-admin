/*
 Navicat Premium Dump SQL

 Source Server         : 127.0.0.1@PostgreSQL
 Source Server Type    : PostgreSQL
 Source Server Version : 180000 (180000)
 Source Host           : 127.0.0.1:5432
 Source Catalog        : spectra_db
 Source Schema         : domain_core

 Target Server Type    : PostgreSQL
 Target Server Version : 180000 (180000)
 File Encoding         : 65001

 Date: 21/01/2026 14:53:02
*/


-- ----------------------------
-- Table structure for sys_account
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_account";
CREATE TABLE "domain_core"."sys_account" (
  "id" int8 NOT NULL,
  "user_id" int8 NOT NULL,
  "type" int4 NOT NULL,
  "login_name" varchar(100) COLLATE "pg_catalog"."default",
  "password" varchar(255) COLLATE "pg_catalog"."default",
  "phone" varchar(20) COLLATE "pg_catalog"."default",
  "email" varchar(100) COLLATE "pg_catalog"."default",
  "openid" varchar(100) COLLATE "pg_catalog"."default",
  "unionid" varchar(100) COLLATE "pg_catalog"."default",
  "provider" varchar(50) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 1,
  "verified" int2 NOT NULL DEFAULT 0,
  "expires_at" timestamptz(6),
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_account"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_account"."user_id" IS '用户ID';
COMMENT ON COLUMN "domain_core"."sys_account"."type" IS '账号类型';
COMMENT ON COLUMN "domain_core"."sys_account"."login_name" IS '用户名（用于账号密码登录）';
COMMENT ON COLUMN "domain_core"."sys_account"."password" IS '密码(仅用作账号密码登录)';
COMMENT ON COLUMN "domain_core"."sys_account"."phone" IS '手机号（用于短信登录）';
COMMENT ON COLUMN "domain_core"."sys_account"."email" IS '邮箱（用于邮箱验证码登录）';
COMMENT ON COLUMN "domain_core"."sys_account"."openid" IS '微信 openid';
COMMENT ON COLUMN "domain_core"."sys_account"."unionid" IS '微信 unionid（跨应用唯一）';
COMMENT ON COLUMN "domain_core"."sys_account"."provider" IS '第三方来源：WECHAT, ALIPAY, APPLE 等';
COMMENT ON COLUMN "domain_core"."sys_account"."status" IS '1:正常 2:禁用 3:未验证';
COMMENT ON COLUMN "domain_core"."sys_account"."verified" IS '0:未验证 1:已验证（如手机号/邮箱）';
COMMENT ON COLUMN "domain_core"."sys_account"."expires_at" IS '用于临时账号（如扫码未确认）';
COMMENT ON COLUMN "domain_core"."sys_account"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_account"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_account"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_account"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_account"."deleted" IS '删除标识';
COMMENT ON COLUMN "domain_core"."sys_account"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_account" IS '用户账号表';

-- ----------------------------
-- Records of sys_account
-- ----------------------------
INSERT INTO "domain_core"."sys_account" VALUES (1999056546640613378, 1999056543696211969, 1, 'devops@devops00.com', '$2a$10$eK//x244OcYDf9iMdJ05L.03nT3GZauoThsbiDy0UrdkH8.QGzqzm', NULL, NULL, NULL, NULL, 'DEFAULT', 1, 0, NULL, 1999056543696211969, '2025-12-11 09:59:56.050555+08', 1999056543696211969, '2025-12-19 08:07:13.01758+08', NULL, 1);
INSERT INTO "domain_core"."sys_account" VALUES (2001897906657751042, 2001897905181356033, 1, 'admin@devops00.com', '$2a$10$wmsvxTtfcksATsQwbEowce09EtISFfioU5V2679V/0s3JDzEHjS4S', NULL, NULL, NULL, NULL, 'DEFAULT', 1, 0, NULL, 1999056543696211969, '2025-12-16 22:10:29.069816+08', 1999056543696211969, '2025-12-19 08:06:57.31997+08', NULL, 7);
INSERT INTO "domain_core"."sys_account" VALUES (2001929942059626497, 2001929941732470786, 1, 'user@devops00.com', '$2a$10$IOME9btF6TY/GL5g802PaeibnZESkM.KDgRDXdk0hEbXVPJgb5wlm', NULL, NULL, NULL, NULL, 'DEFAULT', 1, 0, NULL, 1999056543696211969, '2025-12-19 08:17:46.911867+08', 1999056543696211969, '2025-12-19 08:17:46.911867+08', NULL, 0);
INSERT INTO "domain_core"."sys_account" VALUES (2001930105973026817, 2001930105641676801, 1, 'audit@devops00.com', '$2a$10$yjf3/ybKsZmlK0RdePp7subsl4Cwh7cA/hccPuNBZSpvwr3A.FK.S', NULL, NULL, NULL, NULL, 'DEFAULT', 1, 0, NULL, 1999056543696211969, '2025-12-19 00:18:25.992541+08', 1999056543696211969, '2025-12-19 08:25:47.908017+08', NULL, 1);

-- ----------------------------
-- Table structure for sys_authority
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_authority";
CREATE TABLE "domain_core"."sys_authority" (
  "id" int8 NOT NULL,
  "pid" int8,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_authority"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_authority"."pid" IS '父级ID,用于构建树形结构';
COMMENT ON COLUMN "domain_core"."sys_authority"."name" IS '权限名称';
COMMENT ON COLUMN "domain_core"."sys_authority"."code" IS '权限编码';
COMMENT ON COLUMN "domain_core"."sys_authority"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_authority"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_authority"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_authority"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_authority"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_authority"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_authority" IS '权限表';

-- ----------------------------
-- Records of sys_authority
-- ----------------------------
INSERT INTO "domain_core"."sys_authority" VALUES (1943513441539891202, 1, '菜单权限', 'MENU:*', NULL, '2025-07-11 11:31:28+08', NULL, '2025-07-11 11:31:28+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1943513442269700097, 1943513441539891202, '菜单新增', 'MENU:INSERT', NULL, '2025-07-11 11:31:28+08', NULL, '2025-07-11 11:31:28+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1943513442269700098, 1943513441539891202, '菜单修改', 'MENU:UPDATE', NULL, '2025-07-11 11:31:28+08', NULL, '2025-07-11 11:31:28+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1943513442269700099, 1943513441539891202, '菜单删除', 'MENU:DELETE', NULL, '2025-07-11 11:31:28+08', NULL, '2025-07-11 11:31:28+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1, NULL, '顶级权限', '*', NULL, '2025-01-01 00:00:00+08', NULL, '2025-01-01 00:00:00+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963176940012478465, 1, '字典管理', 'DICT:*', NULL, '2025-09-03 17:47:11.22402+08', NULL, '2025-09-03 17:47:11.22402+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963176940029255681, 1963176940012478465, '字典新增', 'DICT:INSERT', NULL, '2025-09-03 17:47:11.236019+08', NULL, '2025-09-03 17:47:11.236019+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963176940029255682, 1963176940012478465, '字典删除', 'DICT:DELETE', NULL, '2025-09-03 17:47:11.238019+08', NULL, '2025-09-03 17:47:11.238019+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963176940096364545, 1963176940012478465, '字典修改', 'DICT:UPDATE', NULL, '2025-09-03 17:47:11.24202+08', NULL, '2025-09-03 17:47:11.24202+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963505328908578818, 1, '部门管理', 'DEPT:*', NULL, '2025-09-04 15:32:05.236159+08', NULL, '2025-09-04 15:32:05.236159+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963505328975687682, 1963505328908578818, '部门新增', 'DEPT:INSERT', NULL, '2025-09-04 15:32:05.253125+08', NULL, '2025-09-04 15:32:05.253125+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963505328975687683, 1963505328908578818, '部门删除', 'DEPT:DELETE', NULL, '2025-09-04 15:32:05.256124+08', NULL, '2025-09-04 15:32:05.256124+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963505328975687684, 1963505328908578818, '部门修改', 'DEPT:UPDATE', NULL, '2025-09-04 15:32:05.259127+08', NULL, '2025-09-04 15:32:05.259127+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963505678168293377, 1, '用户管理', 'USER:*', NULL, '2025-09-04 15:33:28.507986+08', NULL, '2025-09-04 15:33:28.507986+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963505678197653505, 1963505678168293377, '用户新增', 'USER:INSERT', NULL, '2025-09-04 15:33:28.521987+08', NULL, '2025-09-04 15:33:28.521987+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963505678197653506, 1963505678168293377, '用户删除', 'USER:DELETE', NULL, '2025-09-04 15:33:28.525988+08', NULL, '2025-09-04 15:33:28.525988+08', NULL, 0);
INSERT INTO "domain_core"."sys_authority" VALUES (1963505678264762369, 1963505678168293377, '用户修改', 'USER:UPDATE', NULL, '2025-09-04 15:33:28.527985+08', NULL, '2025-09-04 15:33:28.528987+08', NULL, 0);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_config";
CREATE TABLE "domain_core"."sys_config" (
  "id" int8 NOT NULL,
  "key" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "value" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int4 NOT NULL,
  "dict_code" varchar(255) COLLATE "pg_catalog"."default",
  "remarks" varchar(255) COLLATE "pg_catalog"."default",
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_config"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_config"."key" IS '配置key';
COMMENT ON COLUMN "domain_core"."sys_config"."value" IS '配置VALUE';
COMMENT ON COLUMN "domain_core"."sys_config"."type" IS '值类型';
COMMENT ON COLUMN "domain_core"."sys_config"."dict_code" IS '字典组CODE,可能会有选项之类的,直接关联一个字典做下拉选项';
COMMENT ON COLUMN "domain_core"."sys_config"."remarks" IS '备注说明';
COMMENT ON COLUMN "domain_core"."sys_config"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_config"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_config"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_config"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_config"."deleted" IS '删除时间';
COMMENT ON COLUMN "domain_core"."sys_config"."version" IS '乐观锁版本号,默认0';
COMMENT ON TABLE "domain_core"."sys_config" IS '系统配置表';

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO "domain_core"."sys_config" VALUES (1986321942552965122, 'system.watermark.enable', 'true', 1, NULL, '是否开开启', 0, '2025-11-03 22:37:09.843733+08', 1999056543696211969, '2025-12-25 08:54:36.340247+08', NULL, 8);
INSERT INTO "domain_core"."sys_config" VALUES (1986321942653628418, 'system.watermark.type', '1', 2, 'sys_watermark', '水印类型,1-系统生成 2-固定值', 0, '2025-11-04 10:37:09.864778+08', 1999056543696211969, '2025-12-25 08:53:35.697626+08', NULL, 5);
INSERT INTO "domain_core"."sys_config" VALUES (1986321942678794242, 'system.watermark.fixed', 'yangxj96.com,2022-12-12', 0, NULL, '固定值水印类型的值,多行以,号分割', 0, '2025-11-05 10:37:09.87274+08', 1999056543696211969, '2025-12-25 08:19:14.746388+08', NULL, 2);

-- ----------------------------
-- Table structure for sys_dict_group
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_dict_group";
CREATE TABLE "domain_core"."sys_dict_group" (
  "id" int8 NOT NULL,
  "pid" int8,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "state" bool NOT NULL DEFAULT true,
  "remark" text COLLATE "pg_catalog"."default",
  "builtin" bool NOT NULL DEFAULT false,
  "hide" bool NOT NULL DEFAULT false,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_dict_group"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."pid" IS '父级ID';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."name" IS '字典名称';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."code" IS '字典编码';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."state" IS '字典状态';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."remark" IS '备注';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."builtin" IS '是否内置字段,为TRUE则不允许他进行修改删除操作';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."hide" IS '是否隐藏,为TRUE则前端不可直接进行修改删除等操作';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_dict_group"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_dict_group" IS '字典组表';

-- ----------------------------
-- Records of sys_dict_group
-- ----------------------------
INSERT INTO "domain_core"."sys_dict_group" VALUES (1942142790749085698, 1942142921347129346, '用户状态', 'sys_user_state', 't', '用户状态', 't', 'f', 1927290201865945090, '2025-07-07 16:44:59+08', 1927290201865945090, '2025-07-07 16:44:59+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (1942142921347129346, NULL, '系统配置', 'sys', 't', '系统配置相关的字典组.基本为内置不可修改的字典组', 't', 'f', 1927290201865945090, '2025-07-07 16:45:30+08', 1927290201865945090, '2025-07-07 16:45:30+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (1942143605777850369, 1942142921347129346, '通用状态', 'sys_common_state', 't', '通用状态', 't', 'f', 1927290201865945090, '2025-07-07 16:48:14+08', 1927290201865945090, '2025-07-07 16:48:14+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (1944677668501757953, 1942142921347129346, '组织机构类型', 'sys_organization_type', 't', NULL, 't', 'f', 1927290201865945090, '2025-07-14 16:37:41+08', 1927290201865945090, '2025-07-14 16:37:41+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (1985526822161326081, NULL, 'OA相关', 'idct_oa', 't', 'OA相关的内容', 't', 'f', 1934276682383138817, '2025-11-04 09:57:38.373406+08', 1934276682383138817, '2025-11-04 09:57:38.373406+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (1985527007209824258, 1985526822161326081, '流程分类', 'dict_workflow_type', 't', '流程分类的具体分类列表', 't', 'f', 1934276682383138817, '2025-11-04 09:58:22.491703+08', 1934276682383138817, '2025-11-04 10:02:42.548569+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (2001835764009107458, 1942142921347129346, '用户性别', 'sys_user_gender', 't', '用户的性别列表', 't', 'f', 1999056543696211969, '2025-12-19 02:03:33.11939+08', 1999056543696211969, '2025-12-19 02:03:33.11939+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (2001836722806030337, 1942142921347129346, '时区', 'sys_timezone', 't', '系统时区,主要是为了处理时间的', 't', 'f', 1999056543696211969, '2025-12-19 02:07:21.711197+08', 1999056543696211969, '2025-12-19 02:07:21.711197+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (2001845555582394370, 1942142921347129346, '语言', 'sys_language', 't', NULL, 't', 'f', 1999056543696211969, '2025-12-19 02:42:27.609904+08', 1999056543696211969, '2025-12-19 02:42:27.609904+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (2001938637455818753, 1942142921347129346, '邮箱后缀', 'sys_email_suffix', 't', '常用邮箱后缀', 't', 'f', 1999056543696211969, '2025-12-19 08:52:20.062054+08', 1999056543696211969, '2025-12-19 08:52:20.062054+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_group" VALUES (2004112169723998210, 1942142921347129346, '水印类型', 'sys_watermark', 't', '水印的类型', 't', 'f', 1999056543696211969, '2025-12-25 08:49:10.528381+08', 1999056543696211969, '2025-12-25 08:49:10.528381+08', NULL, 0);

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_dict_item";
CREATE TABLE "domain_core"."sys_dict_item" (
  "id" int8 NOT NULL,
  "gid" int8 NOT NULL,
  "label" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "value" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "sort" int2 NOT NULL DEFAULT 0,
  "state" int2 NOT NULL,
  "remark" varchar(255) COLLATE "pg_catalog"."default",
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_dict_item"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."gid" IS '字典组ID';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."label" IS '标签';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."value" IS '值';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."sort" IS '排序';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."state" IS '状态';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."remark" IS '备注';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_dict_item"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_dict_item" IS '字典数据表';

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO "domain_core"."sys_dict_item" VALUES (1942143228055609345, 1942142790749085698, '正常', '0', 999, 0, '正常', 1927290201865945090, '2025-07-07 16:46:44+08', 1927290201865945090, '2025-07-07 16:46:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1942143271655399425, 1942142790749085698, '冻结', '1', 999, 0, '冻结', 1927290201865945090, '2025-07-07 16:46:54+08', 1927290201865945090, '2025-07-07 16:46:54+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1942143325908721665, 1942142790749085698, '封禁', '2', 999, 0, '封禁', 1927290201865945090, '2025-07-07 16:47:07+08', 1927290201865945090, '2025-07-07 16:47:07+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1942143658663829506, 1942143605777850369, '启用', '0', 999, 0, '启用', 1927290201865945090, '2025-07-07 16:48:26+08', 1927290201865945090, '2025-07-07 16:48:26+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1942143694822924289, 1942143605777850369, '禁用', '1', 999, 0, '禁用', 1927290201865945090, '2025-07-07 16:48:35+08', 1927290201865945090, '2025-07-07 16:48:35+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1944677811678519297, 1944677668501757953, '集团总部', '1', 1, 0, NULL, 1927290201865945090, '2025-07-14 16:38:15+08', 1927290201865945090, '2025-08-05 11:30:17+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1944677899893121026, 1944677668501757953, '省级公司', '2', 2, 0, NULL, 1927290201865945090, '2025-07-14 16:38:36+08', 1927290201865945090, '2025-08-05 11:34:06+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1944677967375278082, 1944677668501757953, '市级公司', '3', 3, 0, NULL, 1927290201865945090, '2025-07-14 16:38:52+08', 1927290201865945090, '2025-08-05 11:30:29+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1944678003773448194, 1944677668501757953, '部门', '5', 5, 0, NULL, 1927290201865945090, '2025-07-14 16:39:01+08', 1927290201865945090, '2025-08-05 11:30:47+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1944678052813250561, 1944677668501757953, '科室/小组', '6', 6, 0, NULL, 1927290201865945090, '2025-07-14 16:39:13+08', 1927290201865945090, '2025-08-05 11:30:52+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1952572801645711361, 1944677668501757953, '县级公司', '4', 4, 0, NULL, 1927290201865945090, '2025-08-05 11:30:08+08', 1927290201865945090, '2025-08-05 11:34:01+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1970017634542866433, 1944677668501757953, '系统运维', '0', 0, 0, '系统运维,请勿删除', 1934276682383138817, '2025-09-22 14:49:39.903488+08', 1934276682383138817, '2025-09-22 14:49:39.903488+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1985527107264946178, 1985527007209824258, '财务', '0', 999, 0, NULL, 1934276682383138817, '2025-11-04 09:58:46.343227+08', 1934276682383138817, '2025-11-04 09:58:46.34434+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (1985527149598056449, 1985527007209824258, '人事', '1', 999, 0, NULL, 1934276682383138817, '2025-11-04 09:58:56.442225+08', 1934276682383138817, '2025-11-04 09:58:56.443545+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001835831147331585, 2001835764009107458, '未知', '0', 999, 0, NULL, 1999056543696211969, '2025-12-19 02:03:49.127191+08', 1999056543696211969, '2025-12-19 02:03:49.127191+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001835881252487170, 2001835764009107458, '男性', '1', 999, 0, NULL, 1999056543696211969, '2025-12-19 02:04:01.062163+08', 1999056543696211969, '2025-12-19 02:04:01.062163+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001835919873638401, 2001835764009107458, '女性', '2', 999, 0, NULL, 1999056543696211969, '2025-12-19 02:04:10.269774+08', 1999056543696211969, '2025-12-19 02:04:10.269774+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001835956548632577, 2001835764009107458, '人妖', '3', 999, 0, NULL, 1999056543696211969, '2025-12-19 02:04:19.020125+08', 1999056543696211969, '2025-12-19 02:04:19.020125+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001836008423784450, 2001835764009107458, '沃尔玛塑料袋', '4', 999, 0, NULL, 1999056543696211969, '2025-12-19 02:04:31.393873+08', 1999056543696211969, '2025-12-19 02:04:31.393873+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000001, 2001836722806030337, '国际日期变更线西', 'Etc/GMT+12', 0, 0, 'UTC-12', 1999056543696211969, '2025-12-19 10:22:47.415+08', 1999056543696211969, '2025-12-19 10:22:47.415+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000002, 2001836722806030337, '萨摩亚时间', 'Pacific/Pago_Pago', 1, 0, 'UTC-11', 1999056543696211969, '2025-12-19 10:22:47.426+08', 1999056543696211969, '2025-12-19 10:22:47.426+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000003, 2001836722806030337, '夏威夷时间', 'Pacific/Honolulu', 2, 0, 'UTC-10', 1999056543696211969, '2025-12-19 10:22:47.427+08', 1999056543696211969, '2025-12-19 10:22:47.427+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000004, 2001836722806030337, '阿拉斯加时间', 'America/Anchorage', 3, 0, 'UTC-9', 1999056543696211969, '2025-12-19 10:22:47.431+08', 1999056543696211969, '2025-12-19 10:22:47.431+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000005, 2001836722806030337, '美国太平洋时间', 'America/Los_Angeles', 4, 0, 'UTC-8', 1999056543696211969, '2025-12-19 10:22:47.432+08', 1999056543696211969, '2025-12-19 10:22:47.432+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000006, 2001836722806030337, '美国山地时间', 'America/Denver', 5, 0, 'UTC-7', 1999056543696211969, '2025-12-19 10:22:47.433+08', 1999056543696211969, '2025-12-19 10:22:47.433+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000007, 2001836722806030337, '美国中部时间', 'America/Chicago', 6, 0, 'UTC-6', 1999056543696211969, '2025-12-19 10:22:47.433+08', 1999056543696211969, '2025-12-19 10:22:47.433+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000008, 2001836722806030337, '美国东部时间', 'America/New_York', 7, 0, 'UTC-5', 1999056543696211969, '2025-12-19 10:22:47.434+08', 1999056543696211969, '2025-12-19 10:22:47.434+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000009, 2001836722806030337, '大西洋时间', 'America/Halifax', 8, 0, 'UTC-4', 1999056543696211969, '2025-12-19 10:22:47.435+08', 1999056543696211969, '2025-12-19 10:22:47.435+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000010, 2001836722806030337, '巴西时间（圣保罗）', 'America/Sao_Paulo', 9, 0, 'UTC-3', 1999056543696211969, '2025-12-19 10:22:47.437+08', 1999056543696211969, '2025-12-19 10:22:47.437+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000011, 2001836722806030337, '亚速尔群岛时间', 'Atlantic/Azores', 10, 0, 'UTC-1', 1999056543696211969, '2025-12-19 10:22:47.437+08', 1999056543696211969, '2025-12-19 10:22:47.437+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000012, 2001836722806030337, '协调世界时', 'UTC', 11, 0, 'UTC+0', 1999056543696211969, '2025-12-19 10:22:47.437+08', 1999056543696211969, '2025-12-19 10:22:47.437+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000013, 2001836722806030337, '中欧时间（柏林）', 'Europe/Berlin', 12, 0, 'UTC+1', 1999056543696211969, '2025-12-19 10:22:47.438+08', 1999056543696211969, '2025-12-19 10:22:47.438+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000014, 2001836722806030337, '东欧时间（雅典）', 'Europe/Athens', 13, 0, 'UTC+2', 1999056543696211969, '2025-12-19 10:22:47.439+08', 1999056543696211969, '2025-12-19 10:22:47.439+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000015, 2001836722806030337, '莫斯科时间', 'Europe/Moscow', 14, 0, 'UTC+3', 1999056543696211969, '2025-12-19 10:22:47.439+08', 1999056543696211969, '2025-12-19 10:22:47.439+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000016, 2001836722806030337, '印度标准时间', 'Asia/Kolkata', 15, 0, 'UTC+5:30', 1999056543696211969, '2025-12-19 10:22:47.441+08', 1999056543696211969, '2025-12-19 10:22:47.441+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000017, 2001836722806030337, '中国标准时间(北京时间)', 'Asia/Shanghai', 16, 0, 'UTC+8', 1999056543696211969, '2025-12-19 10:22:47.441+08', 1999056543696211969, '2025-12-19 10:22:47.441+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000018, 2001836722806030337, '日本标准时间', 'Asia/Tokyo', 17, 0, 'UTC+9', 1999056543696211969, '2025-12-19 10:22:47.442+08', 1999056543696211969, '2025-12-19 10:22:47.442+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000019, 2001836722806030337, '澳大利亚东部时间', 'Australia/Sydney', 18, 0, 'UTC+10', 1999056543696211969, '2025-12-19 10:22:47.443+08', 1999056543696211969, '2025-12-19 10:22:47.443+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001838000000000020, 2001836722806030337, '新西兰时间', 'Pacific/Auckland', 19, 0, 'UTC+12', 1999056543696211969, '2025-12-19 10:22:47.444+08', 1999056543696211969, '2025-12-19 10:22:47.444+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395001, 2001845555582394370, '中文（简体）', 'zh-CN', 0, 0, '中文简体', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395002, 2001845555582394370, '中文（繁体）', 'zh-TW', 1, 0, '中文繁体', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395003, 2001845555582394370, '英语', 'en', 2, 0, 'English', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395004, 2001845555582394370, '日语', 'ja', 3, 0, '日本語', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395005, 2001845555582394370, '韩语', 'ko', 4, 0, '한국어', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395006, 2001845555582394370, '法语', 'fr', 5, 0, 'Français', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395007, 2001845555582394370, '德语', 'de', 6, 0, 'Deutsch', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395008, 2001845555582394370, '西班牙语', 'es', 7, 0, 'Español', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395009, 2001845555582394370, '俄语', 'ru', 8, 0, 'Русский', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395010, 2001845555582394370, '葡萄牙语', 'pt', 9, 0, 'Português', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395011, 2001845555582394370, '意大利语', 'it', 10, 0, 'Italiano', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395012, 2001845555582394370, '阿拉伯语', 'ar', 11, 0, 'العربية', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (3001845555582395013, 2001845555582394370, '印地语', 'hi', 12, 0, 'हिन्दी', 1999056543696211969, '2025-12-19 10:50:44+08', 1999056543696211969, '2025-12-19 10:50:44+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001938723007037442, 2001938637455818753, 'devops', 'devops00.com', 999, 0, NULL, 1999056543696211969, '2025-12-19 08:52:40.456365+08', 1999056543696211969, '2025-12-19 08:52:40.456365+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001938789625167874, 2001938637455818753, '谷歌邮箱', 'gmail.com', 999, 0, NULL, 1999056543696211969, '2025-12-19 08:52:56.341651+08', 1999056543696211969, '2025-12-19 08:52:56.341651+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001938830112784385, 2001938637455818753, 'QQ邮箱', 'qq.com', 999, 0, NULL, 1999056543696211969, '2025-12-19 08:53:05.987604+08', 1999056543696211969, '2025-12-19 08:53:05.987604+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2001938875897806849, 2001938637455818753, '微软hotmail', 'hotmail.com', 999, 0, NULL, 1999056543696211969, '2025-12-19 08:53:16.904496+08', 1999056543696211969, '2025-12-19 08:53:16.904496+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2004112264980836353, 2004112169723998210, '系统生成', '1', 999, 0, NULL, 1999056543696211969, '2025-12-25 08:49:33.238377+08', 1999056543696211969, '2025-12-25 08:49:33.238377+08', NULL, 0);
INSERT INTO "domain_core"."sys_dict_item" VALUES (2004112289467183105, 2004112169723998210, '固定值', '2', 999, 0, NULL, 1999056543696211969, '2025-12-25 08:49:39.085004+08', 1999056543696211969, '2025-12-25 08:49:39.085004+08', NULL, 0);

-- ----------------------------
-- Table structure for sys_file_chunk
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_file_chunk";
CREATE TABLE "domain_core"."sys_file_chunk" (
  "id" int8 NOT NULL,
  "file_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "file_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "chunk_index" int4 NOT NULL,
  "total_chunks" int4 NOT NULL,
  "chunk_path" varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
  "chunk_size" int8 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."file_name" IS '文件原名';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."file_id" IS '文件唯一标识（如 SHA256 或 UUID）';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."chunk_index" IS '分片序号（从 0 开始）';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."total_chunks" IS '总分片数（冗余，便于校验）';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."chunk_path" IS '分片在磁盘/OSS 的存储路径或 Key';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."chunk_size" IS '当前分片字节数';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."deleted" IS '删除时间';
COMMENT ON COLUMN "domain_core"."sys_file_chunk"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_file_chunk" IS '文件上传信息';

-- ----------------------------
-- Records of sys_file_chunk
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file_info
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_file_info";
CREATE TABLE "domain_core"."sys_file_info" (
  "id" int8 NOT NULL,
  "file_name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "origin_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "suffix" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "path" varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
  "size" int8 NOT NULL,
  "hash" varchar(64) COLLATE "pg_catalog"."default",
  "storage_type" int4 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_file_info"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_file_info"."file_name" IS '生成的32位的文件名称';
COMMENT ON COLUMN "domain_core"."sys_file_info"."origin_name" IS '文件源名称';
COMMENT ON COLUMN "domain_core"."sys_file_info"."suffix" IS '文件后缀';
COMMENT ON COLUMN "domain_core"."sys_file_info"."path" IS '文件存储位置';
COMMENT ON COLUMN "domain_core"."sys_file_info"."size" IS '文件大小';
COMMENT ON COLUMN "domain_core"."sys_file_info"."hash" IS '文件hash值';
COMMENT ON COLUMN "domain_core"."sys_file_info"."storage_type" IS '文件存储类型';
COMMENT ON COLUMN "domain_core"."sys_file_info"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_file_info"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_file_info"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_file_info"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_file_info"."deleted" IS '删除时间';
COMMENT ON COLUMN "domain_core"."sys_file_info"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_file_info" IS '文件上传信息';

-- ----------------------------
-- Records of sys_file_info
-- ----------------------------

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_log";
CREATE TABLE "domain_core"."sys_log" (
  "id" int8 NOT NULL,
  "type" int4,
  "explain" varchar(255) COLLATE "pg_catalog"."default",
  "status" int2,
  "ip" varchar(100) COLLATE "pg_catalog"."default",
  "method" varchar(255) COLLATE "pg_catalog"."default",
  "url" varchar(255) COLLATE "pg_catalog"."default",
  "args" bytea,
  "result" bytea,
  "time_cost" int8,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_log"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_log"."type" IS '日志类型';
COMMENT ON COLUMN "domain_core"."sys_log"."explain" IS '日志说明';
COMMENT ON COLUMN "domain_core"."sys_log"."status" IS '请求状态';
COMMENT ON COLUMN "domain_core"."sys_log"."ip" IS '来源IP';
COMMENT ON COLUMN "domain_core"."sys_log"."method" IS '请求方法';
COMMENT ON COLUMN "domain_core"."sys_log"."url" IS '请求URL';
COMMENT ON COLUMN "domain_core"."sys_log"."args" IS '请求参数';
COMMENT ON COLUMN "domain_core"."sys_log"."result" IS '请求响应';
COMMENT ON COLUMN "domain_core"."sys_log"."time_cost" IS '耗时';
COMMENT ON COLUMN "domain_core"."sys_log"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_log"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_log"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_log"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_log"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_log"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_log" IS '操作日志表';

-- ----------------------------
-- Records of sys_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_menu";
CREATE TABLE "domain_core"."sys_menu" (
  "id" int8 NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "pid" int8,
  "icon" varchar(100) COLLATE "pg_catalog"."default",
  "path" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "component" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "layout" varchar(100) COLLATE "pg_catalog"."default",
  "sort" int4 DEFAULT 0,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_menu"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_menu"."name" IS '名称';
COMMENT ON COLUMN "domain_core"."sys_menu"."pid" IS '父级ID';
COMMENT ON COLUMN "domain_core"."sys_menu"."icon" IS '图标';
COMMENT ON COLUMN "domain_core"."sys_menu"."path" IS '请求路径';
COMMENT ON COLUMN "domain_core"."sys_menu"."component" IS '组件路径,为空则使用布局组件';
COMMENT ON COLUMN "domain_core"."sys_menu"."layout" IS '布局';
COMMENT ON COLUMN "domain_core"."sys_menu"."sort" IS '排序';
COMMENT ON COLUMN "domain_core"."sys_menu"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_menu"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_menu"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_menu"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_menu"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_menu"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_menu" IS '菜单表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO "domain_core"."sys_menu" VALUES (1947944068104261634, '服务监控', 1947937225978130433, 'icon-module', 'server', '/Monitor/Server/index', NULL, 3, 1927290201865945090, '2025-07-23 16:57:12+08', 1927290201865945090, '2025-07-23 16:57:12+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1947937225978130433, '系统监控', NULL, 'icon-setting', '/monitor', 'layout', 'default', 3, 1927290201865945090, '2025-07-23 16:30:00+08', 1999056543696211969, '2026-01-12 09:42:57.067289+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929928379667386370, '组件示例', NULL, 'icon-setting', '/example', 'layout', 'default', 3, 1927290201865945090, '2025-06-05 11:37:46+08', 1934276682383138817, '2025-10-16 15:13:16.425401+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1978719286821117953, '工作台', NULL, 'icon-setting', '/workbench', 'layout', 'blank', 1, 1934276682383138817, '2025-10-16 15:06:55.462673+08', 1999056543696211969, '2026-01-12 09:42:47.295232+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929928379575111682, '系统管理', NULL, 'icon-setting', '/system', 'layout', 'default', 2, 1927290201865945090, '2025-06-05 11:37:46+08', 1999056543696211969, '2026-01-12 09:42:51.932451+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929929620753526790, '文件存储', 1929928379575111682, 'icon-module', 'storage', '/System/Storage/index', NULL, 5, 1927290201865945090, '2025-06-05 11:37:46+08', 1927290201865945090, '2025-06-12 10:03:49+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929929620816441347, '列表示例', 1929928379667386370, 'icon-module', 'table', '/Example/Table/index', NULL, 1, 1927290201865945090, '2025-06-05 11:37:46+08', 1927290201865945090, '2025-06-11 09:47:10+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929929620753526788, '定时任务', 1947937225978130433, 'icon-module', 'task', '/Monitor/Task/index', NULL, 2, 1927290201865945090, '2025-06-05 11:37:46+08', 1927290201865945090, '2025-07-23 16:56:11+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929929620715778049, '用户管理', 1929928379575111682, 'icon-module', 'user', '/System/User/index', NULL, 0, 1927290201865945090, '2025-06-05 11:37:46+08', 1927290201865945090, '2025-08-06 11:04:40+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (2010532321008984065, '首页默认', 2010527630413963265, 'icon-module', '', '/Home/index', NULL, 1, 1999056543696211969, '2026-01-12 10:00:33.883774+08', 1999056543696211969, '2026-01-12 10:02:14.671794+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1947944189344813057, '缓存监控', 1947937225978130433, 'icon-module', 'cache', '/Monitor/Cache/index', NULL, 4, 1927290201865945090, '2025-07-23 16:57:40+08', 1927290201865945090, '2025-07-23 16:57:40+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1986315466212065281, '系统配置', 1929928379575111682, 'icon-module', 'configured', 'System/Configured/index', NULL, 9, 1934276682383138817, '2025-11-06 14:11:25.765603+08', 1934276682383138817, '2025-11-06 14:11:25.765603+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (2010527630413963265, '首页', NULL, 'icon-home', '/', 'layout', 'blank', 0, 1999056543696211969, '2026-01-12 09:41:55.556341+08', 1999056543696211969, '2026-01-12 10:00:03.065891+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1943557799479791617, 'Markdown', 1929928379667386370, 'icon-module', 'markdown', '/Example/Markdown/index', NULL, 4, 1927290201865945090, '2025-07-11 14:27:44+08', 1927290201865945090, '2025-07-11 14:27:54+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (2010532645425815554, '工作台默认页面', 1978719286821117953, 'icon-module', '', '/Workbench/index', NULL, 1, 1999056543696211969, '2026-01-12 10:01:51.221078+08', 1999056543696211969, '2026-01-12 10:02:09.314586+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1981263449689100290, '流程管理', 1929928379575111682, 'icon-module', 'workflow', '/System/Workflow/index', NULL, 8, 1934276682383138817, '2025-10-23 15:36:31.159586+08', 1999056543696211969, '2026-01-20 16:16:52.836803+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1947937675586547713, '数据监控', 1947937225978130433, 'icon-module', 'database', '/Monitor/Database/index', NULL, 1, 1927290201865945090, '2025-07-23 16:31:47+08', 1927290201865945090, '2025-07-23 16:31:47+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1932983846772363266, '部门管理', 1929928379575111682, 'icon-module', 'dept', '/System/Dept/index', NULL, 1, 1927290201865945090, '2025-06-12 10:10:37+08', 1927290201865945090, '2025-06-12 10:10:37+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929929620753526787, '字典管理', 1929928379575111682, 'icon-module', 'dict', '/System/Dict/index', NULL, 4, 1927290201865945090, '2025-06-05 11:37:46+08', 1927290201865945090, '2025-06-12 10:03:46+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929929620816441346, '图表示例', 1929928379667386370, 'icon-module', 'echarts', '/Example/Echarts/index', NULL, 3, 1927290201865945090, '2025-06-05 11:37:46+08', 1927290201865945090, '2025-06-11 09:47:18+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929929620816441348, '表单示例', 1929928379667386370, 'icon-module', 'form', '/Example/Form/index', NULL, 2, 1927290201865945090, '2025-06-05 11:37:46+08', 1927290201865945090, '2025-06-11 09:47:14+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1972191867792498690, '许可管理', 1929928379575111682, 'icon-module', 'license', '/System/License/index', NULL, 7, 1934276682383138817, '2025-09-28 14:49:17.492444+08', 1934276682383138817, '2025-09-28 14:49:17.493444+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929929620753526789, '菜单管理', 1929928379575111682, 'icon-module', 'menu', '/System/Menu/index', NULL, 3, 1927290201865945090, '2025-06-05 11:37:46+08', 1927290201865945090, '2025-06-12 10:03:43+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1947943281978445825, '在线用户', 1947937225978130433, 'icon-module', 'online', '/Monitor/Online/index', NULL, 0, 1927290201865945090, '2025-07-23 16:54:04+08', 1927290201865945090, '2025-07-23 16:55:35+08', NULL, 0);
INSERT INTO "domain_core"."sys_menu" VALUES (1929929620753526785, '访问控制', 1929928379575111682, 'icon-module', 'RBAC', '/System/RBAC/index', NULL, 2, 1927290201865945090, '2025-06-05 11:37:46+08', 1927290201865945090, '2025-06-12 10:03:39+08', NULL, 0);

-- ----------------------------
-- Table structure for sys_organization
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_organization";
CREATE TABLE "domain_core"."sys_organization" (
  "id" int8 NOT NULL,
  "pid" int8,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int2,
  "path" varchar(255) COLLATE "pg_catalog"."default",
  "remark" varchar(255) COLLATE "pg_catalog"."default",
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_organization"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_organization"."pid" IS '上级ID';
COMMENT ON COLUMN "domain_core"."sys_organization"."name" IS '名称';
COMMENT ON COLUMN "domain_core"."sys_organization"."code" IS '编码';
COMMENT ON COLUMN "domain_core"."sys_organization"."type" IS '公司类型';
COMMENT ON COLUMN "domain_core"."sys_organization"."path" IS '组织机构路径';
COMMENT ON COLUMN "domain_core"."sys_organization"."remark" IS '备注';
COMMENT ON COLUMN "domain_core"."sys_organization"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_organization"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_organization"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_organization"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_organization"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_organization"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_organization" IS '组织机构表';

-- ----------------------------
-- Records of sys_organization
-- ----------------------------
INSERT INTO "domain_core"."sys_organization" VALUES (1970016645676978177, NULL, '光谱平台', '8AFDB7362FE7BBBA75C5CB060B43617D', 1, '光谱平台', '测试', 1934276682383138817, '2025-09-22 14:45:44.141989+08', 1934276682383138817, '2025-09-22 14:45:44.142986+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970017409916915713, NULL, '系统运维', 'EAB1F32B8B3EE1D3AEF4BFF2F25C3309', 0, '系统运维', '系统运维使用', 1934276682383138817, '2025-09-22 14:48:46.337584+08', 1934276682383138817, '2025-09-22 14:53:19.712089+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970020177696210945, 1970016645676978177, '云南分公司', 'F5B3DA0C392FBE4E9DC049C62CD61799', 2, '光谱平台/云南分公司', '整个云南的顶级公司', 1934276682383138817, '2025-09-22 14:59:46.233088+08', 1934276682383138817, '2025-09-22 14:59:46.233088+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970020240908566529, 1970020177696210945, '昆明分公司', '3C982204BBB6A9C2E88BFA94E029FC3F', 3, '光谱平台/云南分公司/昆明分公司', NULL, 1934276682383138817, '2025-09-22 15:00:01.309225+08', 1934276682383138817, '2025-09-22 15:00:01.310222+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970020285514989569, 1970020177696210945, '保山分公司', 'F3FDE439CCFA999C0973BC0BE16CA928', 3, '光谱平台/云南分公司/保山分公司', NULL, 1934276682383138817, '2025-09-22 15:00:11.946862+08', 1934276682383138817, '2025-09-22 15:00:11.947859+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970020337545330689, 1970020240908566529, '财务部', 'F9BBC7C953470C63DD7E3D2073FB65D9', 5, '光谱平台/云南分公司/昆明分公司/财务部', NULL, 1934276682383138817, '2025-09-22 15:00:24.342556+08', 1934276682383138817, '2025-09-22 15:00:24.342556+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970020393669312514, 1970020240908566529, '人事部', 'B6D75AEA3941977A73E0BF983FB173FD', 5, '光谱平台/云南分公司/昆明分公司/人事部', NULL, 1934276682383138817, '2025-09-22 15:00:37.73122+08', 1934276682383138817, '2025-09-22 15:00:37.73122+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970020430411415553, 1970020285514989569, '财务部', '1D609FD64ADD92F6428643CA2C7979C6', 5, '光谱平台/云南分公司/保山分公司/财务部', NULL, 1934276682383138817, '2025-09-22 15:00:46.488928+08', 1934276682383138817, '2025-09-22 15:00:46.489925+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970020468801880066, 1970020285514989569, '人事部', '17315AA2F2FA68005182EA6A32E39550', 5, '光谱平台/云南分公司/保山分公司/人事部', NULL, 1934276682383138817, '2025-09-22 15:00:55.632122+08', 1934276682383138817, '2025-09-22 15:00:55.633119+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970028813113823233, 1970020285514989569, '调度小组', '665F48E713DD4AC8D63BDE35699B5C13', 5, '光谱平台/云南分公司/保山分公司/调度小组', '调度小组', 1934276682383138817, '2025-09-22 15:34:05.083664+08', 1934276682383138817, '2025-10-10 11:19:42.893019+08', NULL, 0);
INSERT INTO "domain_core"."sys_organization" VALUES (1970029267130454018, 1970020285514989569, '测试小组', 'E55DB2954D238A967564B251394F01CB', 5, '光谱平台/云南分公司/保山分公司/测试小组', '测试', 1934276682383138817, '2025-09-22 15:35:53.321364+08', 1999056543696211969, '2025-12-25 02:16:20.845587+08', NULL, 0);

-- ----------------------------
-- Table structure for sys_rel_role_authority
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_rel_role_authority";
CREATE TABLE "domain_core"."sys_rel_role_authority" (
  "id" int8 NOT NULL,
  "role_id" int8 NOT NULL,
  "authority_id" int8 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_rel_role_authority"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_rel_role_authority"."role_id" IS '角色ID';
COMMENT ON COLUMN "domain_core"."sys_rel_role_authority"."authority_id" IS '权限ID';
COMMENT ON COLUMN "domain_core"."sys_rel_role_authority"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_rel_role_authority"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_rel_role_authority"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_rel_role_authority"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_rel_role_authority"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_rel_role_authority"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_rel_role_authority" IS '中间表-角色到权限';

-- ----------------------------
-- Records of sys_rel_role_authority
-- ----------------------------
INSERT INTO "domain_core"."sys_rel_role_authority" VALUES (1, 1932682189593350146, 1, NULL, '2025-09-03 16:35:02+08', NULL, '2025-09-03 16:35:05+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_authority" VALUES (1973771042936467458, 1932685785802162178, 1, 1934276682383138817, '2025-10-02 23:24:22.187699+08', 1934276682383138817, '2025-10-02 23:24:22.187699+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_authority" VALUES (1979080389988175874, 1972908999916273666, 1963505328908578818, 1934276682383138817, '2025-10-17 15:01:49.166121+08', 1934276682383138817, '2025-10-17 15:01:49.166121+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_authority" VALUES (1979080390076256258, 1972908999916273666, 1963505678168293377, 1934276682383138817, '2025-10-17 15:01:49.188122+08', 1934276682383138817, '2025-10-17 15:01:49.18912+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_authority" VALUES (1979080390088839169, 1972908999916273666, 1963176940012478465, 1934276682383138817, '2025-10-17 15:01:49.190154+08', 1934276682383138817, '2025-10-17 15:01:49.190154+08', NULL, 0);

-- ----------------------------
-- Table structure for sys_rel_role_menu
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_rel_role_menu";
CREATE TABLE "domain_core"."sys_rel_role_menu" (
  "id" int8 NOT NULL,
  "role_id" int8 NOT NULL,
  "menu_id" int8 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_rel_role_menu"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_rel_role_menu"."role_id" IS '角色ID';
COMMENT ON COLUMN "domain_core"."sys_rel_role_menu"."menu_id" IS '菜单ID';
COMMENT ON COLUMN "domain_core"."sys_rel_role_menu"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_rel_role_menu"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_rel_role_menu"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_rel_role_menu"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_rel_role_menu"."deleted" IS '删除标识';
COMMENT ON COLUMN "domain_core"."sys_rel_role_menu"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_rel_role_menu" IS '中间表-角色到菜单';

-- ----------------------------
-- Records of sys_rel_role_menu
-- ----------------------------
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441325056001, 1932682189593350146, 1929929620753526790, 1934276682383138817, '2025-09-04 11:38:05.342307+08', 1934276682383138817, '2025-09-04 11:38:05.343306+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441392164866, 1932682189593350146, 1929929620753526789, 1934276682383138817, '2025-09-04 11:38:05.360768+08', 1934276682383138817, '2025-09-04 11:38:05.361769+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441392164867, 1932682189593350146, 1947944068104261634, 1934276682383138817, '2025-09-04 11:38:05.361769+08', 1934276682383138817, '2025-09-04 11:38:05.362768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441392164868, 1932682189593350146, 1929929620753526788, 1934276682383138817, '2025-09-04 11:38:05.362768+08', 1934276682383138817, '2025-09-04 11:38:05.363768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441392164869, 1932682189593350146, 1929929620816441348, 1934276682383138817, '2025-09-04 11:38:05.363768+08', 1934276682383138817, '2025-09-04 11:38:05.364768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441392164870, 1932682189593350146, 1929929620753526787, 1934276682383138817, '2025-09-04 11:38:05.364768+08', 1934276682383138817, '2025-09-04 11:38:05.365768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441392164871, 1932682189593350146, 1929929620816441347, 1934276682383138817, '2025-09-04 11:38:05.366769+08', 1934276682383138817, '2025-09-04 11:38:05.367768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441392164872, 1932682189593350146, 1929928379575111682, 1934276682383138817, '2025-09-04 11:38:05.368768+08', 1934276682383138817, '2025-09-04 11:38:05.368768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441392164873, 1932682189593350146, 1947944189344813057, 1934276682383138817, '2025-09-04 11:38:05.369768+08', 1934276682383138817, '2025-09-04 11:38:05.369768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441392164874, 1932682189593350146, 1929928379667386370, 1934276682383138817, '2025-09-04 11:38:05.370771+08', 1934276682383138817, '2025-09-04 11:38:05.370771+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441455079425, 1932682189593350146, 1929929620816441346, 1934276682383138817, '2025-09-04 11:38:05.370771+08', 1934276682383138817, '2025-09-04 11:38:05.371768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441455079426, 1932682189593350146, 1929929620715778049, 1934276682383138817, '2025-09-04 11:38:05.371768+08', 1934276682383138817, '2025-09-04 11:38:05.371768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441455079427, 1932682189593350146, 1929929620753526785, 1934276682383138817, '2025-09-04 11:38:05.372768+08', 1934276682383138817, '2025-09-04 11:38:05.373769+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441455079428, 1932682189593350146, 1932983846772363266, 1934276682383138817, '2025-09-04 11:38:05.374769+08', 1934276682383138817, '2025-09-04 11:38:05.375774+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441455079429, 1932682189593350146, 1947937225978130433, 1934276682383138817, '2025-09-04 11:38:05.375774+08', 1934276682383138817, '2025-09-04 11:38:05.376768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441455079430, 1932682189593350146, 1943557799479791617, 1934276682383138817, '2025-09-04 11:38:05.376768+08', 1934276682383138817, '2025-09-04 11:38:05.376768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441455079431, 1932682189593350146, 1947937675586547713, 1934276682383138817, '2025-09-04 11:38:05.377768+08', 1934276682383138817, '2025-09-04 11:38:05.377768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441455079432, 1932682189593350146, 1947943281978445825, 1934276682383138817, '2025-09-04 11:38:05.378768+08', 1934276682383138817, '2025-09-04 11:38:05.378768+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515010588674, 1932685785802162178, 1929929620753526790, 1934276682383138817, '2025-09-04 11:38:22.921124+08', 1934276682383138817, '2025-09-04 11:38:22.922124+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697537, 1932685785802162178, 1929929620753526789, 1934276682383138817, '2025-09-04 11:38:22.924126+08', 1934276682383138817, '2025-09-04 11:38:22.924126+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697538, 1932685785802162178, 1947944068104261634, 1934276682383138817, '2025-09-04 11:38:22.925125+08', 1934276682383138817, '2025-09-04 11:38:22.925125+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697539, 1932685785802162178, 1929929620753526788, 1934276682383138817, '2025-09-04 11:38:22.926124+08', 1934276682383138817, '2025-09-04 11:38:22.927124+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697540, 1932685785802162178, 1929929620816441348, 1934276682383138817, '2025-09-04 11:38:22.929126+08', 1934276682383138817, '2025-09-04 11:38:22.930126+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697541, 1932685785802162178, 1929929620753526787, 1934276682383138817, '2025-09-04 11:38:22.931125+08', 1934276682383138817, '2025-09-04 11:38:22.932125+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697542, 1932685785802162178, 1929929620816441347, 1934276682383138817, '2025-09-04 11:38:22.932125+08', 1934276682383138817, '2025-09-04 11:38:22.933125+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697543, 1932685785802162178, 1929928379575111682, 1934276682383138817, '2025-09-04 11:38:22.933125+08', 1934276682383138817, '2025-09-04 11:38:22.933125+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697544, 1932685785802162178, 1947944189344813057, 1934276682383138817, '2025-09-04 11:38:22.934124+08', 1934276682383138817, '2025-09-04 11:38:22.934124+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697545, 1932685785802162178, 1929928379667386370, 1934276682383138817, '2025-09-04 11:38:22.934124+08', 1934276682383138817, '2025-09-04 11:38:22.934124+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697546, 1932685785802162178, 1929929620816441346, 1934276682383138817, '2025-09-04 11:38:22.935124+08', 1934276682383138817, '2025-09-04 11:38:22.935124+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697547, 1932685785802162178, 1929929620715778049, 1934276682383138817, '2025-09-04 11:38:22.936125+08', 1934276682383138817, '2025-09-04 11:38:22.937125+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515077697548, 1932685785802162178, 1929929620753526785, 1934276682383138817, '2025-09-04 11:38:22.937125+08', 1934276682383138817, '2025-09-04 11:38:22.937125+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515140612097, 1932685785802162178, 1932983846772363266, 1934276682383138817, '2025-09-04 11:38:22.938276+08', 1934276682383138817, '2025-09-04 11:38:22.938781+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515140612098, 1932685785802162178, 1947937225978130433, 1934276682383138817, '2025-09-04 11:38:22.938781+08', 1934276682383138817, '2025-09-04 11:38:22.938781+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515140612099, 1932685785802162178, 1943557799479791617, 1934276682383138817, '2025-09-04 11:38:22.939782+08', 1934276682383138817, '2025-09-04 11:38:22.939782+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515140612100, 1932685785802162178, 1947937675586547713, 1934276682383138817, '2025-09-04 11:38:22.939782+08', 1934276682383138817, '2025-09-04 11:38:22.939782+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446515140612101, 1932685785802162178, 1947943281978445825, 1934276682383138817, '2025-09-04 11:38:22.940784+08', 1934276682383138817, '2025-09-04 11:38:22.940784+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1963446441455079433, 1932682189593350146, 1972191867792498690, 1934276682383138817, '2025-09-28 14:52:53+08', 1934276682383138817, '2025-09-28 14:53:00+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1972941764661559297, 1972908999916273666, 1943557799479791617, 1934276682383138817, '2025-09-30 16:29:06.837961+08', 1934276682383138817, '2025-09-30 16:29:06.837961+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1972941764678336514, 1972908999916273666, 1929929620816441348, 1934276682383138817, '2025-09-30 16:29:06.841951+08', 1934276682383138817, '2025-09-30 16:29:06.842951+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1972941764682530817, 1972908999916273666, 1929929620816441347, 1934276682383138817, '2025-09-30 16:29:06.84395+08', 1934276682383138817, '2025-09-30 16:29:06.844951+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1972941764690919426, 1972908999916273666, 1929928379667386370, 1934276682383138817, '2025-09-30 16:29:06.844951+08', 1934276682383138817, '2025-09-30 16:29:06.844951+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1972941764690919427, 1972908999916273666, 1929929620816441346, 1934276682383138817, '2025-09-30 16:29:06.84595+08', 1934276682383138817, '2025-09-30 16:29:06.84595+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1977574535342292993, 1976487966371991554, 1947937225978130433, 1934276682383138817, '2025-10-13 11:18:05.441869+08', 1934276682383138817, '2025-10-13 11:18:05.44287+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1977574535426179074, 1976487966371991554, 1947937675586547713, 1934276682383138817, '2025-10-13 11:18:05.461082+08', 1934276682383138817, '2025-10-13 11:18:05.461082+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1977574535426179075, 1976487966371991554, 1947944068104261634, 1934276682383138817, '2025-10-13 11:18:05.46208+08', 1934276682383138817, '2025-10-13 11:18:05.46208+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1977574535434567682, 1976487966371991554, 1929929620753526788, 1934276682383138817, '2025-10-13 11:18:05.463081+08', 1934276682383138817, '2025-10-13 11:18:05.463081+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1977574535438761985, 1976487966371991554, 1947944189344813057, 1934276682383138817, '2025-10-13 11:18:05.46508+08', 1934276682383138817, '2025-10-13 11:18:05.46508+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1977574535451344897, 1976487966371991554, 1947943281978445825, 1934276682383138817, '2025-10-13 11:18:05.466082+08', 1934276682383138817, '2025-10-13 11:18:05.467082+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1978720750540914690, 1932682189593350146, 1978719286821117953, 1934276682383138817, '2025-10-16 15:12:44.440831+08', 1934276682383138817, '2025-10-16 15:12:44.441831+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332693983233, 1972908999916273666, 1929929620753526790, 1934276682383138817, '2025-10-17 15:01:35.506005+08', 1934276682383138817, '2025-10-17 15:01:35.507003+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332752703490, 1972908999916273666, 1929929620753526789, 1934276682383138817, '2025-10-17 15:01:35.520004+08', 1934276682383138817, '2025-10-17 15:01:35.520004+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332756897794, 1972908999916273666, 1947944068104261634, 1934276682383138817, '2025-10-17 15:01:35.521003+08', 1934276682383138817, '2025-10-17 15:01:35.521003+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332756897795, 1972908999916273666, 1929929620753526788, 1934276682383138817, '2025-10-17 15:01:35.522003+08', 1934276682383138817, '2025-10-17 15:01:35.522003+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332765286402, 1972908999916273666, 1929929620753526787, 1934276682383138817, '2025-10-17 15:01:35.522003+08', 1934276682383138817, '2025-10-17 15:01:35.523002+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332765286403, 1972908999916273666, 1929928379575111682, 1934276682383138817, '2025-10-17 15:01:35.523002+08', 1934276682383138817, '2025-10-17 15:01:35.523002+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332773675009, 1972908999916273666, 1947944189344813057, 1934276682383138817, '2025-10-17 15:01:35.524002+08', 1934276682383138817, '2025-10-17 15:01:35.524002+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332773675010, 1972908999916273666, 1978719286821117953, 1934276682383138817, '2025-10-17 15:01:35.524002+08', 1934276682383138817, '2025-10-17 15:01:35.525003+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332777869313, 1972908999916273666, 1929929620715778049, 1934276682383138817, '2025-10-17 15:01:35.525003+08', 1934276682383138817, '2025-10-17 15:01:35.526003+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332782063617, 1972908999916273666, 1929929620753526785, 1934276682383138817, '2025-10-17 15:01:35.527003+08', 1934276682383138817, '2025-10-17 15:01:35.527003+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332782063618, 1972908999916273666, 1932983846772363266, 1934276682383138817, '2025-10-17 15:01:35.528001+08', 1934276682383138817, '2025-10-17 15:01:35.528001+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332790452226, 1972908999916273666, 1947937225978130433, 1934276682383138817, '2025-10-17 15:01:35.528001+08', 1934276682383138817, '2025-10-17 15:01:35.528001+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332794646530, 1972908999916273666, 1947937675586547713, 1934276682383138817, '2025-10-17 15:01:35.529003+08', 1934276682383138817, '2025-10-17 15:01:35.529003+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332794646531, 1972908999916273666, 1972191867792498690, 1934276682383138817, '2025-10-17 15:01:35.530001+08', 1934276682383138817, '2025-10-17 15:01:35.530001+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1979080332794646532, 1972908999916273666, 1947943281978445825, 1934276682383138817, '2025-10-17 15:01:35.530001+08', 1934276682383138817, '2025-10-17 15:01:35.530001+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1981263533814255617, 1932685785802162178, 1978719286821117953, 1934276682383138817, '2025-10-23 15:36:51.216723+08', 1934276682383138817, '2025-10-23 15:36:51.217725+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1981263533860392961, 1932685785802162178, 1981263449689100290, 1934276682383138817, '2025-10-23 15:36:51.227727+08', 1934276682383138817, '2025-10-23 15:36:51.228722+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1981263533877170178, 1932685785802162178, 1972191867792498690, 1934276682383138817, '2025-10-23 15:36:51.230722+08', 1934276682383138817, '2025-10-23 15:36:51.231722+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_role_menu" VALUES (1983092960445550594, 1972908999916273666, 1981263449689100290, 1934276682383138817, '2025-10-28 16:46:20.515659+08', 1934276682383138817, '2025-10-28 16:46:20.515659+08', NULL, 0);

-- ----------------------------
-- Table structure for sys_rel_user_role
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_rel_user_role";
CREATE TABLE "domain_core"."sys_rel_user_role" (
  "id" int8 NOT NULL,
  "user_id" int8 NOT NULL,
  "role_id" int8 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_rel_user_role"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_rel_user_role"."user_id" IS '用户ID';
COMMENT ON COLUMN "domain_core"."sys_rel_user_role"."role_id" IS '角色ID';
COMMENT ON COLUMN "domain_core"."sys_rel_user_role"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_rel_user_role"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_rel_user_role"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_rel_user_role"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_rel_user_role"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_rel_user_role"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_rel_user_role" IS '中间表-用户到角色';

-- ----------------------------
-- Records of sys_rel_user_role
-- ----------------------------
INSERT INTO "domain_core"."sys_rel_user_role" VALUES (1999056546812579842, 1999056543696211969, 1932682189593350146, 1999056543696211969, '2025-12-11 17:59:56.092553+08', 1999056543696211969, '2025-12-11 17:59:56.092553+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_user_role" VALUES (2001897906724859906, 2001897905181356033, 1932685785802162178, 1999056543696211969, '2025-12-19 06:10:29.092552+08', 1999056543696211969, '2025-12-19 06:10:29.092552+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_user_role" VALUES (2001929942059626498, 2001929941732470786, 1972908999916273666, 1999056543696211969, '2025-12-19 08:17:46.915233+08', 1999056543696211969, '2025-12-19 08:17:46.915233+08', NULL, 0);
INSERT INTO "domain_core"."sys_rel_user_role" VALUES (2001930105973026818, 2001930105641676801, 1976487966371991554, 1999056543696211969, '2025-12-19 08:18:25.996543+08', 1999056543696211969, '2025-12-19 08:18:25.996543+08', NULL, 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_role";
CREATE TABLE "domain_core"."sys_role" (
  "id" int8 NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default",
  "code" varchar(100) COLLATE "pg_catalog"."default",
  "state" bool DEFAULT true,
  "scope" int4,
  "builtin" bool DEFAULT false,
  "remark" text COLLATE "pg_catalog"."default",
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_role"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_role"."name" IS '名称';
COMMENT ON COLUMN "domain_core"."sys_role"."code" IS '编码';
COMMENT ON COLUMN "domain_core"."sys_role"."state" IS '状态';
COMMENT ON COLUMN "domain_core"."sys_role"."scope" IS '范围';
COMMENT ON COLUMN "domain_core"."sys_role"."builtin" IS '是否内置';
COMMENT ON COLUMN "domain_core"."sys_role"."remark" IS '备注';
COMMENT ON COLUMN "domain_core"."sys_role"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_role"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_role"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_role"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_role"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_role"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_role" IS '角色表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO "domain_core"."sys_role" VALUES (1932682189593350146, '运维管理员', 'ROLE_DEV_OPS', 't', 0, 't', '运维人员使用,全局范围,拥有所有权限', 1927290201865945090, '2025-06-11 14:11:56+08', 1999056543696211969, '2025-12-23 08:59:52.611284+08', NULL, 0);
INSERT INTO "domain_core"."sys_role" VALUES (1932685785802162178, '系统管理员', 'ROLE_ADMIN_SYSTEM', 't', 1, 't', '系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容', 1927290201865945090, '2025-06-11 14:26:14+08', 1934276682383138817, '2025-09-03 16:12:44.003636+08', NULL, 0);
INSERT INTO "domain_core"."sys_role" VALUES (1972908999916273666, '用户', 'ROLE_USER', 't', 2, 't', '普通内置用户', 1934276682383138817, '2025-09-30 14:18:55.117373+08', 1934276682383138817, '2025-09-30 14:18:55.118381+08', NULL, 0);
INSERT INTO "domain_core"."sys_role" VALUES (1976487966371991554, '审计员', 'ROLE_AUDIT', 't', 1, 't', '审计员,审计本级及其下级相关数据', 1934276682383138817, '2025-10-10 11:20:27.210549+08', 1934276682383138817, '2025-10-10 11:20:27.211548+08', NULL, 0);

-- ----------------------------
-- Table structure for sys_role_data_scope
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_role_data_scope";
CREATE TABLE "domain_core"."sys_role_data_scope" (
  "id" int8 NOT NULL,
  "role_id" int8 NOT NULL,
  "scope_type" int4 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_role_data_scope"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope"."role_id" IS '角色ID';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope"."scope_type" IS '范围类型';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_role_data_scope" IS '角色数据范围';

-- ----------------------------
-- Records of sys_role_data_scope
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_data_scope_target
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_role_data_scope_target";
CREATE TABLE "domain_core"."sys_role_data_scope_target" (
  "id" int8 NOT NULL,
  "role_id" int8 NOT NULL,
  "target_id" int8 NOT NULL,
  "target_type" int4 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."role_id" IS '角色ID';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."target_id" IS '目标ID';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."target_type" IS '目标类型（DEPT / PROJECT / ORG / TENANT）';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."deleted" IS '是否删除';
COMMENT ON COLUMN "domain_core"."sys_role_data_scope_target"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_role_data_scope_target" IS '角色数据范围(自定义情况下)';

-- ----------------------------
-- Records of sys_role_data_scope_target
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_user";
CREATE TABLE "domain_core"."sys_user" (
  "id" int8 NOT NULL,
  "username" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "avatar" varchar(255) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 1,
  "real_name" varchar(50) COLLATE "pg_catalog"."default",
  "gender" int4 DEFAULT 0,
  "birthday" date,
  "phone" varchar(20) COLLATE "pg_catalog"."default",
  "email" varchar(100) COLLATE "pg_catalog"."default",
  "country" varchar(50) COLLATE "pg_catalog"."default",
  "city" varchar(50) COLLATE "pg_catalog"."default",
  "language" varchar(10) COLLATE "pg_catalog"."default" DEFAULT 'zh-CN'::character varying,
  "timezone" varchar(40) COLLATE "pg_catalog"."default" DEFAULT 'Asia/Shanghai'::character varying,
  "organization_id" int8 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_user"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_user"."username" IS '显示名称';
COMMENT ON COLUMN "domain_core"."sys_user"."avatar" IS '头像';
COMMENT ON COLUMN "domain_core"."sys_user"."status" IS '状态 (1:正常 0:禁用)';
COMMENT ON COLUMN "domain_core"."sys_user"."real_name" IS '真实姓名';
COMMENT ON COLUMN "domain_core"."sys_user"."gender" IS '性别(0:保密)';
COMMENT ON COLUMN "domain_core"."sys_user"."birthday" IS '生日';
COMMENT ON COLUMN "domain_core"."sys_user"."phone" IS '手机号';
COMMENT ON COLUMN "domain_core"."sys_user"."email" IS '邮箱';
COMMENT ON COLUMN "domain_core"."sys_user"."country" IS '国家';
COMMENT ON COLUMN "domain_core"."sys_user"."city" IS '城市';
COMMENT ON COLUMN "domain_core"."sys_user"."language" IS '语言';
COMMENT ON COLUMN "domain_core"."sys_user"."timezone" IS '时区';
COMMENT ON COLUMN "domain_core"."sys_user"."organization_id" IS '组织机构ID';
COMMENT ON COLUMN "domain_core"."sys_user"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_user"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_user"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_user"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_user"."deleted" IS '删除时间';
COMMENT ON COLUMN "domain_core"."sys_user"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_user" IS '用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO "domain_core"."sys_user" VALUES (2001897905181356033, '系统管理员', NULL, 0, '真实姓名', 0, '2025-12-18', '13312531253', 'admin@devops00.com', '123', '321', 'zh-CN', 'Etc/GMT+12', 1970016645676978177, 1999056543696211969, '2025-12-15 14:10:28.717559+08', 1999056543696211969, '2025-12-23 08:59:39.160801+08', NULL, 11);
INSERT INTO "domain_core"."sys_user" VALUES (2001929941732470786, '普通用户', NULL, 0, '普通用户', 4, '2025-12-19', '13112355213', 'user@devops00.com', '中国', '昆明', 'zh-CN', 'Asia/Shanghai', 1970016645676978177, 1999056543696211969, '2025-12-17 00:17:46.829222+08', 1999056543696211969, '2025-12-25 09:06:21.245021+08', NULL, 7);
INSERT INTO "domain_core"."sys_user" VALUES (2001930105641676801, '审计用户', NULL, 0, '审计用户', 1, '2025-12-19', '13312531253', 'audit@devops00.com', '中国', '昆明', 'zh-CN', 'Asia/Shanghai', 1970016645676978177, 1999056543696211969, '2025-12-18 16:18:25.910025+08', 1999056543696211969, '2025-12-23 08:41:53.996852+08', NULL, 2);
INSERT INTO "domain_core"."sys_user" VALUES (1999056543696211969, '运维管理员', NULL, 0, '运维管理员', 1, '2025-12-18', '13122336655', 'devops@devops00.com', '中国', '云南昆明', 'zh-CN', 'Asia/Shanghai', 1970016645676978177, 0, '2025-12-10 09:59:55.356552+08', 1999056543696211969, '2025-12-29 17:22:37.432548+08', NULL, 5);

-- ----------------------------
-- Table structure for sys_user_data_scope
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_user_data_scope";
CREATE TABLE "domain_core"."sys_user_data_scope" (
  "id" int8 NOT NULL,
  "user_id" int8 NOT NULL,
  "scope_type" int4 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_user_data_scope"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope"."user_id" IS '用户ID';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope"."scope_type" IS '数据范围类型';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope"."deleted" IS '删除时间';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_user_data_scope" IS '用户数据范围（直授，优先级高于角色）';

-- ----------------------------
-- Records of sys_user_data_scope
-- ----------------------------
INSERT INTO "domain_core"."sys_user_data_scope" VALUES (2003381826326839298, 2001929941732470786, 0, 1999056543696211969, '2025-12-22 16:27:03.088777+08', 1999056543696211969, '2025-12-25 09:06:21.394114+08', NULL, 2);
INSERT INTO "domain_core"."sys_user_data_scope" VALUES (2003385563149697026, 2001930105641676801, 4, 1999056543696211969, '2025-12-23 08:41:54.029361+08', 1999056543696211969, '2025-12-23 08:41:54.029361+08', NULL, 0);
INSERT INTO "domain_core"."sys_user_data_scope" VALUES (2003390030716821505, 2001897905181356033, 0, 1999056543696211969, '2025-12-23 08:59:39.176136+08', 1999056543696211969, '2025-12-23 08:59:39.176136+08', NULL, 0);
INSERT INTO "domain_core"."sys_user_data_scope" VALUES (2003389991974035458, 1999056543696211969, 0, 1999056543696211969, '2025-12-23 00:59:29.93832+08', 1999056543696211969, '2025-12-29 17:22:37.470252+08', NULL, 2);

-- ----------------------------
-- Table structure for sys_user_data_scope_target
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_user_data_scope_target";
CREATE TABLE "domain_core"."sys_user_data_scope_target" (
  "id" int8 NOT NULL,
  "user_id" int8 NOT NULL,
  "target_id" int8 NOT NULL,
  "target_type" int4 NOT NULL,
  "created_by" int8,
  "created_at" timestamptz(6) NOT NULL,
  "updated_by" int8,
  "updated_at" timestamptz(6) NOT NULL,
  "deleted" timestamptz(6),
  "version" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."id" IS '主键ID';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."user_id" IS '用户ID';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."target_id" IS '目标ID';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."target_type" IS '目标类型';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."created_by" IS '创建人';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."created_at" IS '创建时间';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."deleted" IS '删除时间';
COMMENT ON COLUMN "domain_core"."sys_user_data_scope_target"."version" IS '乐观锁';
COMMENT ON TABLE "domain_core"."sys_user_data_scope_target" IS '用户数据范围,自定义情况下使用';

-- ----------------------------
-- Records of sys_user_data_scope_target
-- ----------------------------
INSERT INTO "domain_core"."sys_user_data_scope_target" VALUES (2003385563267137538, 2001930105641676801, 1970028813113823233, 0, 1999056543696211969, '2025-12-23 08:41:54.043512+08', 1999056543696211969, '2025-12-23 08:41:54.043512+08', NULL, 0);
INSERT INTO "domain_core"."sys_user_data_scope_target" VALUES (2003385563267137539, 2001930105641676801, 1970020468801880066, 0, 1999056543696211969, '2025-12-23 08:41:54.045518+08', 1999056543696211969, '2025-12-23 08:41:54.045518+08', NULL, 0);

-- ----------------------------
-- Function structure for armor
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."armor"(bytea, _text, _text);
CREATE FUNCTION "domain_core"."armor"(bytea, _text, _text)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pg_armor'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for armor
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."armor"(bytea);
CREATE FUNCTION "domain_core"."armor"(bytea)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pg_armor'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for crypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."crypt"(text, text);
CREATE FUNCTION "domain_core"."crypt"(text, text)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pg_crypt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for dearmor
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."dearmor"(text);
CREATE FUNCTION "domain_core"."dearmor"(text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_dearmor'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for decrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."decrypt"(bytea, bytea, text);
CREATE FUNCTION "domain_core"."decrypt"(bytea, bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_decrypt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for decrypt_iv
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."decrypt_iv"(bytea, bytea, bytea, text);
CREATE FUNCTION "domain_core"."decrypt_iv"(bytea, bytea, bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_decrypt_iv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for digest
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."digest"(text, text);
CREATE FUNCTION "domain_core"."digest"(text, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_digest'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for digest
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."digest"(bytea, text);
CREATE FUNCTION "domain_core"."digest"(bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_digest'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for encrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."encrypt"(bytea, bytea, text);
CREATE FUNCTION "domain_core"."encrypt"(bytea, bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_encrypt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for encrypt_iv
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."encrypt_iv"(bytea, bytea, bytea, text);
CREATE FUNCTION "domain_core"."encrypt_iv"(bytea, bytea, bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_encrypt_iv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for fips_mode
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."fips_mode"();
CREATE FUNCTION "domain_core"."fips_mode"()
  RETURNS "pg_catalog"."bool" AS '$libdir/pgcrypto', 'pg_check_fipsmode'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for gen_random_bytes
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."gen_random_bytes"(int4);
CREATE FUNCTION "domain_core"."gen_random_bytes"(int4)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_random_bytes'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for gen_random_uuid
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."gen_random_uuid"();
CREATE FUNCTION "domain_core"."gen_random_uuid"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/pgcrypto', 'pg_random_uuid'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for gen_salt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."gen_salt"(text, int4);
CREATE FUNCTION "domain_core"."gen_salt"(text, int4)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pg_gen_salt_rounds'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for gen_salt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."gen_salt"(text);
CREATE FUNCTION "domain_core"."gen_salt"(text)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pg_gen_salt'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for hmac
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."hmac"(text, text, text);
CREATE FUNCTION "domain_core"."hmac"(text, text, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_hmac'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for hmac
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."hmac"(bytea, bytea, text);
CREATE FUNCTION "domain_core"."hmac"(bytea, bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pg_hmac'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_armor_headers
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_armor_headers"(text, OUT "key" text, OUT "value" text);
CREATE FUNCTION "domain_core"."pgp_armor_headers"(IN text, OUT "key" text, OUT "value" text)
  RETURNS SETOF "pg_catalog"."record" AS '$libdir/pgcrypto', 'pgp_armor_headers'
  LANGUAGE c IMMUTABLE STRICT
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for pgp_key_id
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_key_id"(bytea);
CREATE FUNCTION "domain_core"."pgp_key_id"(bytea)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pgp_key_id_w'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_decrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_decrypt"(bytea, bytea, text);
CREATE FUNCTION "domain_core"."pgp_pub_decrypt"(bytea, bytea, text)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pgp_pub_decrypt_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_decrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_decrypt"(bytea, bytea);
CREATE FUNCTION "domain_core"."pgp_pub_decrypt"(bytea, bytea)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pgp_pub_decrypt_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_decrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_decrypt"(bytea, bytea, text, text);
CREATE FUNCTION "domain_core"."pgp_pub_decrypt"(bytea, bytea, text, text)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pgp_pub_decrypt_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_decrypt_bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_decrypt_bytea"(bytea, bytea, text);
CREATE FUNCTION "domain_core"."pgp_pub_decrypt_bytea"(bytea, bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_pub_decrypt_bytea'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_decrypt_bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_decrypt_bytea"(bytea, bytea, text, text);
CREATE FUNCTION "domain_core"."pgp_pub_decrypt_bytea"(bytea, bytea, text, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_pub_decrypt_bytea'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_decrypt_bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_decrypt_bytea"(bytea, bytea);
CREATE FUNCTION "domain_core"."pgp_pub_decrypt_bytea"(bytea, bytea)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_pub_decrypt_bytea'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_encrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_encrypt"(text, bytea, text);
CREATE FUNCTION "domain_core"."pgp_pub_encrypt"(text, bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_pub_encrypt_text'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_encrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_encrypt"(text, bytea);
CREATE FUNCTION "domain_core"."pgp_pub_encrypt"(text, bytea)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_pub_encrypt_text'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_encrypt_bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_encrypt_bytea"(bytea, bytea);
CREATE FUNCTION "domain_core"."pgp_pub_encrypt_bytea"(bytea, bytea)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_pub_encrypt_bytea'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_pub_encrypt_bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_pub_encrypt_bytea"(bytea, bytea, text);
CREATE FUNCTION "domain_core"."pgp_pub_encrypt_bytea"(bytea, bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_pub_encrypt_bytea'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_sym_decrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_sym_decrypt"(bytea, text);
CREATE FUNCTION "domain_core"."pgp_sym_decrypt"(bytea, text)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pgp_sym_decrypt_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_sym_decrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_sym_decrypt"(bytea, text, text);
CREATE FUNCTION "domain_core"."pgp_sym_decrypt"(bytea, text, text)
  RETURNS "pg_catalog"."text" AS '$libdir/pgcrypto', 'pgp_sym_decrypt_text'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_sym_decrypt_bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_sym_decrypt_bytea"(bytea, text, text);
CREATE FUNCTION "domain_core"."pgp_sym_decrypt_bytea"(bytea, text, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_sym_decrypt_bytea'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_sym_decrypt_bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_sym_decrypt_bytea"(bytea, text);
CREATE FUNCTION "domain_core"."pgp_sym_decrypt_bytea"(bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_sym_decrypt_bytea'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_sym_encrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_sym_encrypt"(text, text, text);
CREATE FUNCTION "domain_core"."pgp_sym_encrypt"(text, text, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_sym_encrypt_text'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_sym_encrypt
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_sym_encrypt"(text, text);
CREATE FUNCTION "domain_core"."pgp_sym_encrypt"(text, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_sym_encrypt_text'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_sym_encrypt_bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_sym_encrypt_bytea"(bytea, text);
CREATE FUNCTION "domain_core"."pgp_sym_encrypt_bytea"(bytea, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_sym_encrypt_bytea'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for pgp_sym_encrypt_bytea
-- ----------------------------
DROP FUNCTION IF EXISTS "domain_core"."pgp_sym_encrypt_bytea"(bytea, text, text);
CREATE FUNCTION "domain_core"."pgp_sym_encrypt_bytea"(bytea, text, text)
  RETURNS "pg_catalog"."bytea" AS '$libdir/pgcrypto', 'pgp_sym_encrypt_bytea'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Checks structure for table sys_account
-- ----------------------------
ALTER TABLE "domain_core"."sys_account" ADD CONSTRAINT "chk_account_identifier" CHECK (type = 1 AND login_name IS NOT NULL OR type = 2 AND phone IS NOT NULL OR type = 3 AND email IS NOT NULL OR type = 4 AND openid IS NOT NULL);

-- ----------------------------
-- Primary Key structure for table sys_account
-- ----------------------------
ALTER TABLE "domain_core"."sys_account" ADD CONSTRAINT "sys_account_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_authority
-- ----------------------------
ALTER TABLE "domain_core"."sys_authority" ADD CONSTRAINT "sys_authority_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_config
-- ----------------------------
ALTER TABLE "domain_core"."sys_config" ADD CONSTRAINT "sys_config_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_dict_group
-- ----------------------------
ALTER TABLE "domain_core"."sys_dict_group" ADD CONSTRAINT "sys_dict_group_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_dict_item
-- ----------------------------
ALTER TABLE "domain_core"."sys_dict_item" ADD CONSTRAINT "sys_dict_item_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_file_chunk
-- ----------------------------
ALTER TABLE "domain_core"."sys_file_chunk" ADD CONSTRAINT "sys_file_chunk_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_file_info
-- ----------------------------
ALTER TABLE "domain_core"."sys_file_info" ADD CONSTRAINT "sys_file_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_log
-- ----------------------------
ALTER TABLE "domain_core"."sys_log" ADD CONSTRAINT "sys_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_organization
-- ----------------------------
ALTER TABLE "domain_core"."sys_organization" ADD CONSTRAINT "sys_organization_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_rel_role_authority
-- ----------------------------
ALTER TABLE "domain_core"."sys_rel_role_authority" ADD CONSTRAINT "sys_rel_role_authority_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_rel_role_menu
-- ----------------------------
ALTER TABLE "domain_core"."sys_rel_role_menu" ADD CONSTRAINT "sys_rel_role_menu_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_rel_user_role
-- ----------------------------
ALTER TABLE "domain_core"."sys_rel_user_role" ADD CONSTRAINT "sys_rel_user_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_role
-- ----------------------------
ALTER TABLE "domain_core"."sys_role" ADD CONSTRAINT "sys_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_role_data_scope
-- ----------------------------
ALTER TABLE "domain_core"."sys_role_data_scope" ADD CONSTRAINT "sys_role_data_scope_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_role_data_scope_target
-- ----------------------------
ALTER TABLE "domain_core"."sys_role_data_scope_target" ADD CONSTRAINT "sys_role_data_scope_target_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_user
-- ----------------------------
ALTER TABLE "domain_core"."sys_user" ADD CONSTRAINT "sys_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_user_data_scope
-- ----------------------------
ALTER TABLE "domain_core"."sys_user_data_scope" ADD CONSTRAINT "sys_user_data_scope_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_user_data_scope_target
-- ----------------------------
ALTER TABLE "domain_core"."sys_user_data_scope_target" ADD CONSTRAINT "sys_user_data_scope_target_pkey" PRIMARY KEY ("id");
