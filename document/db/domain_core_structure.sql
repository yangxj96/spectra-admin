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

 Date: 21/01/2026 14:53:59
*/


-- ----------------------------
-- Table structure for sys_account
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_account";
CREATE TABLE "domain_core"."sys_account"
(
    "id"         uuid           NOT NULL,
    "user_id"    uuid           NOT NULL,
    "type"       int4           NOT NULL,
    "login_name" VARCHAR(100) COLLATE "pg_catalog"."default",
    "password"   VARCHAR(255) COLLATE "pg_catalog"."default",
    "phone"      VARCHAR(20) COLLATE "pg_catalog"."default",
    "email"      VARCHAR(100) COLLATE "pg_catalog"."default",
    "openid"     VARCHAR(100) COLLATE "pg_catalog"."default",
    "unionid"    VARCHAR(100) COLLATE "pg_catalog"."default",
    "provider"   VARCHAR(50) COLLATE "pg_catalog"."default",
    "status"     int2           NOT NULL DEFAULT 1,
    "verified"   int2           NOT NULL DEFAULT 0,
    "expires_at" timestamptz(6),
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8                    DEFAULT 0
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
-- Table structure for sys_authority
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_authority";
CREATE TABLE "domain_core"."sys_authority"
(
    "id"         uuid                                        NOT NULL,
    "pid"        uuid,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "code"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "created_by" uuid,
    "created_at" timestamptz(6)                              NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6)                              NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_config";
CREATE TABLE "domain_core"."sys_config"
(
    "id"         uuid                                        NOT NULL,
    "key"        VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "value"      VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "type"       int4                                        NOT NULL,
    "dict_code"  VARCHAR(255) COLLATE "pg_catalog"."default",
    "remarks"    VARCHAR(255) COLLATE "pg_catalog"."default",
    "created_by" uuid,
    "created_at" timestamptz(6)                              NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6)                              NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_dict_group
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_dict_group";
CREATE TABLE "domain_core"."sys_dict_group"
(
    "id"         uuid                                        NOT NULL,
    "pid"        uuid,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "code"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "state"      bool                                        NOT NULL DEFAULT TRUE,
    "remark"     TEXT COLLATE "pg_catalog"."default",
    "builtin"    bool                                        NOT NULL DEFAULT FALSE,
    "hide"       bool                                        NOT NULL DEFAULT FALSE,
    "created_by" uuid,
    "created_at" timestamptz(6)                              NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6)                              NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8                                                 DEFAULT 0
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
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_dict_item";
CREATE TABLE "domain_core"."sys_dict_item"
(
    "id"         uuid                                        NOT NULL,
    "gid"        uuid                                        NOT NULL,
    "label"      VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "value"      VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "sort"       int2                                        NOT NULL DEFAULT 0,
    "state"      int2                                        NOT NULL,
    "remark"     VARCHAR(255) COLLATE "pg_catalog"."default",
    "created_by" uuid,
    "created_at" timestamptz(6)                              NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6)                              NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8                                                 DEFAULT 0
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
-- Table structure for sys_file_chunk
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_file_chunk";
CREATE TABLE "domain_core"."sys_file_chunk"
(
    "id"           uuid                                        NOT NULL,
    "file_name"    VARCHAR(255) COLLATE "pg_catalog"."default" NOT NULL,
    "file_id"      VARCHAR(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "chunk_index"  int4                                        NOT NULL,
    "total_chunks" int4                                        NOT NULL,
    "chunk_path"   VARCHAR(500) COLLATE "pg_catalog"."default" NOT NULL,
    "chunk_size"   int8                                        NOT NULL,
    "created_by"   uuid,
    "created_at"   timestamptz(6)                              NOT NULL,
    "updated_by"   uuid,
    "updated_at"   timestamptz(6)                              NOT NULL,
    "deleted"      timestamptz(6),
    "version"      int8 DEFAULT 0
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
-- Table structure for sys_file_info
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_file_info";
CREATE TABLE "domain_core"."sys_file_info"
(
    "id"           uuid                                        NOT NULL,
    "file_name"    VARCHAR(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "origin_name"  VARCHAR(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "suffix"       VARCHAR(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "path"         VARCHAR(500) COLLATE "pg_catalog"."default" NOT NULL,
    "size"         int8                                        NOT NULL,
    "hash"         VARCHAR(64) COLLATE "pg_catalog"."default",
    "storage_type" int4                                        NOT NULL,
    "created_by"   uuid,
    "created_at"   timestamptz(6)                              NOT NULL,
    "updated_by"   uuid,
    "updated_at"   timestamptz(6)                              NOT NULL,
    "deleted"      timestamptz(6),
    "version"      int8 DEFAULT 0
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
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_log";
CREATE TABLE "domain_core"."sys_log"
(
    "id"         uuid           NOT NULL,
    "type"       int4,
    "explain"    VARCHAR(255) COLLATE "pg_catalog"."default",
    "status"     int2,
    "ip"         VARCHAR(100) COLLATE "pg_catalog"."default",
    "method"     VARCHAR(255) COLLATE "pg_catalog"."default",
    "url"        VARCHAR(255) COLLATE "pg_catalog"."default",
    "args"       bytea,
    "result"     bytea,
    "time_cost"  int8,
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_menu";
CREATE TABLE "domain_core"."sys_menu"
(
    "id"         uuid                                        NOT NULL,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "pid"        uuid,
    "icon"       VARCHAR(100) COLLATE "pg_catalog"."default",
    "path"       VARCHAR(255) COLLATE "pg_catalog"."default" NOT NULL,
    "component"  VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "layout"     VARCHAR(100) COLLATE "pg_catalog"."default",
    "sort"       int4 DEFAULT 0,
    "created_by" uuid,
    "created_at" timestamptz(6)                              NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6)                              NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_organization
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_organization";
CREATE TABLE "domain_core"."sys_organization"
(
    "id"         uuid                                        NOT NULL,
    "pid"        uuid,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "code"       VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "type"       int2,
    "path"       VARCHAR(255) COLLATE "pg_catalog"."default",
    "remark"     VARCHAR(255) COLLATE "pg_catalog"."default",
    "created_by" uuid,
    "created_at" timestamptz(6)                              NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6)                              NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_rel_role_authority
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_rel_role_authority";
CREATE TABLE "domain_core"."sys_rel_role_authority"
(
    "id"           uuid           NOT NULL,
    "role_id"      uuid           NOT NULL,
    "authority_id" uuid           NOT NULL,
    "created_by"   uuid,
    "created_at"   timestamptz(6) NOT NULL,
    "updated_by"   uuid,
    "updated_at"   timestamptz(6) NOT NULL,
    "deleted"      timestamptz(6),
    "version"      int8 DEFAULT 0
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
-- Table structure for sys_rel_role_menu
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_rel_role_menu";
CREATE TABLE "domain_core"."sys_rel_role_menu"
(
    "id"         uuid           NOT NULL,
    "role_id"    uuid           NOT NULL,
    "menu_id"    uuid           NOT NULL,
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_rel_user_role
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_rel_user_role";
CREATE TABLE "domain_core"."sys_rel_user_role"
(
    "id"         uuid           NOT NULL,
    "user_id"    uuid           NOT NULL,
    "role_id"    uuid           NOT NULL,
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_role";
CREATE TABLE "domain_core"."sys_role"
(
    "id"         uuid           NOT NULL,
    "name"       VARCHAR(100) COLLATE "pg_catalog"."default",
    "code"       VARCHAR(100) COLLATE "pg_catalog"."default",
    "state"      bool DEFAULT TRUE,
    "scope"      int4,
    "builtin"    bool DEFAULT FALSE,
    "remark"     TEXT COLLATE "pg_catalog"."default",
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_role_data_scope
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_role_data_scope";
CREATE TABLE "domain_core"."sys_role_data_scope"
(
    "id"         uuid           NOT NULL,
    "role_id"    uuid           NOT NULL,
    "scope_type" int4           NOT NULL,
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_role_data_scope_target
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_role_data_scope_target";
CREATE TABLE "domain_core"."sys_role_data_scope_target"
(
    "id"          uuid           NOT NULL,
    "role_id"     uuid           NOT NULL,
    "target_id"   uuid           NOT NULL,
    "target_type" int4           NOT NULL,
    "created_by"  uuid,
    "created_at"  timestamptz(6) NOT NULL,
    "updated_by"  uuid,
    "updated_at"  timestamptz(6) NOT NULL,
    "deleted"     timestamptz(6),
    "version"     int8 DEFAULT 0
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
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_user";
CREATE TABLE "domain_core"."sys_user"
(
    "id"              uuid                                        NOT NULL,
    "username"        VARCHAR(100) COLLATE "pg_catalog"."default" NOT NULL,
    "avatar"          VARCHAR(255) COLLATE "pg_catalog"."default",
    "status"          int2                                        NOT NULL DEFAULT 1,
    "real_name"       VARCHAR(50) COLLATE "pg_catalog"."default",
    "gender"          int4                                                 DEFAULT 0,
    "birthday"        DATE,
    "phone"           VARCHAR(20) COLLATE "pg_catalog"."default",
    "email"           VARCHAR(100) COLLATE "pg_catalog"."default",
    "country"         VARCHAR(50) COLLATE "pg_catalog"."default",
    "city"            VARCHAR(50) COLLATE "pg_catalog"."default",
    "language"        VARCHAR(10) COLLATE "pg_catalog"."default"           DEFAULT 'zh-CN'::CHARACTER VARYING,
    "timezone"        VARCHAR(40) COLLATE "pg_catalog"."default"           DEFAULT 'Asia/Shanghai'::CHARACTER VARYING,
    "organization_id" uuid                                        NOT NULL,
    "created_by"      uuid,
    "created_at"      timestamptz(6)                              NOT NULL,
    "updated_by"      uuid,
    "updated_at"      timestamptz(6)                              NOT NULL,
    "deleted"         timestamptz(6),
    "version"         int8                                                 DEFAULT 0
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
-- Table structure for sys_user_data_scope
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_user_data_scope";
CREATE TABLE "domain_core"."sys_user_data_scope"
(
    "id"         uuid           NOT NULL,
    "user_id"    uuid           NOT NULL,
    "scope_type" int4           NOT NULL,
    "created_by" uuid,
    "created_at" timestamptz(6) NOT NULL,
    "updated_by" uuid,
    "updated_at" timestamptz(6) NOT NULL,
    "deleted"    timestamptz(6),
    "version"    int8 DEFAULT 0
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
-- Table structure for sys_user_data_scope_target
-- ----------------------------
DROP TABLE IF EXISTS "domain_core"."sys_user_data_scope_target";
CREATE TABLE "domain_core"."sys_user_data_scope_target"
(
    "id"          uuid           NOT NULL,
    "user_id"     uuid           NOT NULL,
    "target_id"   uuid           NOT NULL,
    "target_type" int4           NOT NULL,
    "created_by"  uuid,
    "created_at"  timestamptz(6) NOT NULL,
    "updated_by"  uuid,
    "updated_at"  timestamptz(6) NOT NULL,
    "deleted"     timestamptz(6),
    "version"     int8 DEFAULT 0
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
-- Checks structure for table sys_account
-- ----------------------------
ALTER TABLE "domain_core"."sys_account"
    ADD CONSTRAINT "chk_account_identifier" CHECK (type = 1 AND login_name IS NOT NULL OR type = 2 AND phone IS NOT NULL OR
                                                   type = 3 AND email IS NOT NULL OR type = 4 AND openid IS NOT NULL);

-- ----------------------------
-- Primary Key structure for table sys_account
-- ----------------------------
ALTER TABLE "domain_core"."sys_account"
    ADD CONSTRAINT "sys_account_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_authority
-- ----------------------------
ALTER TABLE "domain_core"."sys_authority"
    ADD CONSTRAINT "sys_authority_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_config
-- ----------------------------
ALTER TABLE "domain_core"."sys_config"
    ADD CONSTRAINT "sys_config_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_dict_group
-- ----------------------------
ALTER TABLE "domain_core"."sys_dict_group"
    ADD CONSTRAINT "sys_dict_group_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_dict_item
-- ----------------------------
ALTER TABLE "domain_core"."sys_dict_item"
    ADD CONSTRAINT "sys_dict_item_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_file_chunk
-- ----------------------------
ALTER TABLE "domain_core"."sys_file_chunk"
    ADD CONSTRAINT "sys_file_chunk_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_file_info
-- ----------------------------
ALTER TABLE "domain_core"."sys_file_info"
    ADD CONSTRAINT "sys_file_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_log
-- ----------------------------
ALTER TABLE "domain_core"."sys_log"
    ADD CONSTRAINT "sys_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_organization
-- ----------------------------
ALTER TABLE "domain_core"."sys_organization"
    ADD CONSTRAINT "sys_organization_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_rel_role_authority
-- ----------------------------
ALTER TABLE "domain_core"."sys_rel_role_authority"
    ADD CONSTRAINT "sys_rel_role_authority_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_rel_role_menu
-- ----------------------------
ALTER TABLE "domain_core"."sys_rel_role_menu"
    ADD CONSTRAINT "sys_rel_role_menu_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_rel_user_role
-- ----------------------------
ALTER TABLE "domain_core"."sys_rel_user_role"
    ADD CONSTRAINT "sys_rel_user_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_role
-- ----------------------------
ALTER TABLE "domain_core"."sys_role"
    ADD CONSTRAINT "sys_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_role_data_scope
-- ----------------------------
ALTER TABLE "domain_core"."sys_role_data_scope"
    ADD CONSTRAINT "sys_role_data_scope_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_role_data_scope_target
-- ----------------------------
ALTER TABLE "domain_core"."sys_role_data_scope_target"
    ADD CONSTRAINT "sys_role_data_scope_target_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_user
-- ----------------------------
ALTER TABLE "domain_core"."sys_user"
    ADD CONSTRAINT "sys_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_user_data_scope
-- ----------------------------
ALTER TABLE "domain_core"."sys_user_data_scope"
    ADD CONSTRAINT "sys_user_data_scope_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_user_data_scope_target
-- ----------------------------
ALTER TABLE "domain_core"."sys_user_data_scope_target"
    ADD CONSTRAINT "sys_user_data_scope_target_pkey" PRIMARY KEY ("id");
