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

 Date: 24/06/2025 11:56:46
*/


-- ----------------------------
-- Table structure for t_account
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_account";
CREATE TABLE "db_system"."t_account"
(
    "id"         int8 NOT NULL,
    "username"   VARCHAR(20) COLLATE "pg_catalog"."default",
    "password"   VARCHAR(128) COLLATE "pg_catalog"."default",
    "user_id"    int8,
    "type"       int2,
    "created_by" int8,
    "created_at" TIMESTAMP(6),
    "updated_by" int8,
    "updated_at" TIMESTAMP(6),
    "deleted"    TIMESTAMP(6)
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
INSERT INTO "db_system"."t_account"
VALUES (1927290201865945090, 'yangxj96@gmail.com', '$2a$10$ALzuYNgOSYLlJg/XsxUY7O4BKeqECHf5J7bY8eGPaQK.3VSlkFTaO', 1934276682383138817, 0, NULL, NULL,
        1927290201865945090, '2025-06-24 11:37:14.243354', NULL);
INSERT INTO "db_system"."t_account"
VALUES (1937354421099782146, 'sysadmin@1.com', '$2a$10$zQSrfeQHvHw022UFUOoJwe5oHdOAWcaZr8d2owbbCwAgWqOSjVFVa', 1937354420709711873, 0,
        1927290201865945090, '2025-06-24 11:37:42.957695', 1927290201865945090, '2025-06-24 11:37:42.957695', NULL);

-- ----------------------------
-- Table structure for t_authority
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_authority";
CREATE TABLE "db_system"."t_authority"
(
    "id"         int8 NOT NULL,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default",
    "code"       VARCHAR(100) COLLATE "pg_catalog"."default",
    "created_by" int8,
    "created_at" TIMESTAMP(6),
    "updated_by" int8,
    "updated_at" TIMESTAMP(6),
    "deleted"    TIMESTAMP(6)
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
-- Table structure for t_dict_data
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_dict_data";
CREATE TABLE "db_system"."t_dict_data"
(
    "id"           int8                                        NOT NULL,
    "dict_type_id" int8                                        NOT NULL,
    "label"        VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "value"        VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "sort"         int2                                        NOT NULL DEFAULT 0,
    "state"        int2                                        NOT NULL,
    "remark"       VARCHAR(255) COLLATE "pg_catalog"."default",
    "created_by"   int8,
    "created_at"   TIMESTAMP(6),
    "updated_by"   int8,
    "updated_at"   TIMESTAMP(6),
    "deleted"      TIMESTAMP(6)
)
;
COMMENT ON COLUMN "db_system"."t_dict_data"."id" IS '主键ID';
COMMENT ON COLUMN "db_system"."t_dict_data"."dict_type_id" IS '字典类型ID';
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

-- ----------------------------
-- Table structure for t_dict_type
-- ----------------------------
DROP TABLE IF EXISTS "db_system".t_dict_group;
CREATE TABLE "db_system"."t_dict_type"
(
    "id"         int8                                        NOT NULL,
    "pid"        int8                                        NOT NULL DEFAULT 0,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "code"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "state"      int2                                        NOT NULL,
    "remark"     VARCHAR(255) COLLATE "pg_catalog"."default" NOT NULL,
    "builtin"    bool                                        NOT NULL DEFAULT FALSE,
    "hide"       bool                                        NOT NULL DEFAULT FALSE,
    "created_by" int8,
    "created_at" TIMESTAMP(6),
    "updated_by" int8,
    "updated_at" TIMESTAMP(6),
    "deleted"    TIMESTAMP(6)
)
;
COMMENT ON COLUMN "db_system".t_dict_group."id" IS '主键ID';
COMMENT ON COLUMN "db_system".t_dict_group."pid" IS '父级ID';
COMMENT ON COLUMN "db_system".t_dict_group."name" IS '字典名称';
COMMENT ON COLUMN "db_system".t_dict_group."code" IS '字典编码';
COMMENT ON COLUMN "db_system".t_dict_group."state" IS '字典状态';
COMMENT ON COLUMN "db_system".t_dict_group."remark" IS '备注';
COMMENT ON COLUMN "db_system".t_dict_group."builtin" IS '是否内置字段,为true则不允许他进行修改删除操作';
COMMENT ON COLUMN "db_system".t_dict_group."hide" IS '是否隐藏,为true则前端不可直接进行修改删除等操作';
COMMENT ON COLUMN "db_system".t_dict_group."created_by" IS '创建人';
COMMENT ON COLUMN "db_system".t_dict_group."created_at" IS '创建时间';
COMMENT ON COLUMN "db_system".t_dict_group."updated_by" IS '最后更新人';
COMMENT ON COLUMN "db_system".t_dict_group."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "db_system".t_dict_group."deleted" IS '是否删除';
COMMENT ON TABLE "db_system".t_dict_group IS '数据字典(字典类型)';

-- ----------------------------
-- Records of t_dict_type
-- ----------------------------

-- ----------------------------
-- Table structure for t_menu
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_menu";
CREATE TABLE "db_system"."t_menu"
(
    "id"         int8                                        NOT NULL,
    "pid"        int8,
    "icon"       VARCHAR(100) COLLATE "pg_catalog"."default",
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "path"       VARCHAR(255) COLLATE "pg_catalog"."default" NOT NULL,
    "component"  VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "layout"     VARCHAR(100) COLLATE "pg_catalog"."default",
    "sort"       int4 DEFAULT 0,
    "created_by" int8,
    "created_at" TIMESTAMP(6),
    "updated_by" int8,
    "updated_at" TIMESTAMP(6),
    "deleted"    TIMESTAMP(6)
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
INSERT INTO "db_system"."t_menu"
VALUES (1929929620816441347, 1929928379667386370, 'icon-module', '列表示例', 'table', '/Example/Table/index', NULL, 1, 1927290201865945090,
        '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-11 09:47:10.002749', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929929620816441348, 1929928379667386370, 'icon-module', '表单示例', 'form', '/Example/Form/index', NULL, 2, 1927290201865945090,
        '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-11 09:47:13.831965', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929929620816441346, 1929928379667386370, 'icon-module', '图表示例', 'echarts', '/Example/Echarts/index', NULL, 3, 1927290201865945090,
        '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-11 09:47:18.286463', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929929620753526785, 1929928379575111682, 'icon-module', '访问控制', 'RBAC', '/System/RBAC/index', NULL, 2, 1927290201865945090,
        '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:38.885015', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929929620753526789, 1929928379575111682, 'icon-module', '菜单管理', 'menu', '/System/Menu/index', NULL, 3, 1927290201865945090,
        '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:42.708315', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929929620753526787, 1929928379575111682, 'icon-module', '字典管理', 'dict', '/System/Dict/index', NULL, 4, 1927290201865945090,
        '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:45.588571', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929929620753526790, 1929928379575111682, 'icon-module', '文件存储', 'storage', '/System/Storage/index', NULL, 5, 1927290201865945090,
        '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:48.846378', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929929620753526788, 1929928379575111682, 'icon-module', '定时任务', 'task', '/System/Task/index', NULL, 6, 1927290201865945090,
        '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-12 10:03:52.30893', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1932983846772363266, 1929928379575111682, 'icon-module', '部门管理', 'dept', '/System/Dept/index', NULL, 1, 1927290201865945090,
        '2025-06-12 10:10:36.840451', 1927290201865945090, '2025-06-12 10:10:36.840451', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929929620715778049, 1929928379575111682, 'icon-module', '用户管理', 'user', '/System/User/index', NULL, 0, 1927290201865945090,
        '2025-06-05 11:37:45.58176', 1927290201865945090, '2025-06-13 15:32:51.681846', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929928379667386370, 0, 'icon-setting', '组件示例', '/example', 'layout', 'layout', 1, 1927290201865945090, '2025-06-05 11:37:45.58176',
        1927290201865945090, '2025-06-05 11:37:45.582763', NULL);
INSERT INTO "db_system"."t_menu"
VALUES (1929928379575111682, 0, 'icon-setting', '系统管理', '/system', 'layout', 'layout', 0, 1927290201865945090, '2025-06-05 11:37:45.58176',
        1927290201865945090, '2025-06-05 11:37:45.582763', NULL);

-- ----------------------------
-- Table structure for t_operation_log
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_operation_log";
CREATE TABLE "db_system"."t_operation_log"
(
    "id"         int8 NOT NULL,
    "explain"    TEXT COLLATE "pg_catalog"."default",
    "status"     int2,
    "ip"         VARCHAR(15) COLLATE "pg_catalog"."default",
    "method"     VARCHAR(255) COLLATE "pg_catalog"."default",
    "url"        VARCHAR(255) COLLATE "pg_catalog"."default",
    "args"       json,
    "result"     json,
    "time_cost"  int8,
    "created_by" int8,
    "created_at" TIMESTAMP(6),
    "updated_by" int8,
    "updated_at" TIMESTAMP(6),
    "deleted"    TIMESTAMP(6)
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
-- Records of t_operation_log
-- ----------------------------
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353907444342785, '登录', 200, '127.0.0.1', 'POST', '/api/auth/login', '{
  "params": {
    "username": "yangxj96@gmail.com",
    "password": "sysadmin",
    "code": "1234"
  }
}', NULL, 155, 0, '2025-06-24 11:35:40.488691', 0, '2025-06-24 11:35:40.488691', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353930097778689, '登录', 200, '127.0.0.1', 'POST', '/api/auth/login', '{
  "params": {
    "username": "sysadmin",
    "password": "sysadmin",
    "code": "1234"
  }
}', '{
  "id": "1927290201865945090",
  "username": "sysadmin",
  "access_token": "a95711c9-c482-49f6-9121-e25d657cf301",
  "authorities": [],
  "roles": [
    "DEV_ADMIN"
  ]
}', 155, 1927290201865945090, '2025-06-24 11:35:45.898919', 1927290201865945090, '2025-06-24 11:35:45.898919', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353932379480066, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', '{}', '[
  {
    "id": "1929928379575111682",
    "pid": "0",
    "icon": "icon-setting",
    "name": "系统管理",
    "path": "/system",
    "component": "layout",
    "layout": "layout",
    "sort": 0,
    "children": [
      {
        "id": "1929929620715778049",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "用户管理",
        "path": "user",
        "component": "/System/User/index",
        "sort": 0,
        "children": []
      },
      {
        "id": "1932983846772363266",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "部门管理",
        "path": "dept",
        "component": "/System/Dept/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620753526785",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "访问控制",
        "path": "RBAC",
        "component": "/System/RBAC/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620753526789",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "菜单管理",
        "path": "menu",
        "component": "/System/Menu/index",
        "sort": 3,
        "children": []
      },
      {
        "id": "1929929620753526787",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "字典管理",
        "path": "dict",
        "component": "/System/Dict/index",
        "sort": 4,
        "children": []
      },
      {
        "id": "1929929620753526790",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "文件存储",
        "path": "storage",
        "component": "/System/Storage/index",
        "sort": 5,
        "children": []
      },
      {
        "id": "1929929620753526788",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "定时任务",
        "path": "task",
        "component": "/System/Task/index",
        "sort": 6,
        "children": []
      }
    ]
  },
  {
    "id": "1929928379667386370",
    "pid": "0",
    "icon": "icon-setting",
    "name": "组件示例",
    "path": "/example",
    "component": "layout",
    "layout": "layout",
    "sort": 1,
    "children": [
      {
        "id": "1929929620816441347",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "列表示例",
        "path": "table",
        "component": "/Example/Table/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620816441348",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "表单示例",
        "path": "form",
        "component": "/Example/Form/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620816441346",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "图表示例",
        "path": "echarts",
        "component": "/Example/Echarts/index",
        "sort": 3,
        "children": []
      }
    ]
  }
]', 9, 1927290201865945090, '2025-06-24 11:35:46.435213', 1927290201865945090, '2025-06-24 11:35:46.435213', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353948875677697, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 26, 1927290201865945090, '2025-06-24 11:35:50.376117', 1927290201865945090, '2025-06-24 11:35:50.376117', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353949911670786, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "sysadmin@pt.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 180, 1927290201865945090, '2025-06-24 11:35:50.613724', 1927290201865945090, '2025-06-24 11:35:50.613724', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353957960540161, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 9, 1927290201865945090, '2025-06-24 11:35:52.533013', 1927290201865945090, '2025-06-24 11:35:52.533013', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353958065397762, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "sysadmin@pt.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 15, 1927290201865945090, '2025-06-24 11:35:52.558014', 1927290201865945090, '2025-06-24 11:35:52.558014', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353973374607361, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', '{}', '[
  {
    "id": "1929928379575111682",
    "pid": "0",
    "icon": "icon-setting",
    "name": "系统管理",
    "path": "/system",
    "component": "layout",
    "layout": "layout",
    "sort": 0,
    "children": [
      {
        "id": "1929929620715778049",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "用户管理",
        "path": "user",
        "component": "/System/User/index",
        "sort": 0,
        "children": []
      },
      {
        "id": "1932983846772363266",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "部门管理",
        "path": "dept",
        "component": "/System/Dept/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620753526785",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "访问控制",
        "path": "RBAC",
        "component": "/System/RBAC/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620753526789",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "菜单管理",
        "path": "menu",
        "component": "/System/Menu/index",
        "sort": 3,
        "children": []
      },
      {
        "id": "1929929620753526787",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "字典管理",
        "path": "dict",
        "component": "/System/Dict/index",
        "sort": 4,
        "children": []
      },
      {
        "id": "1929929620753526790",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "文件存储",
        "path": "storage",
        "component": "/System/Storage/index",
        "sort": 5,
        "children": []
      },
      {
        "id": "1929929620753526788",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "定时任务",
        "path": "task",
        "component": "/System/Task/index",
        "sort": 6,
        "children": []
      }
    ]
  },
  {
    "id": "1929928379667386370",
    "pid": "0",
    "icon": "icon-setting",
    "name": "组件示例",
    "path": "/example",
    "component": "layout",
    "layout": "layout",
    "sort": 1,
    "children": [
      {
        "id": "1929929620816441347",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "列表示例",
        "path": "table",
        "component": "/Example/Table/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620816441348",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "表单示例",
        "path": "form",
        "component": "/Example/Form/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620816441346",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "图表示例",
        "path": "echarts",
        "component": "/Example/Echarts/index",
        "sort": 3,
        "children": []
      }
    ]
  }
]', 11, 1927290201865945090, '2025-06-24 11:35:56.217866', 1927290201865945090, '2025-06-24 11:35:56.217866', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353973634654209, '分页查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/pageRole', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1932682189593350146",
      "name": "运维管理员",
      "state": true,
      "scope": "本级包含下级",
      "remark": "运维人员使用,全局范围,拥有所有权限"
    },
    {
      "id": "1932685785802162178",
      "name": "系统管理员",
      "state": true,
      "scope": "全局",
      "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
    },
    {
      "id": "1932687324356775938",
      "name": "小组长",
      "state": false,
      "scope": "本级",
      "remark": "测试禁用状态"
    }
  ],
  "total": 3,
  "size": 100,
  "current": 1,
  "pages": 1
}', 37, 1927290201865945090, '2025-06-24 11:35:56.272134', 1927290201865945090, '2025-06-24 11:35:56.272134', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353980639141889, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', '{}', '[
  {
    "id": "1929928379575111682",
    "pid": "0",
    "icon": "icon-setting",
    "name": "系统管理",
    "path": "/system",
    "component": "layout",
    "layout": "layout",
    "sort": 0,
    "children": [
      {
        "id": "1929929620715778049",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "用户管理",
        "path": "user",
        "component": "/System/User/index",
        "sort": 0,
        "children": []
      },
      {
        "id": "1932983846772363266",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "部门管理",
        "path": "dept",
        "component": "/System/Dept/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620753526785",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "访问控制",
        "path": "RBAC",
        "component": "/System/RBAC/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620753526789",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "菜单管理",
        "path": "menu",
        "component": "/System/Menu/index",
        "sort": 3,
        "children": []
      },
      {
        "id": "1929929620753526787",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "字典管理",
        "path": "dict",
        "component": "/System/Dict/index",
        "sort": 4,
        "children": []
      },
      {
        "id": "1929929620753526790",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "文件存储",
        "path": "storage",
        "component": "/System/Storage/index",
        "sort": 5,
        "children": []
      },
      {
        "id": "1929929620753526788",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "定时任务",
        "path": "task",
        "component": "/System/Task/index",
        "sort": 6,
        "children": []
      }
    ]
  },
  {
    "id": "1929928379667386370",
    "pid": "0",
    "icon": "icon-setting",
    "name": "组件示例",
    "path": "/example",
    "component": "layout",
    "layout": "layout",
    "sort": 1,
    "children": [
      {
        "id": "1929929620816441347",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "列表示例",
        "path": "table",
        "component": "/Example/Table/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620816441348",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "表单示例",
        "path": "form",
        "component": "/Example/Form/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620816441346",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "图表示例",
        "path": "echarts",
        "component": "/Example/Echarts/index",
        "sort": 3,
        "children": []
      }
    ]
  }
]', 5, 1927290201865945090, '2025-06-24 11:35:57.944492', 1927290201865945090, '2025-06-24 11:35:57.944492', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353980702056450, '分页查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/pageRole', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1932682189593350146",
      "name": "运维管理员",
      "state": true,
      "scope": "本级包含下级",
      "remark": "运维人员使用,全局范围,拥有所有权限"
    },
    {
      "id": "1932685785802162178",
      "name": "系统管理员",
      "state": true,
      "scope": "全局",
      "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
    },
    {
      "id": "1932687324356775938",
      "name": "小组长",
      "state": false,
      "scope": "本级",
      "remark": "测试禁用状态"
    }
  ],
  "total": 3,
  "size": 100,
  "current": 1,
  "pages": 1
}', 19, 1927290201865945090, '2025-06-24 11:35:57.953491', 1927290201865945090, '2025-06-24 11:35:57.953491', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937353998687232002, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', '{}', '[
  {
    "id": "1929928379575111682",
    "pid": "0",
    "icon": "icon-setting",
    "name": "系统管理",
    "path": "/system",
    "component": "layout",
    "layout": "layout",
    "sort": 0,
    "children": [
      {
        "id": "1929929620715778049",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "用户管理",
        "path": "user",
        "component": "/System/User/index",
        "sort": 0,
        "children": []
      },
      {
        "id": "1932983846772363266",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "部门管理",
        "path": "dept",
        "component": "/System/Dept/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620753526785",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "访问控制",
        "path": "RBAC",
        "component": "/System/RBAC/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620753526789",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "菜单管理",
        "path": "menu",
        "component": "/System/Menu/index",
        "sort": 3,
        "children": []
      },
      {
        "id": "1929929620753526787",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "字典管理",
        "path": "dict",
        "component": "/System/Dict/index",
        "sort": 4,
        "children": []
      },
      {
        "id": "1929929620753526790",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "文件存储",
        "path": "storage",
        "component": "/System/Storage/index",
        "sort": 5,
        "children": []
      },
      {
        "id": "1929929620753526788",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "定时任务",
        "path": "task",
        "component": "/System/Task/index",
        "sort": 6,
        "children": []
      }
    ]
  },
  {
    "id": "1929928379667386370",
    "pid": "0",
    "icon": "icon-setting",
    "name": "组件示例",
    "path": "/example",
    "component": "layout",
    "layout": "layout",
    "sort": 1,
    "children": [
      {
        "id": "1929929620816441347",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "列表示例",
        "path": "table",
        "component": "/Example/Table/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620816441348",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "表单示例",
        "path": "form",
        "component": "/Example/Form/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620816441346",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "图表示例",
        "path": "echarts",
        "component": "/Example/Echarts/index",
        "sort": 3,
        "children": []
      }
    ]
  }
]', 6, 1927290201865945090, '2025-06-24 11:36:02.251392', 1927290201865945090, '2025-06-24 11:36:02.251392', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354016101982209, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 11, 1927290201865945090, '2025-06-24 11:36:06.405439', 1927290201865945090, '2025-06-24 11:36:06.405439', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354016240394241, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "sysadmin@pt.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 16, 1927290201865945090, '2025-06-24 11:36:06.426437', 1927290201865945090, '2025-06-24 11:36:06.426437', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354022041116674, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 10, 1927290201865945090, '2025-06-24 11:36:07.814342', 1927290201865945090, '2025-06-24 11:36:07.814342', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354029897048065, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "sysadmin@pt.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 13, 1927290201865945090, '2025-06-24 11:36:09.691087', 1927290201865945090, '2025-06-24 11:36:09.691087', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354128609992706, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "sysadmin@pt.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 16, 1927290201865945090, '2025-06-24 11:36:33.216403', 1927290201865945090, '2025-06-24 11:36:33.216403', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354146154766338, '根据ID更新用户信息', 200, '127.0.0.1', 'PUT', '/api/user', '{
  "params": {
    "id": 1934276682383138817,
    "name": "平台管理员",
    "email": "sysadmin@pt.com",
    "state": "正常",
    "role_ids": [
      1932682189593350146
    ]
  }
}', NULL, 41, 1927290201865945090, '2025-06-24 11:36:37.413201', 1927290201865945090, '2025-06-24 11:36:37.413201', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354157835902978, '退出登录', 200, '127.0.0.1', 'POST', '/api/auth/logout', '{}', NULL, 8, 1927290201865945090,
        '2025-06-24 11:36:40.195367', 1927290201865945090, '2025-06-24 11:36:40.195367', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354167252115457, '登录', 200, '127.0.0.1', 'POST', '/api/auth/login', '{
  "params": {
    "username": "yangxj96@gmail.com",
    "password": "sysadmin",
    "code": "1234"
  }
}', NULL, 4, 0, '2025-06-24 11:36:42.443963', 0, '2025-06-24 11:36:42.444973', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354255626100738, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', '{}', '[
  {
    "id": "1929928379575111682",
    "pid": "0",
    "icon": "icon-setting",
    "name": "系统管理",
    "path": "/system",
    "component": "layout",
    "layout": "layout",
    "sort": 0,
    "children": [
      {
        "id": "1929929620715778049",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "用户管理",
        "path": "user",
        "component": "/System/User/index",
        "sort": 0,
        "children": []
      },
      {
        "id": "1932983846772363266",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "部门管理",
        "path": "dept",
        "component": "/System/Dept/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620753526785",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "访问控制",
        "path": "RBAC",
        "component": "/System/RBAC/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620753526789",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "菜单管理",
        "path": "menu",
        "component": "/System/Menu/index",
        "sort": 3,
        "children": []
      },
      {
        "id": "1929929620753526787",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "字典管理",
        "path": "dict",
        "component": "/System/Dict/index",
        "sort": 4,
        "children": []
      },
      {
        "id": "1929929620753526790",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "文件存储",
        "path": "storage",
        "component": "/System/Storage/index",
        "sort": 5,
        "children": []
      },
      {
        "id": "1929929620753526788",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "定时任务",
        "path": "task",
        "component": "/System/Task/index",
        "sort": 6,
        "children": []
      }
    ]
  },
  {
    "id": "1929928379667386370",
    "pid": "0",
    "icon": "icon-setting",
    "name": "组件示例",
    "path": "/example",
    "component": "layout",
    "layout": "layout",
    "sort": 1,
    "children": [
      {
        "id": "1929929620816441347",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "列表示例",
        "path": "table",
        "component": "/Example/Table/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620816441348",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "表单示例",
        "path": "form",
        "component": "/Example/Form/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620816441346",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "图表示例",
        "path": "echarts",
        "component": "/Example/Echarts/index",
        "sort": 3,
        "children": []
      }
    ]
  }
]', 6, 1927290201865945090, '2025-06-24 11:37:03.514864', 1927290201865945090, '2025-06-24 11:37:03.514864', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354262517342210, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 7, 1927290201865945090, '2025-06-24 11:37:05.151407', 1927290201865945090, '2025-06-24 11:37:05.151407', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354300664537089, '根据ID更新用户信息', 200, '127.0.0.1', 'PUT', '/api/user', '{
  "params": {
    "id": 1934276682383138817,
    "name": "平台管理员",
    "email": "yangxj96@gmail.com",
    "state": "正常",
    "role_ids": [
      1932682189593350146
    ]
  }
}', NULL, 82, 1927290201865945090, '2025-06-24 11:37:14.251383', 1927290201865945090, '2025-06-24 11:37:14.251383', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354302988181506, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 12, 1927290201865945090, '2025-06-24 11:37:14.800298', 1927290201865945090, '2025-06-24 11:37:14.800298', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354326878937090, '登录', 200, '127.0.0.1', 'POST', '/api/auth/login', '{
  "params": {
    "username": "yangxj96@gmail.com",
    "password": "sysadmin",
    "code": "1234"
  }
}', '{
  "id": "1927290201865945090",
  "username": "yangxj96@gmail.com",
  "access_token": "5a3955f6-c7da-4ae4-b1bc-e4085dad5e4d",
  "authorities": [],
  "roles": [
    "DEV_ADMIN"
  ]
}', 106, 1927290201865945090, '2025-06-24 11:37:20.499791', 1927290201865945090, '2025-06-24 11:37:20.499791', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354329101918210, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', '{}', '[
  {
    "id": "1929928379575111682",
    "pid": "0",
    "icon": "icon-setting",
    "name": "系统管理",
    "path": "/system",
    "component": "layout",
    "layout": "layout",
    "sort": 0,
    "children": [
      {
        "id": "1929929620715778049",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "用户管理",
        "path": "user",
        "component": "/System/User/index",
        "sort": 0,
        "children": []
      },
      {
        "id": "1932983846772363266",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "部门管理",
        "path": "dept",
        "component": "/System/Dept/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620753526785",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "访问控制",
        "path": "RBAC",
        "component": "/System/RBAC/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620753526789",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "菜单管理",
        "path": "menu",
        "component": "/System/Menu/index",
        "sort": 3,
        "children": []
      },
      {
        "id": "1929929620753526787",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "字典管理",
        "path": "dict",
        "component": "/System/Dict/index",
        "sort": 4,
        "children": []
      },
      {
        "id": "1929929620753526790",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "文件存储",
        "path": "storage",
        "component": "/System/Storage/index",
        "sort": 5,
        "children": []
      },
      {
        "id": "1929929620753526788",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "定时任务",
        "path": "task",
        "component": "/System/Task/index",
        "sort": 6,
        "children": []
      }
    ]
  },
  {
    "id": "1929928379667386370",
    "pid": "0",
    "icon": "icon-setting",
    "name": "组件示例",
    "path": "/example",
    "component": "layout",
    "layout": "layout",
    "sort": 1,
    "children": [
      {
        "id": "1929929620816441347",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "列表示例",
        "path": "table",
        "component": "/Example/Table/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620816441348",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "表单示例",
        "path": "form",
        "component": "/Example/Form/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620816441346",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "图表示例",
        "path": "echarts",
        "component": "/Example/Echarts/index",
        "sort": 3,
        "children": []
      }
    ]
  }
]', 5, 1927290201865945090, '2025-06-24 11:37:21.031958', 1927290201865945090, '2025-06-24 11:37:21.031958', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354336664248321, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 6, 1927290201865945090, '2025-06-24 11:37:22.823727', 1927290201865945090, '2025-06-24 11:37:22.823727', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354421099782148, '创建用户', 200, '127.0.0.1', 'POST', '/api/user', '{
  "params": {
    "name": "超级管理员",
    "email": "sysadmin@1.com",
    "state": "正常",
    "role_ids": [
      1932685785802162178
    ]
  }
}', NULL, 100, 1927290201865945090, '2025-06-24 11:37:42.961701', 1927290201865945090, '2025-06-24 11:37:42.961701', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354423448592385, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1937354420709711873",
      "name": "超级管理员",
      "email": "sysadmin@1.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932685785802162178",
          "name": "系统管理员",
          "state": true,
          "scope": "全局",
          "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
        }
      ]
    },
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 2,
  "size": 100,
  "current": 1,
  "pages": 1
}', 15, 1927290201865945090, '2025-06-24 11:37:43.517632', 1927290201865945090, '2025-06-24 11:37:43.517632', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354514179776514, '登录', 200, '127.0.0.1', 'POST', '/api/auth/login', '{
  "params": {
    "username": "sysadmin@1.com",
    "password": "123456",
    "code": "1234"
  }
}', '{
  "id": "1937354421099782146",
  "username": "sysadmin@1.com",
  "access_token": "28dddbb7-d2d3-49e0-8abb-6022eb8d148e",
  "authorities": [],
  "roles": [
    "SYS_ADMIN"
  ]
}', 98, 1937354421099782146, '2025-06-24 11:38:05.146523', 1937354421099782146, '2025-06-24 11:38:05.146523', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354022108225538, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "sysadmin@pt.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 15, 1927290201865945090, '2025-06-24 11:36:07.837341', 1927290201865945090, '2025-06-24 11:36:07.837341', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354029834133506, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 7, 1927290201865945090, '2025-06-24 11:36:09.671089', 1927290201865945090, '2025-06-24 11:36:09.671089', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354128479969282, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 8, 1927290201865945090, '2025-06-24 11:36:33.196404', 1927290201865945090, '2025-06-24 11:36:33.196404', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354148507770881, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "sysadmin@pt.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 12, 1927290201865945090, '2025-06-24 11:36:37.970662', 1927290201865945090, '2025-06-24 11:36:37.970662', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354183781867522, '登录', 200, '127.0.0.1', 'POST', '/api/auth/login', '{
  "params": {
    "username": "yangxj96@pt.com",
    "password": "sysadmin",
    "code": "1234"
  }
}', NULL, 5, 0, '2025-06-24 11:36:46.371167', 0, '2025-06-24 11:36:46.371167', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354253390536705, '登录', 200, '127.0.0.1', 'POST', '/api/auth/login', '{
  "params": {
    "username": "sysadmin",
    "password": "sysadmin",
    "code": "1234"
  }
}', '{
  "id": "1927290201865945090",
  "username": "sysadmin",
  "access_token": "1c3f673f-7229-42de-8168-a6c4a7828f2b",
  "authorities": [],
  "roles": [
    "DEV_ADMIN"
  ]
}', 105, 1927290201865945090, '2025-06-24 11:37:02.978149', 1927290201865945090, '2025-06-24 11:37:02.978149', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354262651559938, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "sysadmin@pt.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 22, 1927290201865945090, '2025-06-24 11:37:05.183409', 1927290201865945090, '2025-06-24 11:37:05.183409', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354318377082881, '退出登录', 200, '127.0.0.1', 'POST', '/api/auth/logout', '{}', NULL, 10, 1927290201865945090,
        '2025-06-24 11:37:18.466108', 1927290201865945090, '2025-06-24 11:37:18.466108', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354336731357186, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 1,
  "size": 100,
  "current": 1,
  "pages": 1
}', 13, 1927290201865945090, '2025-06-24 11:37:22.841726', 1927290201865945090, '2025-06-24 11:37:22.841726', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354491014635522, '退出登录', 200, '127.0.0.1', 'POST', '/api/auth/logout', '{}', NULL, 3, 1927290201865945090,
        '2025-06-24 11:37:59.634503', 1927290201865945090, '2025-06-24 11:37:59.634503', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354536778686465, '无权操作', 403, '127.0.0.1', 'GET', '/api/user/page', NULL, '{
  "code": 403,
  "msg": "无权操作"
}', 24, 1937354421099782146, '2025-06-24 11:38:10.541715', 1937354421099782146, '2025-06-24 11:38:10.541715', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354550376620033, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 4, 1937354421099782146, '2025-06-24 11:38:13.782553', 1937354421099782146, '2025-06-24 11:38:13.782553', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354558480015362, '退出登录', 200, '127.0.0.1', 'POST', '/api/auth/logout', '{}', NULL, 6, 1937354421099782146,
        '2025-06-24 11:38:15.715631', 1937354421099782146, '2025-06-24 11:38:15.715631', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354575009767425, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1937354420709711873",
      "name": "超级管理员",
      "email": "sysadmin@1.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932685785802162178",
          "name": "系统管理员",
          "state": true,
          "scope": "全局",
          "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
        }
      ]
    },
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 2,
  "size": 100,
  "current": 1,
  "pages": 1
}', 14, 1927290201865945090, '2025-06-24 11:38:19.648927', 1927290201865945090, '2025-06-24 11:38:19.648927', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354648355561474, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 4, 1927290201865945090, '2025-06-24 11:38:37.137118', 1927290201865945090, '2025-06-24 11:38:37.137118', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354516327260162, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', '{}', '[
  {
    "id": "1929928379575111682",
    "pid": "0",
    "icon": "icon-setting",
    "name": "系统管理",
    "path": "/system",
    "component": "layout",
    "layout": "layout",
    "sort": 0,
    "children": [
      {
        "id": "1929929620715778049",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "用户管理",
        "path": "user",
        "component": "/System/User/index",
        "sort": 0,
        "children": []
      },
      {
        "id": "1932983846772363266",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "部门管理",
        "path": "dept",
        "component": "/System/Dept/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620753526785",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "访问控制",
        "path": "RBAC",
        "component": "/System/RBAC/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620753526789",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "菜单管理",
        "path": "menu",
        "component": "/System/Menu/index",
        "sort": 3,
        "children": []
      },
      {
        "id": "1929929620753526787",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "字典管理",
        "path": "dict",
        "component": "/System/Dict/index",
        "sort": 4,
        "children": []
      },
      {
        "id": "1929929620753526790",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "文件存储",
        "path": "storage",
        "component": "/System/Storage/index",
        "sort": 5,
        "children": []
      },
      {
        "id": "1929929620753526788",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "定时任务",
        "path": "task",
        "component": "/System/Task/index",
        "sort": 6,
        "children": []
      }
    ]
  },
  {
    "id": "1929928379667386370",
    "pid": "0",
    "icon": "icon-setting",
    "name": "组件示例",
    "path": "/example",
    "component": "layout",
    "layout": "layout",
    "sort": 1,
    "children": [
      {
        "id": "1929929620816441347",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "列表示例",
        "path": "table",
        "component": "/Example/Table/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620816441348",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "表单示例",
        "path": "form",
        "component": "/Example/Form/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620816441346",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "图表示例",
        "path": "echarts",
        "component": "/Example/Echarts/index",
        "sort": 3,
        "children": []
      }
    ]
  }
]', 3, 1937354421099782146, '2025-06-24 11:38:05.6682', 1937354421099782146, '2025-06-24 11:38:05.6682', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354536652857345, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 6, 1937354421099782146, '2025-06-24 11:38:10.507717', 1937354421099782146, '2025-06-24 11:38:10.507717', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354550439534594, '无权操作', 403, '127.0.0.1', 'GET', '/api/user/page', NULL, '{
  "code": 403,
  "msg": "无权操作"
}', 7, 1937354421099782146, '2025-06-24 11:38:13.793552', 1937354421099782146, '2025-06-24 11:38:13.793552', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354567594237954, '登录', 200, '127.0.0.1', 'POST', '/api/auth/login', '{
  "params": {
    "username": "yangxj96@gmail.com",
    "password": "sysadmin",
    "code": "1234"
  }
}', '{
  "id": "1927290201865945090",
  "username": "yangxj96@gmail.com",
  "access_token": "42660f89-f9ed-4f40-a176-390a7a0f8fc6",
  "authorities": [],
  "roles": [
    "DEV_ADMIN"
  ]
}', 111, 1927290201865945090, '2025-06-24 11:38:17.888769', 1927290201865945090, '2025-06-24 11:38:17.888769', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354569741721602, '获取树形菜单', 200, '127.0.0.1', 'GET', '/api/menu/tree', '{}', '[
  {
    "id": "1929928379575111682",
    "pid": "0",
    "icon": "icon-setting",
    "name": "系统管理",
    "path": "/system",
    "component": "layout",
    "layout": "layout",
    "sort": 0,
    "children": [
      {
        "id": "1929929620715778049",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "用户管理",
        "path": "user",
        "component": "/System/User/index",
        "sort": 0,
        "children": []
      },
      {
        "id": "1932983846772363266",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "部门管理",
        "path": "dept",
        "component": "/System/Dept/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620753526785",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "访问控制",
        "path": "RBAC",
        "component": "/System/RBAC/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620753526789",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "菜单管理",
        "path": "menu",
        "component": "/System/Menu/index",
        "sort": 3,
        "children": []
      },
      {
        "id": "1929929620753526787",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "字典管理",
        "path": "dict",
        "component": "/System/Dict/index",
        "sort": 4,
        "children": []
      },
      {
        "id": "1929929620753526790",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "文件存储",
        "path": "storage",
        "component": "/System/Storage/index",
        "sort": 5,
        "children": []
      },
      {
        "id": "1929929620753526788",
        "pid": "1929928379575111682",
        "icon": "icon-module",
        "name": "定时任务",
        "path": "task",
        "component": "/System/Task/index",
        "sort": 6,
        "children": []
      }
    ]
  },
  {
    "id": "1929928379667386370",
    "pid": "0",
    "icon": "icon-setting",
    "name": "组件示例",
    "path": "/example",
    "component": "layout",
    "layout": "layout",
    "sort": 1,
    "children": [
      {
        "id": "1929929620816441347",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "列表示例",
        "path": "table",
        "component": "/Example/Table/index",
        "sort": 1,
        "children": []
      },
      {
        "id": "1929929620816441348",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "表单示例",
        "path": "form",
        "component": "/Example/Form/index",
        "sort": 2,
        "children": []
      },
      {
        "id": "1929929620816441346",
        "pid": "1929928379667386370",
        "icon": "icon-module",
        "name": "图表示例",
        "path": "echarts",
        "component": "/Example/Echarts/index",
        "sort": 3,
        "children": []
      }
    ]
  }
]', 3, 1927290201865945090, '2025-06-24 11:38:18.402342', 1927290201865945090, '2025-06-24 11:38:18.402342', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354574879744001, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 4, 1927290201865945090, '2025-06-24 11:38:19.62993', 1927290201865945090, '2025-06-24 11:38:19.62993', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354648435253250, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1937354420709711873",
      "name": "超级管理员",
      "email": "sysadmin@1.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932685785802162178",
          "name": "系统管理员",
          "state": true,
          "scope": "全局",
          "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
        }
      ]
    },
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 2,
  "size": 100,
  "current": 1,
  "pages": 1
}', 23, 1927290201865945090, '2025-06-24 11:38:37.165119', 1927290201865945090, '2025-06-24 11:38:37.165119', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354988941434881, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 5, 1927290201865945090, '2025-06-24 11:39:58.345297', 1927290201865945090, '2025-06-24 11:39:58.345297', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937354989008543745, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1937354420709711873",
      "name": "超级管理员",
      "email": "sysadmin@1.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932685785802162178",
          "name": "系统管理员",
          "state": true,
          "scope": "全局",
          "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
        }
      ]
    },
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 2,
  "size": 100,
  "current": 1,
  "pages": 1
}', 11, 1927290201865945090, '2025-06-24 11:39:58.362293', 1927290201865945090, '2025-06-24 11:39:58.362293', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355013838823425, '根据ID更新用户信息', 200, '127.0.0.1', 'PUT', '/api/user', '{
  "params": {
    "id": 1937354420709711873,
    "name": "超级管理员",
    "email": "sysadmin@1.com",
    "state": "正常",
    "role_ids": [
      1932685785802162178
    ]
  }
}', NULL, 12, 1927290201865945090, '2025-06-24 11:40:04.27176', 1927290201865945090, '2025-06-24 11:40:04.27176', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355016074387458, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1937354420709711873",
      "name": "超级管理员",
      "email": "sysadmin@1.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932685785802162178",
          "name": "系统管理员",
          "state": true,
          "scope": "全局",
          "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
        }
      ]
    },
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 2,
  "size": 100,
  "current": 1,
  "pages": 1
}', 11, 1927290201865945090, '2025-06-24 11:40:04.810155', 1927290201865945090, '2025-06-24 11:40:04.810155', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355323269406721, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 3, 1927290201865945090, '2025-06-24 11:41:18.060816', 1927290201865945090, '2025-06-24 11:41:18.060816', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355323399430145, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1937354420709711873",
      "name": "超级管理员",
      "email": "sysadmin@1.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932685785802162178",
          "name": "系统管理员",
          "state": true,
          "scope": "全局",
          "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
        }
      ]
    },
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 2,
  "size": 100,
  "current": 1,
  "pages": 1
}', 17, 1927290201865945090, '2025-06-24 11:41:18.081817', 1927290201865945090, '2025-06-24 11:41:18.082814', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355370778288129, '查询角色列表', 200, '127.0.0.1', 'GET', '/api/permission/listRole', '{}', '[
  {
    "id": "1932687324356775938",
    "name": "小组长",
    "state": false,
    "scope": "本级",
    "remark": "测试禁用状态"
  },
  {
    "id": "1932685785802162178",
    "name": "系统管理员",
    "state": true,
    "scope": "全局",
    "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
  },
  {
    "id": "1932682189593350146",
    "name": "运维管理员",
    "state": true,
    "scope": "本级包含下级",
    "remark": "运维人员使用,全局范围,拥有所有权限"
  }
]', 4, 1927290201865945090, '2025-06-24 11:41:29.382001', 1927290201865945090, '2025-06-24 11:41:29.382001', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355384543993858, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1937354420709711873",
      "name": "超级管理员",
      "email": "sysadmin@1.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932685785802162178",
          "name": "系统管理员",
          "state": true,
          "scope": "全局",
          "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
        }
      ]
    },
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 2,
  "size": 100,
  "current": 1,
  "pages": 1
}', 11, 1927290201865945090, '2025-06-24 11:41:32.657028', 1927290201865945090, '2025-06-24 11:41:32.657028', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355391942746113, '根据ID更新用户信息', 200, '127.0.0.1', 'PUT', '/api/user', '{
  "params": {
    "id": 1937354420709711873,
    "name": "超级管理员",
    "email": "sysadmin@1.com",
    "state": "正常",
    "role_ids": [
      1932685785802162178
    ]
  }
}', NULL, 8, 1927290201865945090, '2025-06-24 11:41:34.427014', 1927290201865945090, '2025-06-24 11:41:34.427014', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355394237030401, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1937354420709711873",
      "name": "超级管理员",
      "email": "sysadmin@1.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932685785802162178",
          "name": "系统管理员",
          "state": true,
          "scope": "全局",
          "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
        }
      ]
    },
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 2,
  "size": 100,
  "current": 1,
  "pages": 1
}', 9, 1927290201865945090, '2025-06-24 11:41:34.977818', 1927290201865945090, '2025-06-24 11:41:34.977818', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355463531126786, '退出登录', 200, '127.0.0.1', 'POST', '/api/auth/logout', '{}', NULL, 1, 1927290201865945090,
        '2025-06-24 11:41:51.500175', 1927290201865945090, '2025-06-24 11:41:51.500175', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355370845396994, '分页查询用户列表', 200, '127.0.0.1', 'GET', '/api/user/page', '{
  "page": {
    "page_size": 100,
    "page_num": 1
  },
  "params": {}
}', '{
  "records": [
    {
      "id": "1937354420709711873",
      "name": "超级管理员",
      "email": "sysadmin@1.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932685785802162178",
          "name": "系统管理员",
          "state": true,
          "scope": "全局",
          "remark": "系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容"
        }
      ]
    },
    {
      "id": "1934276682383138817",
      "name": "平台管理员",
      "email": "yangxj96@gmail.com",
      "state": "正常",
      "roles": [
        {
          "id": "1932682189593350146",
          "name": "运维管理员",
          "state": true,
          "scope": "本级包含下级",
          "remark": "运维人员使用,全局范围,拥有所有权限"
        }
      ]
    }
  ],
  "total": 2,
  "size": 100,
  "current": 1,
  "pages": 1
}', 17, 1927290201865945090, '2025-06-24 11:41:29.404001', 1927290201865945090, '2025-06-24 11:41:29.404001', NULL);
INSERT INTO "db_system"."t_operation_log"
VALUES (1937355382232932353, '根据ID更新用户信息', 200, '127.0.0.1', 'PUT', '/api/user', '{
  "params": {
    "id": 1937354420709711873,
    "name": "超级管理员",
    "email": "sysadmin@1.com",
    "state": "正常",
    "role_ids": [
      1932685785802162178
    ]
  }
}', NULL, 11, 1927290201865945090, '2025-06-24 11:41:32.108542', 1927290201865945090, '2025-06-24 11:41:32.108542', NULL);

