/*
 Navicat Premium Dump SQL

 Source Server         : PostgreSQL@localhost
 Source Server Type    : PostgreSQL
 Source Server Version : 170004 (170004)
 Source Host           : localhost:5432
 Source Catalog        : spectra_framework_db
 Source Schema         : db_system

 Target Server Type    : PostgreSQL
 Target Server Version : 170004 (170004)
 File Encoding         : 65001

 Date: 24/07/2025 17:28:56
*/


-- ----------------------------
-- Table structure for t_dict_data
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_dict_data";
CREATE TABLE "db_system"."t_dict_data" (
  "id" int8 NOT NULL,
  "gid" int8 NOT NULL,
  "label" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "value" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "sort" int2 NOT NULL DEFAULT 0,
  "state" int2 NOT NULL,
  "remark" varchar(255) COLLATE "pg_catalog"."default",
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_dict_data"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_dict_data"."gid" IS '字典组ID';
COMMENT ON COLUMN "db_system"."t_dict_data"."label" IS '标签';
COMMENT ON COLUMN "db_system"."t_dict_data"."value" IS '值';
COMMENT ON COLUMN "db_system"."t_dict_data"."sort" IS '排序';
COMMENT ON COLUMN "db_system"."t_dict_data"."state" IS '状态';
COMMENT ON COLUMN "db_system"."t_dict_data"."remark" IS '备注';
COMMENT ON COLUMN "db_system"."t_dict_data"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_dict_data"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_dict_data"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_dict_data"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_dict_data"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_dict_data" IS '数据字典(字典值)';

-- ----------------------------
-- Records of t_dict_data
-- ----------------------------
INSERT INTO "db_system"."t_dict_data" VALUES (1942142254075305986, 1942142182856024066, '全局', '0', 999, 0, '全局范围可查询', 1927290201865945090, '2025-07-07 16:42:51.29908', 1927290201865945090, '2025-07-07 16:42:51.29908', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942142354801516546, 1942142182856024066, '本级', '1', 999, 0, '可以查询当前所属的组织机构+部门本级别的内容', 1927290201865945090, '2025-07-07 16:43:15.314449', 1927290201865945090, '2025-07-07 16:43:15.314449', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942142489367371777, 1942142182856024066, '本级包含下级', '2', 999, 0, '可以查询当前组织机构+部门,包含子级组织机构+部门的数据', 1927290201865945090, '2025-07-07 16:43:47.386874', 1927290201865945090, '2025-07-07 16:43:47.386874', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942143228055609345, 1942142790749085698, '正常', '0', 999, 0, '正常', 1927290201865945090, '2025-07-07 16:46:43.508788', 1927290201865945090, '2025-07-07 16:46:43.509787', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942143271655399425, 1942142790749085698, '冻结', '1', 999, 0, '冻结', 1927290201865945090, '2025-07-07 16:46:53.905348', 1927290201865945090, '2025-07-07 16:46:53.906348', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942143325908721665, 1942142790749085698, '封禁', '2', 999, 0, '封禁', 1927290201865945090, '2025-07-07 16:47:06.841538', 1927290201865945090, '2025-07-07 16:47:06.842538', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942143694822924289, 1942143605777850369, '禁用', '1', 999, 0, '禁用', 1927290201865945090, '2025-07-07 16:48:34.796278', 1927290201865945090, '2025-07-07 16:48:34.796278', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942151875150163969, 1942151722745933825, '邮箱登录', '0', 999, 0, '邮箱登录', 1927290201865945090, '2025-07-07 17:21:05.137799', 1927290201865945090, '2025-07-07 17:21:05.137799', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942151904090861570, 1942151722745933825, '手机号码', '1', 999, 0, '手机号码', 1927290201865945090, '2025-07-07 17:21:12.03659', 1927290201865945090, '2025-07-07 17:21:12.03659', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942151942581989377, 1942151722745933825, '微信登录', '2', 999, 0, '微信登录', 1927290201865945090, '2025-07-07 17:21:21.209022', 1927290201865945090, '2025-07-07 17:21:21.21002', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942143658663829506, 1942143605777850369, '启用', '0', 999, 0, '启用', 1927290201865945090, '2025-07-07 16:48:26.176609', 1927290201865945090, '2025-07-07 16:48:26.176609', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942506181581787138, 1942429096108576770, '业务测试_V_1', 'yw_test_v_1', 1, 0, NULL, 1927290201865945090, '2025-07-08 16:48:58.368541', 1927290201865945090, '2025-07-08 16:48:58.36954', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1942506793794981889, 1942429096108576770, '业务测试_V_2', 'yw_test_v_2', 1, 1, NULL, 1927290201865945090, '2025-07-08 16:51:24.338238', 1927290201865945090, '2025-07-11 10:15:42.122677', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1944677811678519297, 1944677668501757953, '集团总部', '1', 999, 0, NULL, 1927290201865945090, '2025-07-14 16:38:15.341787', 1927290201865945090, '2025-07-14 16:38:15.341787', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1944677899893121026, 1944677668501757953, '省级公司/大区', '2', 999, 0, NULL, 1927290201865945090, '2025-07-14 16:38:36.366005', 1927290201865945090, '2025-07-14 16:38:36.366005', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1944677967375278082, 1944677668501757953, '市级公司/分公司', '3', 999, 0, NULL, 1927290201865945090, '2025-07-14 16:38:52.457352', 1927290201865945090, '2025-07-14 16:38:52.457352', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1944678003773448194, 1944677668501757953, '部门', '4', 999, 0, NULL, 1927290201865945090, '2025-07-14 16:39:01.14016', 1927290201865945090, '2025-07-14 16:39:01.14016', NULL);
INSERT INTO "db_system"."t_dict_data" VALUES (1944678052813250561, 1944677668501757953, '科室/小组', '5', 999, 0, NULL, 1927290201865945090, '2025-07-14 16:39:12.820422', 1927290201865945090, '2025-07-14 16:39:12.826403', NULL);

-- ----------------------------
-- Table structure for t_dict_group
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_dict_group";
CREATE TABLE "db_system"."t_dict_group" (
  "id" int8 NOT NULL,
  "pid" int8,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "state" int2 NOT NULL,
  "remark" varchar(255) COLLATE "pg_catalog"."default",
  "builtin" bool NOT NULL DEFAULT false,
  "hide" bool NOT NULL DEFAULT false,
  "created_by" int8,
  "created_at" timestamp(6),
  "updated_by" int8,
  "updated_at" timestamp(6),
  "deleted" timestamp(6)
)
;
COMMENT ON COLUMN "db_system"."t_dict_group"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_dict_group"."pid" IS '父级ID';
COMMENT ON COLUMN "db_system"."t_dict_group"."name" IS '字典名称';
COMMENT ON COLUMN "db_system"."t_dict_group"."code" IS '字典编码';
COMMENT ON COLUMN "db_system"."t_dict_group"."state" IS '字典状态';
COMMENT ON COLUMN "db_system"."t_dict_group"."remark" IS '备注';
COMMENT ON COLUMN "db_system"."t_dict_group"."builtin" IS '是否内置字段,为true则不允许他进行修改删除操作';
COMMENT ON COLUMN "db_system"."t_dict_group"."hide" IS '是否隐藏,为true则前端不可直接进行修改删除等操作';
COMMENT ON COLUMN "db_system"."t_dict_group"."created_by" IS '创建人';
COMMENT ON COLUMN "db_system"."t_dict_group"."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system"."t_dict_group"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system"."t_dict_group"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system"."t_dict_group"."deleted" IS '是否删除';
COMMENT ON TABLE "db_system"."t_dict_group" IS '数据字典(字典类型)';

-- ----------------------------
-- Records of t_dict_group
-- ----------------------------
INSERT INTO "db_system"."t_dict_group" VALUES (1942142790749085698, 1942142921347129346, '用户状态', 'sys_user_state', 0, '用户状态', 't', 'f', 1927290201865945090, '2025-07-07 16:44:59.241607', 1927290201865945090, '2025-07-07 16:44:59.242608', NULL);
INSERT INTO "db_system"."t_dict_group" VALUES (1942143605777850369, 1942142921347129346, '通用状态', 'sys_common_state', 0, '通用状态', 't', 'f', 1927290201865945090, '2025-07-07 16:48:13.563688', 1927290201865945090, '2025-07-07 16:48:13.563688', NULL);
INSERT INTO "db_system"."t_dict_group" VALUES (1942142921347129346, NULL, '系统配置', 'sys', 0, '系统配置相关的字典组.基本为内置不可修改的字典组', 't', 'f', 1927290201865945090, '2025-07-07 16:45:30.388337', 1927290201865945090, '2025-07-07 16:45:30.389337', NULL);
INSERT INTO "db_system"."t_dict_group" VALUES (1942150470595194881, NULL, '业务配置', 'business', 0, '业务部分需要的配置', 'f', 'f', 1927290201865945090, '2025-07-07 17:15:30.261611', 1927290201865945090, '2025-07-07 17:15:30.262611', NULL);
INSERT INTO "db_system"."t_dict_group" VALUES (1942151722745933825, 1942142921347129346, '账号类型', 'sys_account_type', 0, '账号类型，一个用户可以有多个不同账号类型的账号，对应不同登录方式', 't', 'f', 1927290201865945090, '2025-07-07 17:20:28.804878', 1927290201865945090, '2025-07-07 17:20:28.804878', NULL);
INSERT INTO "db_system"."t_dict_group" VALUES (1942429096108576770, 1942150470595194881, '业务测试1', 'business_test_1', 0, '', 'f', 'f', 1927290201865945090, '2025-07-08 11:42:39.770422', 1927290201865945090, '2025-07-08 11:42:39.770422', NULL);
INSERT INTO "db_system"."t_dict_group" VALUES (1942142182856024066, 1942142921347129346, '权限范围', 'sys_power_scope', 0, '系统权限范围', 't', 'f', 1927290201865945090, '2025-07-07 16:42:34.314302', 1927290201865945090, '2025-07-07 16:42:34.314302', NULL);
INSERT INTO "db_system"."t_dict_group" VALUES (1944677668501757953, 1942142921347129346, '组织机构类型', 'sys_organization_type', 0, NULL, 't', 'f', 1927290201865945090, '2025-07-14 16:37:41.203419', 1927290201865945090, '2025-07-14 16:37:41.204416', NULL);

-- ----------------------------
-- Table structure for t_menu
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_menu";
CREATE TABLE "db_system"."t_menu" (
  "id" int8 NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "pid" int8,
  "icon" varchar(100) COLLATE "pg_catalog"."default",
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
COMMENT ON COLUMN "db_system"."t_menu"."name" IS '名称';
COMMENT ON COLUMN "db_system"."t_menu"."pid" IS '父级ID';
COMMENT ON COLUMN "db_system"."t_menu"."icon" IS '图标';
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
INSERT INTO "db_system"."t_menu" VALUES (1929929620816441347, '列表示例', 1929928379667386370, 'icon-module', 'table', '/Example/Table/index', NULL, 1, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-11 09:47:10.002749', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620816441348, '表单示例', 1929928379667386370, 'icon-module', 'form', '/Example/Form/index', NULL, 2, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-11 09:47:13.831965', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620816441346, '图表示例', 1929928379667386370, 'icon-module', 'echarts', '/Example/Echarts/index', NULL, 3, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-11 09:47:18.286463', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526785, '访问控制', 1929928379575111682, 'icon-module', 'RBAC', '/System/RBAC/index', NULL, 2, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:38.885015', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526789, '菜单管理', 1929928379575111682, 'icon-module', 'menu', '/System/Menu/index', NULL, 3, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:42.708315', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526787, '字典管理', 1929928379575111682, 'icon-module', 'dict', '/System/Dict/index', NULL, 4, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:45.588571', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526790, '文件存储', 1929928379575111682, 'icon-module', 'storage', '/System/Storage/index', NULL, 5, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:48.846378', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1932983846772363266, '部门管理', 1929928379575111682, 'icon-module', 'dept', '/System/Dept/index', NULL, 1, 1927290201865945090, '2025-06-12 10:10:36.840451', 1927290201865945090, '2025-06-12 10:10:36.840451', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620715778049, '用户管理', 1929928379575111682, 'icon-module', 'user', '/System/User/index', NULL, 0, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-13 15:32:51.681846', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929928379575111682, '系统管理', NULL, 'icon-setting', '/system', 'layout', 'layout', 0, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-05 11:37:45.582763', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1943557799479791617, 'Markdown', 1929928379667386370, 'icon-module', 'markdown', '/Example/Markdown/index', NULL, 4, 1927290201865945090, '2025-07-11 14:27:43.626812', 1927290201865945090, '2025-07-11 14:27:54.073722', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1947937675586547713, '数据监控', 1947937225978130433, 'icon-module', 'database', '/Monitor/Database/index', NULL, 1, 1927290201865945090, '2025-07-23 16:31:47.451283', 1927290201865945090, '2025-07-23 16:31:47.452283', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1947943281978445825, '在线用户', 1947937225978130433, 'icon-module', 'online', '/Monitor/Online/index', NULL, 0, 1927290201865945090, '2025-07-23 16:54:04.131705', 1927290201865945090, '2025-07-23 16:55:35.179344', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929929620753526788, '定时任务', 1947937225978130433, 'icon-module', 'task', '/Monitor/Task/index', NULL, 2, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-07-23 16:56:10.572491', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1947944068104261634, '服务监控', 1947937225978130433, 'icon-module', 'server', '/Monitor/Server/index', NULL, 3, 1927290201865945090, '2025-07-23 16:57:11.555295', 1927290201865945090, '2025-07-23 16:57:11.555295', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1947944189344813057, '缓存监控', 1947937225978130433, 'icon-module', 'cache', '/Monitor/Cache/index', NULL, 4, 1927290201865945090, '2025-07-23 16:57:40.453861', 1927290201865945090, '2025-07-23 16:57:40.454861', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1929928379667386370, '组件示例', NULL, 'icon-setting', '/example', 'layout', 'layout', 2, 1927290201865945090, '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-07-23 16:59:53.164102', NULL);
INSERT INTO "db_system"."t_menu" VALUES (1947937225978130433, '系统监控', NULL, 'icon-setting', '/monitor', 'layout', 'layout', 1, 1927290201865945090, '2025-07-23 16:30:00.263877', 1927290201865945090, '2025-07-23 16:59:58.752796', NULL);

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
COMMENT ON TABLE "db_system"."t_operation_log" IS '操作日志表';

-- ----------------------------
-- Records of t_operation_log
-- ----------------------------
INSERT INTO "db_system"."t_operation_log" VALUES (1947951946055798786, '获取组织机构树形列表', 200, '127.0.0.1', 'GET', '/api/organization/tree', NULL, '[{"id":"1944679712507760641","name":"道一科技","code":"ORG_ROOT","type":1,"address":"中国","children":[{"id":"1944693648674168833","pid":"1944679712507760641","name":"西部大区","code":"ORG_WEST","type":2,"address":"西部大区","remark":"整个西部大区相关的"},{"id":"1944694522943242242","pid":"1944679712507760641","name":"东部大区","code":"ORG_EAST","type":2,"address":"东部大区","remark":"整个东部大区相关的"},{"id":"1944694666350690306","pid":"1944679712507760641","name":"南部大区","code":"ORG_SOUTH","type":2,"address":"南部大区","remark":"整个南部大区相关的","children":[{"id":"1944710101167226882","pid":"1944694666350690306","name":"道一科技云南分公司","code":"ORG_YUNNAN","type":3,"address":"云南省昆明市五华区XX大道XX号","remark":"道一科技云南分公司"}]},{"id":"1944694800480337921","pid":"1944679712507760641","name":"北部大区","code":"ORG_NORTH","type":2,"address":"北部大区","remark":"整个北部大区相关的"}]}]', 4, 1927290201865945090, '2025-07-23 17:28:29.806058', 1927290201865945090, '2025-07-23 17:28:29.806058', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951949113446401, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/role/list', NULL, '[{"id":"1932682189593350146","name":"运维管理员","state":true,"scope":2,"remark":"运维人员使用,全局范围,拥有所有权限"},{"id":"1932685785802162178","name":"系统管理员","state":true,"scope":0,"remark":"系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"}]', 4, 1927290201865945090, '2025-07-23 17:28:30.535565', 1927290201865945090, '2025-07-23 17:28:30.535565', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951949314772994, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', NULL, '{"records":[{"id":"1934276682383138817","name":"平台管理员","email":"yangxj96@gmail.com","state":0,"roles":[{"id":"1932682189593350146","name":"运维管理员","state":true,"scope":2,"remark":"运维人员使用,全局范围,拥有所有权限"}],"organization_id":"1944679712507760641","organization_name":"道一科技"},{"id":"1937354420709711873","name":"超级管理员","email":"sysadmin@1.com","state":0,"roles":[{"id":"1932685785802162178","name":"系统管理员","state":true,"scope":0,"remark":"系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"}]},{"id":"1937706586813169665","name":"测试用户","email":"ceshi@1.com","state":0,"roles":[{"id":"1932687324356775938","name":"小组长","state":false,"scope":1,"remark":"测试禁用状态"}]}],"total":3,"size":100,"current":1,"pages":1}', 32, 1927290201865945090, '2025-07-23 17:28:30.583564', 1927290201865945090, '2025-07-23 17:28:30.583564', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951953244835841, '获取组织机构树形列表', 200, '127.0.0.1', 'GET', '/api/organization/tree', NULL, '[{"id":"1944679712507760641","name":"道一科技","code":"ORG_ROOT","type":1,"address":"中国","children":[{"id":"1944693648674168833","pid":"1944679712507760641","name":"西部大区","code":"ORG_WEST","type":2,"address":"西部大区","remark":"整个西部大区相关的"},{"id":"1944694522943242242","pid":"1944679712507760641","name":"东部大区","code":"ORG_EAST","type":2,"address":"东部大区","remark":"整个东部大区相关的"},{"id":"1944694666350690306","pid":"1944679712507760641","name":"南部大区","code":"ORG_SOUTH","type":2,"address":"南部大区","remark":"整个南部大区相关的","children":[{"id":"1944710101167226882","pid":"1944694666350690306","name":"道一科技云南分公司","code":"ORG_YUNNAN","type":3,"address":"云南省昆明市五华区XX大道XX号","remark":"道一科技云南分公司"}]},{"id":"1944694800480337921","pid":"1944679712507760641","name":"北部大区","code":"ORG_NORTH","type":2,"address":"北部大区","remark":"整个北部大区相关的"}]}]', 5, 1927290201865945090, '2025-07-23 17:28:31.507265', 1927290201865945090, '2025-07-23 17:28:31.507265', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951956566724610, '获取权限树列表', 200, '127.0.0.1', 'GET', '/api/permission/authority/tree', NULL, '[{"id":1943513441539891202,"name":"字典权限","code":"MENU:*","children":[{"id":1943513442269700097,"pid":1943513441539891202,"name":"字典新增","code":"MENU:INSERT"},{"id":1943513442269700098,"pid":1943513441539891202,"name":"字典修改","code":"MENU:UPDATE"},{"id":1943513442269700099,"pid":1943513441539891202,"name":"字典删除","code":"MENU:DELETE"}]}]', 5, 1927290201865945090, '2025-07-23 17:28:32.304308', 1927290201865945090, '2025-07-23 17:28:32.304308', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951956566724611, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', NULL, '[{"id":"1929928379575111682","icon":"icon-setting","name":"系统管理","path":"/system","component":"layout","layout":"layout","sort":0,"children":[{"id":"1929929620715778049","pid":"1929928379575111682","icon":"icon-module","name":"用户管理","path":"user","component":"/System/User/index","sort":0,"children":[]},{"id":"1932983846772363266","pid":"1929928379575111682","icon":"icon-module","name":"部门管理","path":"dept","component":"/System/Dept/index","sort":1,"children":[]},{"id":"1929929620753526785","pid":"1929928379575111682","icon":"icon-module","name":"访问控制","path":"RBAC","component":"/System/RBAC/index","sort":2,"children":[]},{"id":"1929929620753526789","pid":"1929928379575111682","icon":"icon-module","name":"菜单管理","path":"menu","component":"/System/Menu/index","sort":3,"children":[]},{"id":"1929929620753526787","pid":"1929928379575111682","icon":"icon-module","name":"字典管理","path":"dict","component":"/System/Dict/index","sort":4,"children":[]},{"id":"1929929620753526790","pid":"1929928379575111682","icon":"icon-module","name":"文件存储","path":"storage","component":"/System/Storage/index","sort":5,"children":[]}]},{"id":"1947937225978130433","icon":"icon-setting","name":"系统监控","path":"/monitor","component":"layout","layout":"layout","sort":1,"children":[{"id":"1947943281978445825","pid":"1947937225978130433","icon":"icon-module","name":"在线用户","path":"online","component":"/Monitor/Online/index","sort":0,"children":[]},{"id":"1947937675586547713","pid":"1947937225978130433","icon":"icon-module","name":"数据监控","path":"database","component":"/Monitor/Database/index","sort":1,"children":[]},{"id":"1929929620753526788","pid":"1947937225978130433","icon":"icon-module","name":"定时任务","path":"task","component":"/Monitor/Task/index","sort":2,"children":[]},{"id":"1947944068104261634","pid":"1947937225978130433","icon":"icon-module","name":"服务监控","path":"server","component":"/Monitor/Server/index","sort":3,"children":[]},{"id":"1947944189344813057","pid":"1947937225978130433","icon":"icon-module","name":"缓存监控","path":"cache","component":"/Monitor/Cache/index","sort":4,"children":[]}]},{"id":"1929928379667386370","icon":"icon-setting","name":"组件示例","path":"/example","component":"layout","layout":"layout","sort":2,"children":[{"id":"1929929620816441347","pid":"1929928379667386370","icon":"icon-module","name":"列表示例","path":"table","component":"/Example/Table/index","sort":1,"children":[]},{"id":"1929929620816441348","pid":"1929928379667386370","icon":"icon-module","name":"表单示例","path":"form","component":"/Example/Form/index","sort":2,"children":[]},{"id":"1929929620816441346","pid":"1929928379667386370","icon":"icon-module","name":"图表示例","path":"echarts","component":"/Example/Echarts/index","sort":3,"children":[]},{"id":"1943557799479791617","pid":"1929928379667386370","icon":"icon-module","name":"Markdown","path":"markdown","component":"/Example/Markdown/index","sort":4,"children":[]}]}]', 6, 1927290201865945090, '2025-07-23 17:28:32.305307', 1927290201865945090, '2025-07-23 17:28:32.305307', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951956633833473, '分页查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/role/page', NULL, '{"records":[{"id":"1932682189593350146","name":"运维管理员","state":true,"scope":2,"remark":"运维人员使用,全局范围,拥有所有权限"},{"id":"1932685785802162178","name":"系统管理员","state":true,"scope":0,"remark":"系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"},{"id":"1932687324356775938","name":"小组长","state":false,"scope":1,"remark":"测试禁用状态"}],"total":3,"size":100,"current":1,"pages":1}', 18, 1927290201865945090, '2025-07-23 17:28:32.318308', 1927290201865945090, '2025-07-23 17:28:32.318308', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951959444017153, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', NULL, '[{"id":"1929928379575111682","icon":"icon-setting","name":"系统管理","path":"/system","component":"layout","layout":"layout","sort":0,"children":[{"id":"1929929620715778049","pid":"1929928379575111682","icon":"icon-module","name":"用户管理","path":"user","component":"/System/User/index","sort":0,"children":[]},{"id":"1932983846772363266","pid":"1929928379575111682","icon":"icon-module","name":"部门管理","path":"dept","component":"/System/Dept/index","sort":1,"children":[]},{"id":"1929929620753526785","pid":"1929928379575111682","icon":"icon-module","name":"访问控制","path":"RBAC","component":"/System/RBAC/index","sort":2,"children":[]},{"id":"1929929620753526789","pid":"1929928379575111682","icon":"icon-module","name":"菜单管理","path":"menu","component":"/System/Menu/index","sort":3,"children":[]},{"id":"1929929620753526787","pid":"1929928379575111682","icon":"icon-module","name":"字典管理","path":"dict","component":"/System/Dict/index","sort":4,"children":[]},{"id":"1929929620753526790","pid":"1929928379575111682","icon":"icon-module","name":"文件存储","path":"storage","component":"/System/Storage/index","sort":5,"children":[]}]},{"id":"1947937225978130433","icon":"icon-setting","name":"系统监控","path":"/monitor","component":"layout","layout":"layout","sort":1,"children":[{"id":"1947943281978445825","pid":"1947937225978130433","icon":"icon-module","name":"在线用户","path":"online","component":"/Monitor/Online/index","sort":0,"children":[]},{"id":"1947937675586547713","pid":"1947937225978130433","icon":"icon-module","name":"数据监控","path":"database","component":"/Monitor/Database/index","sort":1,"children":[]},{"id":"1929929620753526788","pid":"1947937225978130433","icon":"icon-module","name":"定时任务","path":"task","component":"/Monitor/Task/index","sort":2,"children":[]},{"id":"1947944068104261634","pid":"1947937225978130433","icon":"icon-module","name":"服务监控","path":"server","component":"/Monitor/Server/index","sort":3,"children":[]},{"id":"1947944189344813057","pid":"1947937225978130433","icon":"icon-module","name":"缓存监控","path":"cache","component":"/Monitor/Cache/index","sort":4,"children":[]}]},{"id":"1929928379667386370","icon":"icon-setting","name":"组件示例","path":"/example","component":"layout","layout":"layout","sort":2,"children":[{"id":"1929929620816441347","pid":"1929928379667386370","icon":"icon-module","name":"列表示例","path":"table","component":"/Example/Table/index","sort":1,"children":[]},{"id":"1929929620816441348","pid":"1929928379667386370","icon":"icon-module","name":"表单示例","path":"form","component":"/Example/Form/index","sort":2,"children":[]},{"id":"1929929620816441346","pid":"1929928379667386370","icon":"icon-module","name":"图表示例","path":"echarts","component":"/Example/Echarts/index","sort":3,"children":[]},{"id":"1943557799479791617","pid":"1929928379667386370","icon":"icon-module","name":"Markdown","path":"markdown","component":"/Example/Markdown/index","sort":4,"children":[]}]}]', 5, 1927290201865945090, '2025-07-23 17:28:32.997704', 1927290201865945090, '2025-07-23 17:28:32.997704', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947952001525469186, '获取权限树列表', 200, '127.0.0.1', 'GET', '/api/permission/authority/tree', NULL, '[{"id":1943513441539891202,"name":"字典权限","code":"MENU:*","children":[{"id":1943513442269700097,"pid":1943513441539891202,"name":"字典新增","code":"MENU:INSERT"},{"id":1943513442269700098,"pid":1943513441539891202,"name":"字典修改","code":"MENU:UPDATE"},{"id":1943513442269700099,"pid":1943513441539891202,"name":"字典删除","code":"MENU:DELETE"}]}]', 5, 1927290201865945090, '2025-07-23 17:28:43.034261', 1927290201865945090, '2025-07-23 17:28:43.034261', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947952004859940866, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/role/list', NULL, '[{"id":"1932682189593350146","name":"运维管理员","state":true,"scope":2,"remark":"运维人员使用,全局范围,拥有所有权限"},{"id":"1932685785802162178","name":"系统管理员","state":true,"scope":0,"remark":"系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"}]', 5, 1927290201865945090, '2025-07-23 17:28:43.814596', 1927290201865945090, '2025-07-23 17:28:43.814596', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951962212257793, '获取所有字典类型的树形列表', 200, '127.0.0.1', 'GET', '/api/dict/group/tree', NULL, '[{"id":"1942142921347129346","name":"系统配置","code":"sys","state":0,"builtin":true,"remark":"系统配置相关的字典组.基本为内置不可修改的字典组","children":[{"id":"1942142182856024066","pid":"1942142921347129346","name":"权限范围","code":"sys_power_scope","state":0,"builtin":true,"remark":"系统权限范围","children":[]},{"id":"1942142790749085698","pid":"1942142921347129346","name":"用户状态","code":"sys_user_state","state":0,"builtin":true,"remark":"用户状态","children":[]},{"id":"1942143605777850369","pid":"1942142921347129346","name":"通用状态","code":"sys_common_state","state":0,"builtin":true,"remark":"通用状态","children":[]},{"id":"1942151722745933825","pid":"1942142921347129346","name":"账号类型","code":"sys_account_type","state":0,"builtin":true,"remark":"账号类型，一个用户可以有多个不同账号类型的账号，对应不同登录方式","children":[]},{"id":"1944677668501757953","pid":"1942142921347129346","name":"组织机构类型","code":"sys_organization_type","state":0,"builtin":true,"children":[]}]},{"id":"1942150470595194881","name":"业务配置","code":"business","state":0,"builtin":false,"remark":"业务部分需要的配置","children":[{"id":"1942429096108576770","pid":"1942150470595194881","name":"业务测试1","code":"business_test_1","state":0,"builtin":false,"remark":"","children":[]}]}]', 6, 1927290201865945090, '2025-07-23 17:28:33.64618', 1927290201865945090, '2025-07-23 17:28:33.64618', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951967706796033, '获取所有字典类型的树形列表', 200, '127.0.0.1', 'GET', '/api/dict/group/tree', NULL, '[{"id":"1942142921347129346","name":"系统配置","code":"sys","state":0,"builtin":true,"remark":"系统配置相关的字典组.基本为内置不可修改的字典组","children":[{"id":"1942142182856024066","pid":"1942142921347129346","name":"权限范围","code":"sys_power_scope","state":0,"builtin":true,"remark":"系统权限范围","children":[]},{"id":"1942142790749085698","pid":"1942142921347129346","name":"用户状态","code":"sys_user_state","state":0,"builtin":true,"remark":"用户状态","children":[]},{"id":"1942143605777850369","pid":"1942142921347129346","name":"通用状态","code":"sys_common_state","state":0,"builtin":true,"remark":"通用状态","children":[]},{"id":"1942151722745933825","pid":"1942142921347129346","name":"账号类型","code":"sys_account_type","state":0,"builtin":true,"remark":"账号类型，一个用户可以有多个不同账号类型的账号，对应不同登录方式","children":[]},{"id":"1944677668501757953","pid":"1942142921347129346","name":"组织机构类型","code":"sys_organization_type","state":0,"builtin":true,"children":[]}]},{"id":"1942150470595194881","name":"业务配置","code":"business","state":0,"builtin":false,"remark":"业务部分需要的配置","children":[{"id":"1942429096108576770","pid":"1942150470595194881","name":"业务测试1","code":"business_test_1","state":0,"builtin":false,"remark":"","children":[]}]}]', 6, 1927290201865945090, '2025-07-23 17:28:34.964331', 1927290201865945090, '2025-07-23 17:28:34.964331', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951970567311361, '根据类型编码获取字典数据', 200, '127.0.0.1', 'GET', '/api/dict/data/sys_account_type', NULL, '[{"id":"1942151875150163969","gid":"1942151722745933825","label":"邮箱登录","value":"0","sort":999,"state":0,"remark":"邮箱登录"},{"id":"1942151904090861570","gid":"1942151722745933825","label":"手机号码","value":"1","sort":999,"state":0,"remark":"手机号码"},{"id":"1942151942581989377","gid":"1942151722745933825","label":"微信登录","value":"2","sort":999,"state":0,"remark":"微信登录"}]', 17, 1927290201865945090, '2025-07-23 17:28:35.650784', 1927290201865945090, '2025-07-23 17:28:35.650784', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951970693140482, '根据类型编码获取字典数据', 200, '127.0.0.1', 'GET', '/api/dict/data/sys_common_state', NULL, '[{"id":"1942143658663829506","gid":"1942143605777850369","label":"启用","value":"0","sort":999,"state":0,"remark":"启用"},{"id":"1942143694822924289","gid":"1942143605777850369","label":"禁用","value":"1","sort":999,"state":0,"remark":"禁用"}]', 9, 1927290201865945090, '2025-07-23 17:28:35.680784', 1927290201865945090, '2025-07-23 17:28:35.680784', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951973838868481, '根据类型编码获取字典数据', 200, '127.0.0.1', 'GET', '/api/dict/data/sys_common_state', NULL, '[{"id":"1942143658663829506","gid":"1942143605777850369","label":"启用","value":"0","sort":999,"state":0,"remark":"启用"},{"id":"1942143694822924289","gid":"1942143605777850369","label":"禁用","value":"1","sort":999,"state":0,"remark":"禁用"}]', 16, 1927290201865945090, '2025-07-23 17:28:36.431336', 1927290201865945090, '2025-07-23 17:28:36.431336', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951975839551490, '根据类型编码获取字典数据', 200, '127.0.0.1', 'GET', '/api/dict/data/sys_user_state', NULL, '[{"id":"1942143228055609345","gid":"1942142790749085698","label":"正常","value":"0","sort":999,"state":0,"remark":"正常"},{"id":"1942143271655399425","gid":"1942142790749085698","label":"冻结","value":"1","sort":999,"state":0,"remark":"冻结"},{"id":"1942143325908721665","gid":"1942142790749085698","label":"封禁","value":"2","sort":999,"state":0,"remark":"封禁"}]', 8, 1927290201865945090, '2025-07-23 17:28:36.897974', 1927290201865945090, '2025-07-23 17:28:36.897974', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951977873788929, '根据类型编码获取字典数据', 200, '127.0.0.1', 'GET', '/api/dict/data/sys_power_scope', NULL, '[{"id":"1942142254075305986","gid":"1942142182856024066","label":"全局","value":"0","sort":999,"state":0,"remark":"全局范围可查询"},{"id":"1942142354801516546","gid":"1942142182856024066","label":"本级","value":"1","sort":999,"state":0,"remark":"可以查询当前所属的组织机构+部门本级别的内容"},{"id":"1942142489367371777","gid":"1942142182856024066","label":"本级包含下级","value":"2","sort":999,"state":0,"remark":"可以查询当前组织机构+部门,包含子级组织机构+部门的数据"}]', 9, 1927290201865945090, '2025-07-23 17:28:37.394105', 1927290201865945090, '2025-07-23 17:28:37.394105', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951980746887170, '根据类型编码获取字典数据', 200, '127.0.0.1', 'GET', '/api/dict/data/business', NULL, '[]', 9, 1927290201865945090, '2025-07-23 17:28:38.067022', 1927290201865945090, '2025-07-23 17:28:38.067022', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947951988846088194, '根据类型编码获取字典数据', 200, '127.0.0.1', 'GET', '/api/dict/data/business_test_1', NULL, '[{"id":"1942506181581787138","gid":"1942429096108576770","label":"业务测试_V_1","value":"yw_test_v_1","sort":1,"state":0},{"id":"1942506793794981889","gid":"1942429096108576770","label":"业务测试_V_2","value":"yw_test_v_2","sort":1,"state":1}]', 9, 1927290201865945090, '2025-07-23 17:28:40.010586', 1927290201865945090, '2025-07-23 17:28:40.010586', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947952001525469187, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', NULL, '[{"id":"1929928379575111682","icon":"icon-setting","name":"系统管理","path":"/system","component":"layout","layout":"layout","sort":0,"children":[{"id":"1929929620715778049","pid":"1929928379575111682","icon":"icon-module","name":"用户管理","path":"user","component":"/System/User/index","sort":0,"children":[]},{"id":"1932983846772363266","pid":"1929928379575111682","icon":"icon-module","name":"部门管理","path":"dept","component":"/System/Dept/index","sort":1,"children":[]},{"id":"1929929620753526785","pid":"1929928379575111682","icon":"icon-module","name":"访问控制","path":"RBAC","component":"/System/RBAC/index","sort":2,"children":[]},{"id":"1929929620753526789","pid":"1929928379575111682","icon":"icon-module","name":"菜单管理","path":"menu","component":"/System/Menu/index","sort":3,"children":[]},{"id":"1929929620753526787","pid":"1929928379575111682","icon":"icon-module","name":"字典管理","path":"dict","component":"/System/Dict/index","sort":4,"children":[]},{"id":"1929929620753526790","pid":"1929928379575111682","icon":"icon-module","name":"文件存储","path":"storage","component":"/System/Storage/index","sort":5,"children":[]}]},{"id":"1947937225978130433","icon":"icon-setting","name":"系统监控","path":"/monitor","component":"layout","layout":"layout","sort":1,"children":[{"id":"1947943281978445825","pid":"1947937225978130433","icon":"icon-module","name":"在线用户","path":"online","component":"/Monitor/Online/index","sort":0,"children":[]},{"id":"1947937675586547713","pid":"1947937225978130433","icon":"icon-module","name":"数据监控","path":"database","component":"/Monitor/Database/index","sort":1,"children":[]},{"id":"1929929620753526788","pid":"1947937225978130433","icon":"icon-module","name":"定时任务","path":"task","component":"/Monitor/Task/index","sort":2,"children":[]},{"id":"1947944068104261634","pid":"1947937225978130433","icon":"icon-module","name":"服务监控","path":"server","component":"/Monitor/Server/index","sort":3,"children":[]},{"id":"1947944189344813057","pid":"1947937225978130433","icon":"icon-module","name":"缓存监控","path":"cache","component":"/Monitor/Cache/index","sort":4,"children":[]}]},{"id":"1929928379667386370","icon":"icon-setting","name":"组件示例","path":"/example","component":"layout","layout":"layout","sort":2,"children":[{"id":"1929929620816441347","pid":"1929928379667386370","icon":"icon-module","name":"列表示例","path":"table","component":"/Example/Table/index","sort":1,"children":[]},{"id":"1929929620816441348","pid":"1929928379667386370","icon":"icon-module","name":"表单示例","path":"form","component":"/Example/Form/index","sort":2,"children":[]},{"id":"1929929620816441346","pid":"1929928379667386370","icon":"icon-module","name":"图表示例","path":"echarts","component":"/Example/Echarts/index","sort":3,"children":[]},{"id":"1943557799479791617","pid":"1929928379667386370","icon":"icon-module","name":"Markdown","path":"markdown","component":"/Example/Markdown/index","sort":4,"children":[]}]}]', 5, 1927290201865945090, '2025-07-23 17:28:43.034261', 1927290201865945090, '2025-07-23 17:28:43.034261', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947952001596772354, '分页查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/role/page', NULL, '{"records":[{"id":"1932682189593350146","name":"运维管理员","state":true,"scope":2,"remark":"运维人员使用,全局范围,拥有所有权限"},{"id":"1932685785802162178","name":"系统管理员","state":true,"scope":0,"remark":"系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"},{"id":"1932687324356775938","name":"小组长","state":false,"scope":1,"remark":"测试禁用状态"}],"total":3,"size":100,"current":1,"pages":1}', 18, 1927290201865945090, '2025-07-23 17:28:43.045261', 1927290201865945090, '2025-07-23 17:28:43.04626', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947952005178707970, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', NULL, '{"records":[{"id":"1934276682383138817","name":"平台管理员","email":"yangxj96@gmail.com","state":0,"roles":[{"id":"1932682189593350146","name":"运维管理员","state":true,"scope":2,"remark":"运维人员使用,全局范围,拥有所有权限"}],"organization_id":"1944679712507760641","organization_name":"道一科技"},{"id":"1937354420709711873","name":"超级管理员","email":"sysadmin@1.com","state":0,"roles":[{"id":"1932685785802162178","name":"系统管理员","state":true,"scope":0,"remark":"系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"}]},{"id":"1937706586813169665","name":"测试用户","email":"ceshi@1.com","state":0,"roles":[{"id":"1932687324356775938","name":"小组长","state":false,"scope":1,"remark":"测试禁用状态"}]}],"total":3,"size":100,"current":1,"pages":1}', 56, 1927290201865945090, '2025-07-23 17:28:43.898187', 1927290201865945090, '2025-07-23 17:28:43.898187', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947952041820147713, '获取组织机构树形列表', 200, '127.0.0.1', 'GET', '/api/organization/tree', NULL, '[{"id":"1944679712507760641","name":"道一科技","code":"ORG_ROOT","type":1,"address":"中国","children":[{"id":"1944693648674168833","pid":"1944679712507760641","name":"西部大区","code":"ORG_WEST","type":2,"address":"西部大区","remark":"整个西部大区相关的"},{"id":"1944694522943242242","pid":"1944679712507760641","name":"东部大区","code":"ORG_EAST","type":2,"address":"东部大区","remark":"整个东部大区相关的"},{"id":"1944694666350690306","pid":"1944679712507760641","name":"南部大区","code":"ORG_SOUTH","type":2,"address":"南部大区","remark":"整个南部大区相关的","children":[{"id":"1944710101167226882","pid":"1944694666350690306","name":"道一科技云南分公司","code":"ORG_YUNNAN","type":3,"address":"云南省昆明市五华区XX大道XX号","remark":"道一科技云南分公司"}]},{"id":"1944694800480337921","pid":"1944679712507760641","name":"北部大区","code":"ORG_NORTH","type":2,"address":"北部大区","remark":"整个北部大区相关的"}]}]', 3, 1927290201865945090, '2025-07-23 17:28:52.626378', 1927290201865945090, '2025-07-23 17:28:52.626378', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947952072002359297, '获取权限树列表', 200, '127.0.0.1', 'GET', '/api/permission/authority/tree', NULL, '[{"id":1943513441539891202,"name":"字典权限","code":"MENU:*","children":[{"id":1943513442269700097,"pid":1943513441539891202,"name":"字典新增","code":"MENU:INSERT"},{"id":1943513442269700098,"pid":1943513441539891202,"name":"字典修改","code":"MENU:UPDATE"},{"id":1943513442269700099,"pid":1943513441539891202,"name":"字典删除","code":"MENU:DELETE"}]}]', 4, 1927290201865945090, '2025-07-23 17:28:59.841211', 1927290201865945090, '2025-07-23 17:28:59.841211', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947952072086245377, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', NULL, '[{"id":"1929928379575111682","icon":"icon-setting","name":"系统管理","path":"/system","component":"layout","layout":"layout","sort":0,"children":[{"id":"1929929620715778049","pid":"1929928379575111682","icon":"icon-module","name":"用户管理","path":"user","component":"/System/User/index","sort":0,"children":[]},{"id":"1932983846772363266","pid":"1929928379575111682","icon":"icon-module","name":"部门管理","path":"dept","component":"/System/Dept/index","sort":1,"children":[]},{"id":"1929929620753526785","pid":"1929928379575111682","icon":"icon-module","name":"访问控制","path":"RBAC","component":"/System/RBAC/index","sort":2,"children":[]},{"id":"1929929620753526789","pid":"1929928379575111682","icon":"icon-module","name":"菜单管理","path":"menu","component":"/System/Menu/index","sort":3,"children":[]},{"id":"1929929620753526787","pid":"1929928379575111682","icon":"icon-module","name":"字典管理","path":"dict","component":"/System/Dict/index","sort":4,"children":[]},{"id":"1929929620753526790","pid":"1929928379575111682","icon":"icon-module","name":"文件存储","path":"storage","component":"/System/Storage/index","sort":5,"children":[]}]},{"id":"1947937225978130433","icon":"icon-setting","name":"系统监控","path":"/monitor","component":"layout","layout":"layout","sort":1,"children":[{"id":"1947943281978445825","pid":"1947937225978130433","icon":"icon-module","name":"在线用户","path":"online","component":"/Monitor/Online/index","sort":0,"children":[]},{"id":"1947937675586547713","pid":"1947937225978130433","icon":"icon-module","name":"数据监控","path":"database","component":"/Monitor/Database/index","sort":1,"children":[]},{"id":"1929929620753526788","pid":"1947937225978130433","icon":"icon-module","name":"定时任务","path":"task","component":"/Monitor/Task/index","sort":2,"children":[]},{"id":"1947944068104261634","pid":"1947937225978130433","icon":"icon-module","name":"服务监控","path":"server","component":"/Monitor/Server/index","sort":3,"children":[]},{"id":"1947944189344813057","pid":"1947937225978130433","icon":"icon-module","name":"缓存监控","path":"cache","component":"/Monitor/Cache/index","sort":4,"children":[]}]},{"id":"1929928379667386370","icon":"icon-setting","name":"组件示例","path":"/example","component":"layout","layout":"layout","sort":2,"children":[{"id":"1929929620816441347","pid":"1929928379667386370","icon":"icon-module","name":"列表示例","path":"table","component":"/Example/Table/index","sort":1,"children":[]},{"id":"1929929620816441348","pid":"1929928379667386370","icon":"icon-module","name":"表单示例","path":"form","component":"/Example/Form/index","sort":2,"children":[]},{"id":"1929929620816441346","pid":"1929928379667386370","icon":"icon-module","name":"图表示例","path":"echarts","component":"/Example/Echarts/index","sort":3,"children":[]},{"id":"1943557799479791617","pid":"1929928379667386370","icon":"icon-module","name":"Markdown","path":"markdown","component":"/Example/Markdown/index","sort":4,"children":[]}]}]', 5, 1927290201865945090, '2025-07-23 17:28:59.841211', 1927290201865945090, '2025-07-23 17:28:59.841211', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947952072086245378, '分页查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/role/page', NULL, '{"records":[{"id":"1932682189593350146","name":"运维管理员","state":true,"scope":2,"remark":"运维人员使用,全局范围,拥有所有权限"},{"id":"1932685785802162178","name":"系统管理员","state":true,"scope":0,"remark":"系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"},{"id":"1932687324356775938","name":"小组长","state":false,"scope":1,"remark":"测试禁用状态"}],"total":3,"size":100,"current":1,"pages":1}', 15, 1927290201865945090, '2025-07-23 17:28:59.85225', 1927290201865945090, '2025-07-23 17:28:59.85225', NULL);
INSERT INTO "db_system"."t_operation_log" VALUES (1947957829259218946, '退出登录', 200, '127.0.0.1', 'POST', '/api/auth/logout', NULL, NULL, 15, NULL, '2025-07-23 17:51:52.473514', NULL, '2025-07-23 17:51:52.473514', NULL);

-- ----------------------------
-- Table structure for t_organization
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_organization";
CREATE TABLE "db_system"."t_organization" (
  "id" int8 NOT NULL,
  "pid" int8,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int2,
  "address" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "manager_id" int8,
  "remark" varchar(255) COLLATE "pg_catalog"."default",
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
COMMENT ON COLUMN "db_system"."t_organization"."type" IS '公司类型';
COMMENT ON COLUMN "db_system"."t_organization"."address" IS '注册地址';
COMMENT ON COLUMN "db_system"."t_organization"."manager_id" IS '负责人ID';
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
INSERT INTO "db_system"."t_organization" VALUES (1944679712507760641, NULL, '道一科技', 'ORG_ROOT', 1, '中国', NULL, NULL, NULL, '2025-07-14 16:45:48.523736', NULL, '2025-07-14 16:45:48.524733', NULL);
INSERT INTO "db_system"."t_organization" VALUES (1944693648674168833, 1944679712507760641, '西部大区', 'ORG_WEST', 2, '西部大区', NULL, '整个西部大区相关的', 1927290201865945090, '2025-07-14 17:41:11.176668', 1927290201865945090, '2025-07-14 17:43:43.02278', NULL);
INSERT INTO "db_system"."t_organization" VALUES (1944694522943242242, 1944679712507760641, '东部大区', 'ORG_EAST', 2, '东部大区', NULL, '整个东部大区相关的', 1927290201865945090, '2025-07-14 17:44:39.614233', 1927290201865945090, '2025-07-14 17:44:39.615294', NULL);
INSERT INTO "db_system"."t_organization" VALUES (1944694666350690306, 1944679712507760641, '南部大区', 'ORG_SOUTH', 2, '南部大区', NULL, '整个南部大区相关的', 1927290201865945090, '2025-07-14 17:45:13.805384', 1927290201865945090, '2025-07-14 17:45:13.80638', NULL);
INSERT INTO "db_system"."t_organization" VALUES (1944694800480337921, 1944679712507760641, '北部大区', 'ORG_NORTH', 2, '北部大区', NULL, '整个北部大区相关的', 1927290201865945090, '2025-07-14 17:45:45.775249', 1927290201865945090, '2025-07-14 17:45:45.776246', NULL);
INSERT INTO "db_system"."t_organization" VALUES (1944710101167226882, 1944694666350690306, '道一科技云南分公司', 'ORG_YUNNAN', 3, '云南省昆明市五华区XX大道XX号', NULL, '道一科技云南分公司', 1927290201865945090, '2025-07-14 18:46:33.751915', 1927290201865945090, '2025-07-14 18:46:33.752948', NULL);

-- ----------------------------
-- Primary Key structure for table t_dict_data
-- ----------------------------
ALTER TABLE "db_system"."t_dict_data" ADD CONSTRAINT "t_dict_data_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_dict_group
-- ----------------------------
ALTER TABLE "db_system"."t_dict_group" ADD CONSTRAINT "t_dict_type_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_menu
-- ----------------------------
ALTER TABLE "db_system"."t_menu" ADD CONSTRAINT "t_menu_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_operation_log
-- ----------------------------
ALTER TABLE "db_system"."t_operation_log" ADD CONSTRAINT "t_operation_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table t_organization
-- ----------------------------
CREATE UNIQUE INDEX "idx_t_organization_code_unique" ON "db_system"."t_organization" USING btree (
  "code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE UNIQUE INDEX "idx_t_organization_name_unique" ON "db_system"."t_organization" USING btree (
  "name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table t_organization
-- ----------------------------
ALTER TABLE "db_system"."t_organization" ADD CONSTRAINT "t_organization_pkey" PRIMARY KEY ("id");