-- ----------------------------
-- Table structure for t_organization
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_organization";
CREATE TABLE "db_system"."t_organization"
(
    "id"         int8                                        NOT NULL,
    "pid"        int8                                        NOT NULL DEFAULT 0,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "code"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "type"       int2                                        NOT NULL,
    "remark"     VARCHAR(255) COLLATE "pg_catalog"."default" NOT NULL,
    "created_by" int8,
    "created_at" TIMESTAMP(6),
    "updated_by" int8,
    "updated_at" TIMESTAMP(6),
    "deleted"    TIMESTAMP(6)
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
CREATE TABLE "db_system"."t_role"
(
    "id"         int8 NOT NULL,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default",
    "code"       VARCHAR(100) COLLATE "pg_catalog"."default",
    "state"      bool DEFAULT TRUE,
    "scope"      int2,
    "remark"     VARCHAR(255) COLLATE "pg_catalog"."default",
    "created_by" int8,
    "created_at" TIMESTAMP(6),
    "updated_by" int8,
    "updated_at" TIMESTAMP(6),
    "deleted"    TIMESTAMP(6)
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
INSERT INTO "db_system"."t_role"
VALUES (1932682189593350146, '运维管理员', 'DEV_ADMIN', 't', 2, '运维人员使用,全局范围,拥有所有权限', 1927290201865945090,
        '2025-06-11 14:11:56.208812', 1927290201865945090, '2025-06-11 14:33:59.593709', NULL);
INSERT INTO "db_system"."t_role"
VALUES (1932687324356775938, '小组长', 'GROUP_LEADER', 'f', 1, '测试禁用状态', 1927290201865945090, '2025-06-11 14:32:20.385948', 1927290201865945090,
        '2025-06-12 17:15:18.034439', NULL);
INSERT INTO "db_system"."t_role"
VALUES (1932685785802162178, '系统管理员', 'SYS_ADMIN', 't', 0, '系统管理员,管理整个系统的,但是看不到系统运维相关的一些内容', 1927290201865945090,
        '2025-06-11 14:26:13.572692', 1927290201865945090, '2025-06-11 14:26:13.572692', NULL);

-- ----------------------------
-- Table structure for t_role_authority_map
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_role_authority_map";
CREATE TABLE "db_system"."t_role_authority_map"
(
    "id"           int8 NOT NULL,
    "role_id"      int8 NOT NULL,
    "authority_id" int8 NOT NULL,
    "created_by"   int8,
    "created_at"   TIMESTAMP(6),
    "updated_by"   int8,
    "updated_at"   TIMESTAMP(6),
    "deleted"      TIMESTAMP(6)
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
CREATE TABLE "db_system"."t_user"
(
    "id"         int8 NOT NULL,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default",
    "email"      VARCHAR(100) COLLATE "pg_catalog"."default",
    "avatar"     VARCHAR(100) COLLATE "pg_catalog"."default",
    "state"      int2,
    "created_by" int8,
    "created_at" TIMESTAMP(6),
    "updated_by" int8,
    "updated_at" TIMESTAMP(6),
    "deleted"    TIMESTAMP(6)
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
INSERT INTO "db_system"."t_user"
VALUES (1934276682383138817, '平台管理员', 'yangxj96@gmail.com', NULL, 0, 0, '2025-06-15 23:47:52.86429', 1927290201865945090,
        '2025-06-24 11:37:14.216352', NULL);
INSERT INTO "db_system"."t_user"
VALUES (1937354420709711873, '超级管理员', 'sysadmin@1.com', NULL, 0, 1927290201865945090, '2025-06-24 11:37:42.866695', 1927290201865945090,
        '2025-06-24 11:41:34.424012', NULL);

-- ----------------------------
-- Table structure for t_user_role_map
-- ----------------------------
DROP TABLE IF EXISTS "db_system"."t_user_role_map";
CREATE TABLE "db_system"."t_user_role_map"
(
    "id"         int8 NOT NULL,
    "user_id"    int8 NOT NULL,
    "role_id"    int8 NOT NULL,
    "created_by" int8,
    "created_at" TIMESTAMP(6),
    "updated_by" int8,
    "updated_at" TIMESTAMP(6),
    "deleted"    TIMESTAMP(6)
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
INSERT INTO "db_system"."t_user_role_map"
VALUES (1934292480493473793, 1934276682383138817, 1932682189593350146, 1927290201865945090, '2025-06-16 00:50:39.420317', 1927290201865945090,
        '2025-06-16 00:50:39.420317', NULL);
INSERT INTO "db_system"."t_user_role_map"
VALUES (1937354421099782147, 1937354420709711873, 1932685785802162178, 0, '2025-06-24 11:37:42.867752', 0, '2025-06-24 11:37:42.867752', NULL);

-- ----------------------------
-- Uniques structure for table t_account
-- ----------------------------
ALTER TABLE "db_system"."t_account"
    ADD CONSTRAINT "t_account_username_key" UNIQUE ("username");

-- ----------------------------
-- Primary Key structure for table t_account
-- ----------------------------
ALTER TABLE "db_system"."t_account"
    ADD CONSTRAINT "t_account_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_authority
-- ----------------------------
ALTER TABLE "db_system"."t_authority"
    ADD CONSTRAINT "t_authority_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_dict_data
-- ----------------------------
ALTER TABLE "db_system"."t_dict_data"
    ADD CONSTRAINT "t_dict_data_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_dict_type
-- ----------------------------
ALTER TABLE "db_system".t_dict_group
    ADD CONSTRAINT "t_dict_type_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_menu
-- ----------------------------
ALTER TABLE "db_system"."t_menu"
    ADD CONSTRAINT "t_menu_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_operation_log
-- ----------------------------
ALTER TABLE "db_system"."t_operation_log"
    ADD CONSTRAINT "t_operation_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_organization
-- ----------------------------
ALTER TABLE "db_system"."t_organization"
    ADD CONSTRAINT "t_organization_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_role
-- ----------------------------
ALTER TABLE "db_system"."t_role"
    ADD CONSTRAINT "t_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_role_authority_map
-- ----------------------------
ALTER TABLE "db_system"."t_role_authority_map"
    ADD CONSTRAINT "t_role_authority_map_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_user
-- ----------------------------
ALTER TABLE "db_system"."t_user"
    ADD CONSTRAINT "t_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_user_role_map
-- ----------------------------
ALTER TABLE "db_system"."t_user_role_map"
    ADD CONSTRAINT "t_user_role_map_pkey" PRIMARY KEY ("id");
