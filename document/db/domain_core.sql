--
-- PostgreSQL database dump
--

\restrict Mb40bdRn1ZjjWpanZgbDbMqI3WpsfwISTfisoxRhG3MlAgtCbGhdnPMkmue4aCp

-- Dumped from database version 18.0 (Ubuntu 18.0-1.pgdg24.04+3)
-- Dumped by pg_dump version 18.0 (Ubuntu 18.0-1.pgdg24.04+3)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: domain_core; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA domain_core;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: sys_account; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_account (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    type integer NOT NULL,
    login_name character varying(100),
    password character varying(255),
    phone character varying(20),
    email character varying(100),
    openid character varying(100),
    unionid character varying(100),
    provider character varying(50),
    status smallint DEFAULT 1 NOT NULL,
    verified smallint DEFAULT 0,
    expires_at timestamp(6) with time zone,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_account; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_account IS '用户账号表';


--
-- Name: COLUMN sys_account.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.id IS '主键ID';


--
-- Name: COLUMN sys_account.user_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.user_id IS '用户ID';


--
-- Name: COLUMN sys_account.type; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.type IS '账号类型';


--
-- Name: COLUMN sys_account.login_name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.login_name IS '用户名（用于账号密码登录）';


--
-- Name: COLUMN sys_account.password; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.password IS '密码(仅用作账号密码登录)';


--
-- Name: COLUMN sys_account.phone; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.phone IS '手机号（用于短信登录）';


--
-- Name: COLUMN sys_account.email; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.email IS '邮箱（用于邮箱验证码登录）';


--
-- Name: COLUMN sys_account.openid; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.openid IS '微信 openid';


--
-- Name: COLUMN sys_account.unionid; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.unionid IS '微信 unionid（跨应用唯一）';


--
-- Name: COLUMN sys_account.provider; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.provider IS '第三方来源：WECHAT, ALIPAY, APPLE 等';


--
-- Name: COLUMN sys_account.status; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.status IS '1:正常 2:禁用 3:未验证';


--
-- Name: COLUMN sys_account.verified; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.verified IS '0:未验证 1:已验证（如手机号/邮箱）';


--
-- Name: COLUMN sys_account.expires_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.expires_at IS '用于临时账号（如扫码未确认）';


--
-- Name: COLUMN sys_account.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.created_by IS '创建人';


--
-- Name: COLUMN sys_account.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.created_at IS '创建时间';


--
-- Name: COLUMN sys_account.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_account.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_account.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.deleted IS '删除标识';


--
-- Name: COLUMN sys_account.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_account.version IS '乐观锁';


--
-- Name: sys_authority; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_authority (
    id uuid NOT NULL,
    pid uuid,
    name character varying(100) NOT NULL,
    code character varying(100) NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_authority; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_authority IS '权限表';


--
-- Name: COLUMN sys_authority.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.id IS '主键ID';


--
-- Name: COLUMN sys_authority.pid; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.pid IS '父级ID,用于构建树形结构';


--
-- Name: COLUMN sys_authority.name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.name IS '权限名称';


--
-- Name: COLUMN sys_authority.code; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.code IS '权限编码';


--
-- Name: COLUMN sys_authority.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.created_by IS '创建人';


--
-- Name: COLUMN sys_authority.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.created_at IS '创建时间';


--
-- Name: COLUMN sys_authority.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_authority.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_authority.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.deleted IS '是否删除';


--
-- Name: COLUMN sys_authority.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_authority.version IS '乐观锁';


--
-- Name: sys_config; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_config (
    id uuid NOT NULL,
    key character varying(100) NOT NULL,
    value character varying(100) NOT NULL,
    type integer NOT NULL,
    dict_code character varying(255),
    remarks character varying(255),
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_config; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_config IS '系统配置表';


--
-- Name: COLUMN sys_config.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.id IS '主键ID';


--
-- Name: COLUMN sys_config.key; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.key IS '配置key';


--
-- Name: COLUMN sys_config.value; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.value IS '配置VALUE';


--
-- Name: COLUMN sys_config.type; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.type IS '值类型';


--
-- Name: COLUMN sys_config.dict_code; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.dict_code IS '字典组CODE,可能会有选项之类的,直接关联一个字典做下拉选项';


--
-- Name: COLUMN sys_config.remarks; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.remarks IS '备注说明';


--
-- Name: COLUMN sys_config.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.created_by IS '创建人';


--
-- Name: COLUMN sys_config.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.created_at IS '创建时间';


--
-- Name: COLUMN sys_config.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_config.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_config.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.deleted IS '删除时间';


--
-- Name: COLUMN sys_config.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_config.version IS '乐观锁版本号,默认0';


--
-- Name: sys_dict_group; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_dict_group (
    id uuid NOT NULL,
    pid uuid,
    name character varying(100) NOT NULL,
    code character varying(100) NOT NULL,
    state boolean DEFAULT true NOT NULL,
    remark text,
    builtin boolean DEFAULT false NOT NULL,
    hide boolean DEFAULT false NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_dict_group; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_dict_group IS '字典组表';


--
-- Name: COLUMN sys_dict_group.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.id IS '主键ID';


--
-- Name: COLUMN sys_dict_group.pid; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.pid IS '父级ID';


--
-- Name: COLUMN sys_dict_group.name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.name IS '字典名称';


--
-- Name: COLUMN sys_dict_group.code; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.code IS '字典编码';


--
-- Name: COLUMN sys_dict_group.state; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.state IS '字典状态';


--
-- Name: COLUMN sys_dict_group.remark; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.remark IS '备注';


--
-- Name: COLUMN sys_dict_group.builtin; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.builtin IS '是否内置字段,为TRUE则不允许他进行修改删除操作';


--
-- Name: COLUMN sys_dict_group.hide; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.hide IS '是否隐藏,为TRUE则前端不可直接进行修改删除等操作';


--
-- Name: COLUMN sys_dict_group.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.created_by IS '创建人';


--
-- Name: COLUMN sys_dict_group.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.created_at IS '创建时间';


--
-- Name: COLUMN sys_dict_group.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_dict_group.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_dict_group.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.deleted IS '是否删除';


--
-- Name: COLUMN sys_dict_group.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_group.version IS '乐观锁';


--
-- Name: sys_dict_item; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_dict_item (
    id uuid NOT NULL,
    gid uuid NOT NULL,
    label character varying(100) NOT NULL,
    value character varying(100) NOT NULL,
    sort smallint DEFAULT 0 NOT NULL,
    state smallint NOT NULL,
    remark character varying(255),
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_dict_item; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_dict_item IS '字典数据表';


--
-- Name: COLUMN sys_dict_item.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.id IS '主键ID';


--
-- Name: COLUMN sys_dict_item.gid; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.gid IS '字典组ID';


--
-- Name: COLUMN sys_dict_item.label; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.label IS '标签';


--
-- Name: COLUMN sys_dict_item.value; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.value IS '值';


--
-- Name: COLUMN sys_dict_item.sort; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.sort IS '排序';


--
-- Name: COLUMN sys_dict_item.state; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.state IS '状态';


--
-- Name: COLUMN sys_dict_item.remark; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.remark IS '备注';


--
-- Name: COLUMN sys_dict_item.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.created_by IS '创建人';


--
-- Name: COLUMN sys_dict_item.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.created_at IS '创建时间';


--
-- Name: COLUMN sys_dict_item.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_dict_item.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_dict_item.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.deleted IS '是否删除';


--
-- Name: COLUMN sys_dict_item.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_dict_item.version IS '乐观锁';


--
-- Name: sys_file_chunk; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_file_chunk (
    id uuid NOT NULL,
    file_name character varying(255) NOT NULL,
    file_id character varying(64) NOT NULL,
    chunk_index integer NOT NULL,
    total_chunks integer NOT NULL,
    chunk_path character varying(500) NOT NULL,
    chunk_size bigint NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_file_chunk; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_file_chunk IS '文件上传信息';


--
-- Name: COLUMN sys_file_chunk.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.id IS '主键ID';


--
-- Name: COLUMN sys_file_chunk.file_name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.file_name IS '文件原名';


--
-- Name: COLUMN sys_file_chunk.file_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.file_id IS '文件唯一标识（如 SHA256 或 UUID）';


--
-- Name: COLUMN sys_file_chunk.chunk_index; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.chunk_index IS '分片序号（从 0 开始）';


--
-- Name: COLUMN sys_file_chunk.total_chunks; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.total_chunks IS '总分片数（冗余，便于校验）';


--
-- Name: COLUMN sys_file_chunk.chunk_path; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.chunk_path IS '分片在磁盘/OSS 的存储路径或 Key';


--
-- Name: COLUMN sys_file_chunk.chunk_size; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.chunk_size IS '当前分片字节数';


--
-- Name: COLUMN sys_file_chunk.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.created_by IS '创建人';


--
-- Name: COLUMN sys_file_chunk.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.created_at IS '创建时间';


--
-- Name: COLUMN sys_file_chunk.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_file_chunk.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_file_chunk.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.deleted IS '删除时间';


--
-- Name: COLUMN sys_file_chunk.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_chunk.version IS '乐观锁';


--
-- Name: sys_file_info; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_file_info (
    id uuid NOT NULL,
    file_name character varying(32) NOT NULL,
    origin_name character varying(50) NOT NULL,
    suffix character varying(50) NOT NULL,
    path character varying(500) NOT NULL,
    size bigint NOT NULL,
    hash character varying(64),
    storage_type integer NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_file_info; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_file_info IS '文件上传信息';


--
-- Name: COLUMN sys_file_info.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.id IS '主键ID';


--
-- Name: COLUMN sys_file_info.file_name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.file_name IS '生成的32位的文件名称';


--
-- Name: COLUMN sys_file_info.origin_name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.origin_name IS '文件源名称';


--
-- Name: COLUMN sys_file_info.suffix; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.suffix IS '文件后缀';


--
-- Name: COLUMN sys_file_info.path; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.path IS '文件存储位置';


--
-- Name: COLUMN sys_file_info.size; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.size IS '文件大小';


--
-- Name: COLUMN sys_file_info.hash; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.hash IS '文件hash值';


--
-- Name: COLUMN sys_file_info.storage_type; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.storage_type IS '文件存储类型';


--
-- Name: COLUMN sys_file_info.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.created_by IS '创建人';


--
-- Name: COLUMN sys_file_info.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.created_at IS '创建时间';


--
-- Name: COLUMN sys_file_info.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_file_info.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_file_info.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.deleted IS '删除时间';


--
-- Name: COLUMN sys_file_info.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_file_info.version IS '乐观锁';


--
-- Name: sys_log; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_log (
    id uuid NOT NULL,
    type integer,
    explain character varying(255),
    status smallint,
    ip character varying(100),
    method character varying(255),
    url character varying(255),
    args bytea,
    result bytea,
    time_cost bigint,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_log; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_log IS '操作日志表';


--
-- Name: COLUMN sys_log.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.id IS '主键ID';


--
-- Name: COLUMN sys_log.type; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.type IS '日志类型';


--
-- Name: COLUMN sys_log.explain; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.explain IS '日志说明';


--
-- Name: COLUMN sys_log.status; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.status IS '请求状态';


--
-- Name: COLUMN sys_log.ip; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.ip IS '来源IP';


--
-- Name: COLUMN sys_log.method; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.method IS '请求方法';


--
-- Name: COLUMN sys_log.url; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.url IS '请求URL';


--
-- Name: COLUMN sys_log.args; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.args IS '请求参数';


--
-- Name: COLUMN sys_log.result; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.result IS '请求响应';


--
-- Name: COLUMN sys_log.time_cost; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.time_cost IS '耗时';


--
-- Name: COLUMN sys_log.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.created_by IS '创建人';


--
-- Name: COLUMN sys_log.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.created_at IS '创建时间';


--
-- Name: COLUMN sys_log.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_log.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_log.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.deleted IS '是否删除';


--
-- Name: COLUMN sys_log.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_log.version IS '乐观锁';


--
-- Name: sys_menu; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_menu (
    id uuid NOT NULL,
    name character varying(100) NOT NULL,
    pid uuid,
    icon character varying(100),
    path character varying(255) NOT NULL,
    component character varying(100) NOT NULL,
    layout character varying(100),
    sort integer DEFAULT 0,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0,
    hide boolean DEFAULT false,
    metadata jsonb DEFAULT '{}'::jsonb
);


--
-- Name: TABLE sys_menu; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_menu IS '菜单表';


--
-- Name: COLUMN sys_menu.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.id IS '主键ID';


--
-- Name: COLUMN sys_menu.name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.name IS '名称';


--
-- Name: COLUMN sys_menu.pid; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.pid IS '父级ID';


--
-- Name: COLUMN sys_menu.icon; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.icon IS '图标';


--
-- Name: COLUMN sys_menu.path; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.path IS '请求路径';


--
-- Name: COLUMN sys_menu.component; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.component IS '组件路径,为空则使用布局组件';


--
-- Name: COLUMN sys_menu.layout; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.layout IS '布局';


--
-- Name: COLUMN sys_menu.sort; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.sort IS '排序';


--
-- Name: COLUMN sys_menu.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.created_by IS '创建人';


--
-- Name: COLUMN sys_menu.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.created_at IS '创建时间';


--
-- Name: COLUMN sys_menu.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_menu.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_menu.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.deleted IS '是否删除';


--
-- Name: COLUMN sys_menu.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.version IS '乐观锁';


--
-- Name: COLUMN sys_menu.hide; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.hide IS '是否显示再菜单(默认不显示)';


--
-- Name: COLUMN sys_menu.metadata; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_menu.metadata IS '元数据';


--
-- Name: sys_organization; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_organization (
    id uuid NOT NULL,
    pid uuid,
    name character varying(100) NOT NULL,
    code character varying(100) NOT NULL,
    type smallint,
    path character varying(255),
    remark character varying(255),
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_organization; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_organization IS '组织机构表';


--
-- Name: COLUMN sys_organization.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.id IS '主键ID';


--
-- Name: COLUMN sys_organization.pid; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.pid IS '上级ID';


--
-- Name: COLUMN sys_organization.name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.name IS '名称';


--
-- Name: COLUMN sys_organization.code; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.code IS '编码';


--
-- Name: COLUMN sys_organization.type; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.type IS '公司类型';


--
-- Name: COLUMN sys_organization.path; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.path IS '组织机构路径';


--
-- Name: COLUMN sys_organization.remark; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.remark IS '备注';


--
-- Name: COLUMN sys_organization.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.created_by IS '创建人';


--
-- Name: COLUMN sys_organization.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.created_at IS '创建时间';


--
-- Name: COLUMN sys_organization.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_organization.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_organization.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.deleted IS '是否删除';


--
-- Name: COLUMN sys_organization.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_organization.version IS '乐观锁';


--
-- Name: sys_rel_role_authority; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_rel_role_authority (
    id uuid NOT NULL,
    role_id uuid NOT NULL,
    authority_id uuid NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_rel_role_authority; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_rel_role_authority IS '中间表-角色到权限';


--
-- Name: COLUMN sys_rel_role_authority.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_authority.id IS '主键ID';


--
-- Name: COLUMN sys_rel_role_authority.role_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_authority.role_id IS '角色ID';


--
-- Name: COLUMN sys_rel_role_authority.authority_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_authority.authority_id IS '权限ID';


--
-- Name: COLUMN sys_rel_role_authority.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_authority.created_by IS '创建人';


--
-- Name: COLUMN sys_rel_role_authority.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_authority.created_at IS '创建时间';


--
-- Name: COLUMN sys_rel_role_authority.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_authority.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_rel_role_authority.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_authority.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_rel_role_authority.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_authority.deleted IS '是否删除';


--
-- Name: COLUMN sys_rel_role_authority.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_authority.version IS '乐观锁';


--
-- Name: sys_rel_role_menu; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_rel_role_menu (
    id uuid NOT NULL,
    role_id uuid NOT NULL,
    menu_id uuid NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_rel_role_menu; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_rel_role_menu IS '中间表-角色到菜单';


--
-- Name: COLUMN sys_rel_role_menu.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_menu.id IS '主键ID';


--
-- Name: COLUMN sys_rel_role_menu.role_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_menu.role_id IS '角色ID';


--
-- Name: COLUMN sys_rel_role_menu.menu_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_menu.menu_id IS '菜单ID';


--
-- Name: COLUMN sys_rel_role_menu.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_menu.created_by IS '创建人';


--
-- Name: COLUMN sys_rel_role_menu.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_menu.created_at IS '创建时间';


--
-- Name: COLUMN sys_rel_role_menu.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_menu.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_rel_role_menu.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_menu.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_rel_role_menu.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_menu.deleted IS '删除标识';


--
-- Name: COLUMN sys_rel_role_menu.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_role_menu.version IS '乐观锁';


--
-- Name: sys_rel_user_role; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_rel_user_role (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    role_id uuid NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_rel_user_role; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_rel_user_role IS '中间表-用户到角色';


--
-- Name: COLUMN sys_rel_user_role.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_user_role.id IS '主键ID';


--
-- Name: COLUMN sys_rel_user_role.user_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_user_role.user_id IS '用户ID';


--
-- Name: COLUMN sys_rel_user_role.role_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_user_role.role_id IS '角色ID';


--
-- Name: COLUMN sys_rel_user_role.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_user_role.created_by IS '创建人';


--
-- Name: COLUMN sys_rel_user_role.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_user_role.created_at IS '创建时间';


--
-- Name: COLUMN sys_rel_user_role.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_user_role.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_rel_user_role.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_user_role.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_rel_user_role.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_user_role.deleted IS '是否删除';


--
-- Name: COLUMN sys_rel_user_role.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_rel_user_role.version IS '乐观锁';


--
-- Name: sys_role; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_role (
    id uuid NOT NULL,
    name character varying(100),
    code character varying(100),
    state boolean DEFAULT true,
    scope integer,
    builtin boolean DEFAULT false,
    remark text,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_role; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_role IS '角色表';


--
-- Name: COLUMN sys_role.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.id IS '主键ID';


--
-- Name: COLUMN sys_role.name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.name IS '名称';


--
-- Name: COLUMN sys_role.code; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.code IS '编码';


--
-- Name: COLUMN sys_role.state; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.state IS '状态';


--
-- Name: COLUMN sys_role.scope; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.scope IS '范围';


--
-- Name: COLUMN sys_role.builtin; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.builtin IS '是否内置';


--
-- Name: COLUMN sys_role.remark; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.remark IS '备注';


--
-- Name: COLUMN sys_role.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.created_by IS '创建人';


--
-- Name: COLUMN sys_role.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.created_at IS '创建时间';


--
-- Name: COLUMN sys_role.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_role.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_role.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.deleted IS '是否删除';


--
-- Name: COLUMN sys_role.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role.version IS '乐观锁';


--
-- Name: sys_role_data_scope; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_role_data_scope (
    id uuid NOT NULL,
    role_id uuid NOT NULL,
    scope_type integer NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_role_data_scope; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_role_data_scope IS '角色数据范围';


--
-- Name: COLUMN sys_role_data_scope.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope.id IS '主键ID';


--
-- Name: COLUMN sys_role_data_scope.role_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope.role_id IS '角色ID';


--
-- Name: COLUMN sys_role_data_scope.scope_type; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope.scope_type IS '范围类型';


--
-- Name: COLUMN sys_role_data_scope.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope.created_by IS '创建人';


--
-- Name: COLUMN sys_role_data_scope.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope.created_at IS '创建时间';


--
-- Name: COLUMN sys_role_data_scope.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_role_data_scope.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_role_data_scope.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope.deleted IS '是否删除';


--
-- Name: COLUMN sys_role_data_scope.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope.version IS '乐观锁';


--
-- Name: sys_role_data_scope_target; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_role_data_scope_target (
    id uuid NOT NULL,
    role_id uuid NOT NULL,
    target_id uuid NOT NULL,
    target_type integer NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_role_data_scope_target; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_role_data_scope_target IS '角色数据范围(自定义情况下)';


--
-- Name: COLUMN sys_role_data_scope_target.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.id IS '主键ID';


--
-- Name: COLUMN sys_role_data_scope_target.role_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.role_id IS '角色ID';


--
-- Name: COLUMN sys_role_data_scope_target.target_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.target_id IS '目标ID';


--
-- Name: COLUMN sys_role_data_scope_target.target_type; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.target_type IS '目标类型（DEPT / PROJECT / ORG / TENANT）';


--
-- Name: COLUMN sys_role_data_scope_target.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.created_by IS '创建人';


--
-- Name: COLUMN sys_role_data_scope_target.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.created_at IS '创建时间';


--
-- Name: COLUMN sys_role_data_scope_target.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_role_data_scope_target.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_role_data_scope_target.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.deleted IS '是否删除';


--
-- Name: COLUMN sys_role_data_scope_target.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_role_data_scope_target.version IS '乐观锁';


--
-- Name: sys_user; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_user (
    id uuid NOT NULL,
    username character varying(100) NOT NULL,
    avatar character varying(255),
    status smallint DEFAULT 1 NOT NULL,
    real_name character varying(50),
    gender integer DEFAULT 0,
    birthday date,
    phone character varying(20),
    email character varying(100),
    country character varying(50),
    city character varying(50),
    language character varying(10) DEFAULT 'zh-CN'::character varying,
    timezone character varying(40) DEFAULT 'Asia/Shanghai'::character varying,
    organization_id uuid NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_user; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_user IS '用户表';


--
-- Name: COLUMN sys_user.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.id IS '主键ID';


--
-- Name: COLUMN sys_user.username; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.username IS '显示名称';


--
-- Name: COLUMN sys_user.avatar; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.avatar IS '头像';


--
-- Name: COLUMN sys_user.status; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.status IS '状态 (1:正常 0:禁用)';


--
-- Name: COLUMN sys_user.real_name; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.real_name IS '真实姓名';


--
-- Name: COLUMN sys_user.gender; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.gender IS '性别(0:保密)';


--
-- Name: COLUMN sys_user.birthday; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.birthday IS '生日';


--
-- Name: COLUMN sys_user.phone; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.phone IS '手机号';


--
-- Name: COLUMN sys_user.email; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.email IS '邮箱';


--
-- Name: COLUMN sys_user.country; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.country IS '国家';


--
-- Name: COLUMN sys_user.city; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.city IS '城市';


--
-- Name: COLUMN sys_user.language; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.language IS '语言';


--
-- Name: COLUMN sys_user.timezone; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.timezone IS '时区';


--
-- Name: COLUMN sys_user.organization_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.organization_id IS '组织机构ID';


--
-- Name: COLUMN sys_user.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.created_by IS '创建人';


--
-- Name: COLUMN sys_user.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.created_at IS '创建时间';


--
-- Name: COLUMN sys_user.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_user.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_user.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.deleted IS '删除时间';


--
-- Name: COLUMN sys_user.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user.version IS '乐观锁';


--
-- Name: sys_user_data_scope; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_user_data_scope (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    scope_type integer NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_user_data_scope; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_user_data_scope IS '用户数据范围（直授，优先级高于角色）';


--
-- Name: COLUMN sys_user_data_scope.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope.id IS '主键ID';


--
-- Name: COLUMN sys_user_data_scope.user_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope.user_id IS '用户ID';


--
-- Name: COLUMN sys_user_data_scope.scope_type; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope.scope_type IS '数据范围类型';


--
-- Name: COLUMN sys_user_data_scope.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope.created_by IS '创建人';


--
-- Name: COLUMN sys_user_data_scope.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope.created_at IS '创建时间';


--
-- Name: COLUMN sys_user_data_scope.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_user_data_scope.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_user_data_scope.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope.deleted IS '删除时间';


--
-- Name: COLUMN sys_user_data_scope.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope.version IS '乐观锁';


--
-- Name: sys_user_data_scope_target; Type: TABLE; Schema: domain_core; Owner: -
--

CREATE TABLE domain_core.sys_user_data_scope_target (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    target_id uuid NOT NULL,
    target_type integer NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0
);


--
-- Name: TABLE sys_user_data_scope_target; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON TABLE domain_core.sys_user_data_scope_target IS '用户数据范围,自定义情况下使用';


--
-- Name: COLUMN sys_user_data_scope_target.id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.id IS '主键ID';


--
-- Name: COLUMN sys_user_data_scope_target.user_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.user_id IS '用户ID';


--
-- Name: COLUMN sys_user_data_scope_target.target_id; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.target_id IS '目标ID';


--
-- Name: COLUMN sys_user_data_scope_target.target_type; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.target_type IS '目标类型';


--
-- Name: COLUMN sys_user_data_scope_target.created_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.created_by IS '创建人';


--
-- Name: COLUMN sys_user_data_scope_target.created_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.created_at IS '创建时间';


--
-- Name: COLUMN sys_user_data_scope_target.updated_by; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.updated_by IS '最后更新人';


--
-- Name: COLUMN sys_user_data_scope_target.updated_at; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.updated_at IS '最后更新时间';


--
-- Name: COLUMN sys_user_data_scope_target.deleted; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.deleted IS '删除时间';


--
-- Name: COLUMN sys_user_data_scope_target.version; Type: COMMENT; Schema: domain_core; Owner: -
--

COMMENT ON COLUMN domain_core.sys_user_data_scope_target.version IS '乐观锁';


--
-- Data for Name: sys_account; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_account (id, user_id, type, login_name, password, phone, email, openid, unionid, provider, status, verified, expires_at, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfa5-e4cc-7f46-bc0c-8559c2a2f3d2	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	1	devops@devops00.com	$2a$10$ouLcBhi7OK3JFHeuRvRyu.HfL1naBROM09QPoYSydRxhk6WP4l5c6	\N	\N	\N	\N	DEFAULT	0	\N	\N	\N	2026-01-21 16:22:29.836799+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:37:38.22079+08	\N	1
019bdfec-3275-7032-9bd9-c854be37aad4	019bdfec-3202-764d-a1fc-d44c05c72db2	1	admin@devops00.com	$2a$10$joERR4zsN/scRgrdRoh4W.TvqbdZfqi8l4pKqvuYVpAfN4ntS0NAi	\N	\N	\N	\N	DEFAULT	0	0	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:17.237102+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:17.237102+08	\N	0
019bdfec-d39e-7cda-874e-fe410ff4783d	019bdfec-d334-7278-985c-44cfaa9b1a68	1	user@devops00.com	$2a$10$SEYQH73YEUboyu7JVNUGpurOTki4G5TO.R/OrREnuA7C9U6yaMzt.	\N	\N	\N	\N	DEFAULT	0	0	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:58.495469+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:58.495469+08	\N	0
019bdfed-7f69-7d3a-86e5-d442f990bc61	019bdfed-7ef5-7026-a8f0-c0c987653bee	1	audit@devops00.com	$2a$10$nlnGVUJCOczL7TbVFb06v.m36zXsETiXILDXX55HyrvaoimNbsUFy	\N	\N	\N	\N	DEFAULT	0	0	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:40:42.473797+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:40:42.473797+08	\N	0
\.


--
-- Data for Name: sys_authority; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_authority (id, pid, name, code, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdf8f-6542-7b9b-8fc7-2eae5b1a4c94	\N	顶级权限	*	\N	2026-01-21 15:57:55.394469+08	\N	2026-01-21 15:57:55.394469+08	\N	0
019bdf8f-6563-728e-8fb4-0b6b77e87de7	019bdf8f-6542-7b9b-8fc7-2eae5b1a4c94	菜单权限	MENU:*	\N	2026-01-21 15:57:55.427466+08	\N	2026-01-21 15:57:55.427466+08	\N	0
019bdf8f-6564-73e4-b9ad-f4f36250d4a5	019bdf8f-6542-7b9b-8fc7-2eae5b1a4c94	字典管理	DICT:*	\N	2026-01-21 15:57:55.428466+08	\N	2026-01-21 15:57:55.428466+08	\N	0
019bdf8f-6566-7223-87cc-0a6f50434579	019bdf8f-6542-7b9b-8fc7-2eae5b1a4c94	部门管理	DEPT:*	\N	2026-01-21 15:57:55.430143+08	\N	2026-01-21 15:57:55.430143+08	\N	0
019bdf8f-6566-7223-87cd-87dbcc2326cb	019bdf8f-6542-7b9b-8fc7-2eae5b1a4c94	用户管理	USER:*	\N	2026-01-21 15:57:55.43115+08	\N	2026-01-21 15:57:55.43115+08	\N	0
019bdf8f-656c-7b4b-aa6f-f05ae97ff563	019bdf8f-6563-728e-8fb4-0b6b77e87de7	菜单新增	MENU:INSERT	\N	2026-01-21 15:57:55.436149+08	\N	2026-01-21 15:57:55.436149+08	\N	0
019bdf8f-656d-7592-8ab1-ffe539bb695e	019bdf8f-6563-728e-8fb4-0b6b77e87de7	菜单修改	MENU:UPDATE	\N	2026-01-21 15:57:55.437151+08	\N	2026-01-21 15:57:55.438149+08	\N	0
019bdf8f-656e-74b6-ab1a-41ce6ce22cf2	019bdf8f-6563-728e-8fb4-0b6b77e87de7	菜单删除	MENU:DELETE	\N	2026-01-21 15:57:55.438149+08	\N	2026-01-21 15:57:55.438149+08	\N	0
019bdf8f-656f-7346-a28b-8bf16af555f3	019bdf8f-6564-73e4-b9ad-f4f36250d4a5	字典新增	DICT:INSERT	\N	2026-01-21 15:57:55.440152+08	\N	2026-01-21 15:57:55.440152+08	\N	0
019bdf8f-6572-7480-a8f9-21d5c04482c1	019bdf8f-6564-73e4-b9ad-f4f36250d4a5	字典删除	DICT:DELETE	\N	2026-01-21 15:57:55.442151+08	\N	2026-01-21 15:57:55.442151+08	\N	0
019bdf8f-6575-7f1f-af89-9c8962284585	019bdf8f-6564-73e4-b9ad-f4f36250d4a5	字典修改	DICT:UPDATE	\N	2026-01-21 15:57:55.445151+08	\N	2026-01-21 15:57:55.445151+08	\N	0
019bdf8f-6578-7934-835f-95dabf4e16d0	019bdf8f-6566-7223-87cc-0a6f50434579	部门新增	DEPT:INSERT	\N	2026-01-21 15:57:55.448154+08	\N	2026-01-21 15:57:55.448154+08	\N	0
019bdf8f-657b-72a2-9667-3fc36dcc701b	019bdf8f-6566-7223-87cc-0a6f50434579	部门删除	DEPT:DELETE	\N	2026-01-21 15:57:55.451151+08	\N	2026-01-21 15:57:55.451151+08	\N	0
019bdf8f-657d-7e14-96c5-33b987552a24	019bdf8f-6566-7223-87cc-0a6f50434579	部门修改	DEPT:UPDATE	\N	2026-01-21 15:57:55.453159+08	\N	2026-01-21 15:57:55.453159+08	\N	0
019bdf8f-6580-779d-922d-5c318eb945d8	019bdf8f-6566-7223-87cd-87dbcc2326cb	用户新增	USER:INSERT	\N	2026-01-21 15:57:55.456156+08	\N	2026-01-21 15:57:55.456156+08	\N	0
019bdf8f-6583-7f1f-939e-3768f6da693b	019bdf8f-6566-7223-87cd-87dbcc2326cb	用户删除	USER:DELETE	\N	2026-01-21 15:57:55.459155+08	\N	2026-01-21 15:57:55.459155+08	\N	0
019bdf8f-6585-7ba3-b71c-f04590393627	019bdf8f-6566-7223-87cd-87dbcc2326cb	用户修改	USER:UPDATE	\N	2026-01-21 15:57:55.461154+08	\N	2026-01-21 15:57:55.461154+08	\N	0
\.


--
-- Data for Name: sys_config; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_config (id, key, value, type, dict_code, remarks, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
\.


--
-- Data for Name: sys_dict_group; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_dict_group (id, pid, name, code, state, remark, builtin, hide, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfd1-02a5-73f1-be0f-4bd3c53306e0	\N	OA相关	oa	t	\N	f	f	\N	2026-01-21 17:09:35.525161+08	\N	2026-01-21 17:09:35.525161+08	\N	0
019bdfd1-02e3-7a4f-a6cf-826d04d5a4cb	019bdfd1-02a5-73f1-be0f-4bd3c53306e0	流程分类	dict_workflow_type	t	\N	f	f	\N	2026-01-21 17:09:35.588161+08	\N	2026-01-21 17:09:35.588161+08	\N	0
019bdfd1-01ca-7fc2-81df-45019d3b1672	\N	系统配置	sys	t	\N	t	f	\N	2026-01-21 17:09:35.309686+08	\N	2026-01-21 17:09:35.309686+08	\N	0
019bdfd1-02e9-7570-a250-d3c48bd15fff	019bdfd1-01ca-7fc2-81df-45019d3b1672	用户状态	sys_user_state	t	\N	t	f	\N	2026-01-21 17:09:35.593161+08	\N	2026-01-21 17:09:35.593161+08	\N	0
019bdfd1-02ec-7305-bb8b-ab958b70b6c7	019bdfd1-01ca-7fc2-81df-45019d3b1672	通用状态	sys_common_state	t	\N	t	f	\N	2026-01-21 17:09:35.596163+08	\N	2026-01-21 17:09:35.596163+08	\N	0
019bdfd1-02ee-7a8c-b71c-fdebcfd30441	019bdfd1-01ca-7fc2-81df-45019d3b1672	组织机构类型	sys_organization_type	t	\N	t	f	\N	2026-01-21 17:09:35.599164+08	\N	2026-01-21 17:09:35.599164+08	\N	0
019bdfd1-02f2-74ee-aa27-116d0ae87c42	019bdfd1-01ca-7fc2-81df-45019d3b1672	用户性别	sys_user_gender	t	\N	t	f	\N	2026-01-21 17:09:35.60217+08	\N	2026-01-21 17:09:35.60217+08	\N	0
019bdfd1-02f5-7fbd-9ad6-bec21e763338	019bdfd1-01ca-7fc2-81df-45019d3b1672	时区	sys_timezone	t	\N	t	f	\N	2026-01-21 17:09:35.605164+08	\N	2026-01-21 17:09:35.605164+08	\N	0
019bdfd1-02f9-74a3-a177-b07ba13b2f8d	019bdfd1-01ca-7fc2-81df-45019d3b1672	语言	sys_language	t	\N	t	f	\N	2026-01-21 17:09:35.609162+08	\N	2026-01-21 17:09:35.609162+08	\N	0
019bdfd1-02fc-7881-870d-91e9b209218c	019bdfd1-01ca-7fc2-81df-45019d3b1672	邮箱后缀	sys_email_suffix	t	\N	t	f	\N	2026-01-21 17:09:35.613162+08	\N	2026-01-21 17:09:35.613162+08	\N	0
019bdfd1-02ff-746e-a7b2-df0bfa44e659	019bdfd1-01ca-7fc2-81df-45019d3b1672	水印类型	sys_watermark	t	\N	t	f	\N	2026-01-21 17:09:35.61516+08	\N	2026-01-21 17:09:35.61516+08	\N	0
\.


--
-- Data for Name: sys_dict_item; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_dict_item (id, gid, label, value, sort, state, remark, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfdc-92bb-7f5a-ae4b-36fd1a6cfb60	019bdfd1-02e3-7a4f-a6cf-826d04d5a4cb	财务	0	0	0	\N	\N	2026-01-21 17:22:13.312308+08	\N	2026-01-21 17:22:13.312308+08	\N	0
019bdfdc-92d8-70d3-98c6-a1a1b8710a18	019bdfd1-02e3-7a4f-a6cf-826d04d5a4cb	人事	1	0	0	\N	\N	2026-01-21 17:22:13.337304+08	\N	2026-01-21 17:22:13.337304+08	\N	0
019bdfdc-92dd-789f-bf23-78a6a309c61f	019bdfd1-02e9-7570-a250-d3c48bd15fff	正常	0	0	0	\N	\N	2026-01-21 17:22:13.341305+08	\N	2026-01-21 17:22:13.341305+08	\N	0
019bdfdc-92e3-7199-b91b-a730bea38304	019bdfd1-02e9-7570-a250-d3c48bd15fff	冻结	1	0	0	\N	\N	2026-01-21 17:22:13.347306+08	\N	2026-01-21 17:22:13.347306+08	\N	0
019bdfdc-92e5-7647-9ca7-3df8f956928e	019bdfd1-02e9-7570-a250-d3c48bd15fff	封禁	2	0	0	\N	\N	2026-01-21 17:22:13.350308+08	\N	2026-01-21 17:22:13.350308+08	\N	0
019bdfdc-92e9-7e06-bec2-94323686d9be	019bdfd1-02ec-7305-bb8b-ab958b70b6c7	启用	0	0	0	\N	\N	2026-01-21 17:22:13.353305+08	\N	2026-01-21 17:22:13.353305+08	\N	0
019bdfdc-92ec-78dc-bf95-f9fbb122adb5	019bdfd1-02ec-7305-bb8b-ab958b70b6c7	禁用	1	0	0	\N	\N	2026-01-21 17:22:13.356308+08	\N	2026-01-21 17:22:13.356308+08	\N	0
019bdfdc-92ef-706b-aebb-e9a8865e7e0d	019bdfd1-02ee-7a8c-b71c-fdebcfd30441	系统运维	0	0	0	\N	\N	2026-01-21 17:22:13.359302+08	\N	2026-01-21 17:22:13.359302+08	\N	0
019bdfdc-92f1-7a96-b602-c820e6a1efbc	019bdfd1-02ee-7a8c-b71c-fdebcfd30441	集团总部	1	0	0	\N	\N	2026-01-21 17:22:13.361302+08	\N	2026-01-21 17:22:13.362303+08	\N	0
019bdfdc-92f4-7d7d-9868-c7387336c9d1	019bdfd1-02ee-7a8c-b71c-fdebcfd30441	省级公司	2	0	0	\N	\N	2026-01-21 17:22:13.364302+08	\N	2026-01-21 17:22:13.364302+08	\N	0
019bdfdc-92f6-7ce0-9876-b265f6dba3ae	019bdfd1-02ee-7a8c-b71c-fdebcfd30441	市级公司	3	0	0	\N	\N	2026-01-21 17:22:13.366305+08	\N	2026-01-21 17:22:13.366305+08	\N	0
019bdfdc-92f8-7a70-aa29-864107dea7dc	019bdfd1-02ee-7a8c-b71c-fdebcfd30441	县级公司	4	0	0	\N	\N	2026-01-21 17:22:13.368304+08	\N	2026-01-21 17:22:13.368304+08	\N	0
019bdfdc-92fa-76cf-87a3-5b030e71a57c	019bdfd1-02ee-7a8c-b71c-fdebcfd30441	部门	5	0	0	\N	\N	2026-01-21 17:22:13.370303+08	\N	2026-01-21 17:22:13.370303+08	\N	0
019bdfdc-92fc-7017-855c-fc0655ac2324	019bdfd1-02ee-7a8c-b71c-fdebcfd30441	科室/小组	6	0	0	\N	\N	2026-01-21 17:22:13.372302+08	\N	2026-01-21 17:22:13.372302+08	\N	0
019bdfdc-92fe-7799-97cc-46a6004ce2b7	019bdfd1-02f2-74ee-aa27-116d0ae87c42	未知	1	0	0	\N	\N	2026-01-21 17:22:13.374308+08	\N	2026-01-21 17:22:13.374308+08	\N	0
019bdfdc-9301-7451-850c-6b0b15149ca0	019bdfd1-02f2-74ee-aa27-116d0ae87c42	男性	2	0	0	\N	\N	2026-01-21 17:22:13.377302+08	\N	2026-01-21 17:22:13.377302+08	\N	0
019bdfdc-9303-7fbe-9647-25f7ed0008c9	019bdfd1-02f2-74ee-aa27-116d0ae87c42	女性	3	0	0	\N	\N	2026-01-21 17:22:13.379304+08	\N	2026-01-21 17:22:13.379304+08	\N	0
019bdfdc-9305-7645-bcb1-eebc3e19470f	019bdfd1-02f2-74ee-aa27-116d0ae87c42	人妖	4	0	0	\N	\N	2026-01-21 17:22:13.3813+08	\N	2026-01-21 17:22:13.3813+08	\N	0
019bdfdc-9307-761f-af79-850d14f6d3a9	019bdfd1-02f2-74ee-aa27-116d0ae87c42	沃尔玛塑料袋	5	0	0	\N	\N	2026-01-21 17:22:13.384302+08	\N	2026-01-21 17:22:13.384302+08	\N	0
019bdfdc-930a-7823-8d53-bc37c26f9087	019bdfd1-02f5-7fbd-9ad6-bec21e763338	国际日期变更线西	Etc/GMT+12	0	0	\N	\N	2026-01-21 17:22:13.386303+08	\N	2026-01-21 17:22:13.386303+08	\N	0
019bdfdc-930c-7a85-ae43-6bcb00ffcaa1	019bdfd1-02f5-7fbd-9ad6-bec21e763338	萨摩亚时间	Pacific/Pago_Pago	0	0	\N	\N	2026-01-21 17:22:13.388302+08	\N	2026-01-21 17:22:13.388781+08	\N	0
019bdfdc-930e-768e-aa7a-ba2b259e7dc9	019bdfd1-02f5-7fbd-9ad6-bec21e763338	夏威夷时间	Pacific/Honolulu	0	0	\N	\N	2026-01-21 17:22:13.390303+08	\N	2026-01-21 17:22:13.390303+08	\N	0
019bdfdc-930f-7499-b01b-873dd61559b2	019bdfd1-02f5-7fbd-9ad6-bec21e763338	阿拉斯加时间	America/Anchorage	0	0	\N	\N	2026-01-21 17:22:13.391828+08	\N	2026-01-21 17:22:13.391828+08	\N	0
019bdfdc-9311-7897-8b67-275683446e2b	019bdfd1-02f5-7fbd-9ad6-bec21e763338	美国太平洋时间	America/Los_Angeles	0	0	\N	\N	2026-01-21 17:22:13.393829+08	\N	2026-01-21 17:22:13.393829+08	\N	0
019bdfdc-9312-75ec-ba9f-2d9377523bcb	019bdfd1-02f5-7fbd-9ad6-bec21e763338	美国山地时间	America/Denver	0	0	\N	\N	2026-01-21 17:22:13.395843+08	\N	2026-01-21 17:22:13.395843+08	\N	0
019bdfdc-9315-70af-922a-8915c33fe540	019bdfd1-02f5-7fbd-9ad6-bec21e763338	美国中部时间	America/Chicago	0	0	\N	\N	2026-01-21 17:22:13.397828+08	\N	2026-01-21 17:22:13.398834+08	\N	0
019bdfdc-9318-753b-b85f-6e805903fd46	019bdfd1-02f5-7fbd-9ad6-bec21e763338	美国东部时间	America/New_York	0	0	\N	\N	2026-01-21 17:22:13.400838+08	\N	2026-01-21 17:22:13.400838+08	\N	0
019bdfdc-931d-7a2b-8cb5-70c88663c27c	019bdfd1-02f5-7fbd-9ad6-bec21e763338	大西洋时间	America/Halifax	0	0	\N	\N	2026-01-21 17:22:13.406831+08	\N	2026-01-21 17:22:13.406831+08	\N	0
019bdfdc-9320-74bd-8f48-c45990c71947	019bdfd1-02f5-7fbd-9ad6-bec21e763338	巴西时间（圣保罗）	America/Sao_Paulo	0	0	\N	\N	2026-01-21 17:22:13.408834+08	\N	2026-01-21 17:22:13.408834+08	\N	0
019bdfdc-9322-73be-bca6-1618999a21f4	019bdfd1-02f5-7fbd-9ad6-bec21e763338	亚速尔群岛时间	Atlantic/Azores	0	0	\N	\N	2026-01-21 17:22:13.410829+08	\N	2026-01-21 17:22:13.410829+08	\N	0
019bdfdc-9324-7ed3-903a-0821dc7289a9	019bdfd1-02f5-7fbd-9ad6-bec21e763338	协调世界时	UTC	0	0	\N	\N	2026-01-21 17:22:13.412834+08	\N	2026-01-21 17:22:13.412834+08	\N	0
019bdfdc-9325-74fc-a126-ab23dbed02bb	019bdfd1-02f5-7fbd-9ad6-bec21e763338	中欧时间（柏林）	Europe/Berlin	0	0	\N	\N	2026-01-21 17:22:13.414836+08	\N	2026-01-21 17:22:13.414836+08	\N	0
019bdfdc-932a-70ce-86ae-a1fdc61d0799	019bdfd1-02f5-7fbd-9ad6-bec21e763338	东欧时间（雅典）	Europe/Athens	0	0	\N	\N	2026-01-21 17:22:13.41883+08	\N	2026-01-21 17:22:13.41883+08	\N	0
019bdfdc-932e-7ac1-b734-1acbd3521d76	019bdfd1-02f5-7fbd-9ad6-bec21e763338	莫斯科时间	Europe/Moscow	0	0	\N	\N	2026-01-21 17:22:13.42283+08	\N	2026-01-21 17:22:13.42283+08	\N	0
019bdfdc-9330-7e85-b541-20667a503a2f	019bdfd1-02f5-7fbd-9ad6-bec21e763338	印度标准时间	Asia/Kolkata	0	0	\N	\N	2026-01-21 17:22:13.424828+08	\N	2026-01-21 17:22:13.424828+08	\N	0
019bdfdc-9332-75c0-aa5b-ed63ac918310	019bdfd1-02f5-7fbd-9ad6-bec21e763338	中国标准时间(北京时间)	Asia/Shanghai	0	0	\N	\N	2026-01-21 17:22:13.426832+08	\N	2026-01-21 17:22:13.426832+08	\N	0
019bdfdc-9333-7026-a092-e80556a1564d	019bdfd1-02f5-7fbd-9ad6-bec21e763338	日本标准时间	Asia/Tokyo	0	0	\N	\N	2026-01-21 17:22:13.428829+08	\N	2026-01-21 17:22:13.428829+08	\N	0
019bdfdc-9335-7e8c-9d6a-99fe9a9e6169	019bdfd1-02f5-7fbd-9ad6-bec21e763338	澳大利亚东部时间	Australia/Sydney	0	0	\N	\N	2026-01-21 17:22:13.429831+08	\N	2026-01-21 17:22:13.429831+08	\N	0
019bdfdc-9337-77aa-a8dc-3042514400c1	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	中文（简体）	zh-CN	0	0	\N	\N	2026-01-21 17:22:13.432834+08	\N	2026-01-21 17:22:13.432834+08	\N	0
019bdfdc-933a-7755-b511-9a18c352f209	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	中文（繁体）	zh-TW	0	0	\N	\N	2026-01-21 17:22:13.434828+08	\N	2026-01-21 17:22:13.434828+08	\N	0
019bdfdc-933d-7f5e-b9f4-d1a20824476e	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	英语	en	0	0	\N	\N	2026-01-21 17:22:13.437831+08	\N	2026-01-21 17:22:13.437831+08	\N	0
019bdfdc-933f-7774-b7e5-4cc7b36be952	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	日语	ja	0	0	\N	\N	2026-01-21 17:22:13.439828+08	\N	2026-01-21 17:22:13.439828+08	\N	0
019bdfdc-9347-708c-889f-dd431ab9d690	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	韩语	ko	0	0	\N	\N	2026-01-21 17:22:13.447857+08	\N	2026-01-21 17:22:13.447857+08	\N	0
019bdfdc-934b-7ccc-b5fc-08d6793e2e48	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	法语	fr	0	0	\N	\N	2026-01-21 17:22:13.451832+08	\N	2026-01-21 17:22:13.451832+08	\N	0
019bdfdc-934e-7de3-ac44-47db37e7c1f7	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	德语	de	0	0	\N	\N	2026-01-21 17:22:13.454833+08	\N	2026-01-21 17:22:13.454833+08	\N	0
019bdfdc-9350-70d0-9ccc-152f1a0622c9	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	西班牙语	es	0	0	\N	\N	2026-01-21 17:22:13.456831+08	\N	2026-01-21 17:22:13.456831+08	\N	0
019bdfdc-9352-72b8-835f-f7e8c34fb9bd	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	俄语	ru	0	0	\N	\N	2026-01-21 17:22:13.458831+08	\N	2026-01-21 17:22:13.458831+08	\N	0
019bdfdc-9354-7249-8559-09084c9a8fb3	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	葡萄牙语	pt	0	0	\N	\N	2026-01-21 17:22:13.460827+08	\N	2026-01-21 17:22:13.460827+08	\N	0
019bdfdc-9356-7ca7-a904-a252b37d522a	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	意大利语	it	0	0	\N	\N	2026-01-21 17:22:13.462826+08	\N	2026-01-21 17:22:13.463831+08	\N	0
019bdfdc-9359-772a-b19f-949deb27a7fd	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	阿拉伯语	ar	0	0	\N	\N	2026-01-21 17:22:13.466831+08	\N	2026-01-21 17:22:13.466831+08	\N	0
019bdfdc-9360-7985-b0fc-07bff0388409	019bdfd1-02f9-74a3-a177-b07ba13b2f8d	印地语	hi	0	0	\N	\N	2026-01-21 17:22:13.472831+08	\N	2026-01-21 17:22:13.472831+08	\N	0
019bdfdc-9363-7ece-8500-b710a4681417	019bdfd1-02fc-7881-870d-91e9b209218c	devops	devops00.com	0	0	\N	\N	2026-01-21 17:22:13.475833+08	\N	2026-01-21 17:22:13.475833+08	\N	0
019bdfdc-9365-7e3b-abab-8965e099d6e0	019bdfd1-02fc-7881-870d-91e9b209218c	谷歌邮箱	gmail.com	0	0	\N	\N	2026-01-21 17:22:13.477831+08	\N	2026-01-21 17:22:13.477831+08	\N	0
019bdfdc-9366-7a1f-a36f-5661d435593c	019bdfd1-02fc-7881-870d-91e9b209218c	QQ邮箱	qq.com	0	0	\N	\N	2026-01-21 17:22:13.479834+08	\N	2026-01-21 17:22:13.479834+08	\N	0
019bdfdc-936a-73ac-b860-229971655692	019bdfd1-02fc-7881-870d-91e9b209218c	微软hotmail	hotmail.com	0	0	\N	\N	2026-01-21 17:22:13.482833+08	\N	2026-01-21 17:22:13.482833+08	\N	0
019bdfdc-936c-7bed-8a7d-aa3b5d0d6441	019bdfd1-02ff-746e-a7b2-df0bfa44e659	系统生成	1	0	0	\N	\N	2026-01-21 17:22:13.485829+08	\N	2026-01-21 17:22:13.485829+08	\N	0
019bdfdc-936e-7dbe-887f-dcf83df17b33	019bdfd1-02ff-746e-a7b2-df0bfa44e659	固定值	2	0	0	\N	\N	2026-01-21 17:22:13.487832+08	\N	2026-01-21 17:22:13.487832+08	\N	0
\.


--
-- Data for Name: sys_file_chunk; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_file_chunk (id, file_name, file_id, chunk_index, total_chunks, chunk_path, chunk_size, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
\.


--
-- Data for Name: sys_file_info; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_file_info (id, file_name, origin_name, suffix, path, size, hash, storage_type, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
\.


--
-- Data for Name: sys_log; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_log (id, type, explain, status, ip, method, url, args, result, time_cost, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
\.


--
-- Data for Name: sys_menu; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_menu (id, name, pid, icon, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted, version, hide, metadata) FROM stdin;
019bdfc5-b220-7bd9-80d1-1a1db193c151	首页	\N	icon-home	/	layout	blank	0	\N	2026-01-21 16:57:14.020461+08	\N	2026-01-21 16:57:14.02146+08	\N	0	f	{}
019bdfc5-b31f-7020-b678-35fae63c432c	工作台	\N	icon-setting	/workbench	layout	blank	1	\N	2026-01-21 16:57:14.272982+08	\N	2026-01-21 16:57:14.272982+08	\N	0	f	{}
019bdfc5-b370-70ca-a33c-25044878eeda	访问控制	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-module	RBAC	/System/RBAC/index	\N	1	\N	2026-01-21 16:57:14.353982+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:42:02.294811+08	\N	0	f	{}
019bdfc5-b32c-74e9-90ac-0540954c4e4a	系统管理	\N	icon-setting	/system	layout	default	2	\N	2026-01-21 16:57:14.284977+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:01:49.940919+08	\N	0	f	{}
019bdfc5-b328-7de0-9e8c-2ac0cc51969e	系统监控	\N	icon-setting	/monitor	layout	default	3	\N	2026-01-21 16:57:14.282559+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:01:56.864649+08	\N	0	f	{}
019bdfc5-b32a-7c31-bbff-3992be5fff64	组件示例	\N	icon-setting	/exampl	layout	default	4	\N	2026-01-21 16:57:14.283978+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:02:05.223473+08	\N	0	f	{}
019bdfc5-b34b-7619-8f37-b052e64e4e27	工作台默认页面	019bdfc5-b31f-7020-b678-35fae63c432c	icon-module		/Workbench/index	\N	999	\N	2026-01-21 16:57:14.316985+08	\N	2026-01-21 16:57:14.316985+08	\N	0	f	{}
019bdfc5-b35f-74b6-abe3-3816db511129	文件存储	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-module	storage	/System/Storage/index	\N	7	\N	2026-01-21 16:57:14.335987+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-29 14:07:02.947915+08	\N	0	f	{}
019bdfc5-b365-7a12-b646-3a5c922bd6f9	流程管理	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-module	workflow	/System/Workflow/index	\N	8	\N	2026-01-21 16:57:14.341991+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-29 14:07:07.998376+08	\N	0	f	{}
019bdfc5-b36d-72df-b7a7-6c82d9199988	许可管理	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-module	license	/System/License/index	\N	9	\N	2026-01-21 16:57:14.349983+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-29 14:07:12.311505+08	\N	0	f	{}
019bdfc5-b347-75c0-bcac-98ed9e44cf93	首页默认	019bdfc5-b220-7bd9-80d1-1a1db193c151	icon-module		/Home/index	\N	999	\N	2026-01-21 16:57:14.312987+08	\N	2026-01-21 16:57:14.312987+08	\N	0	f	{"title": "test"}
019c085c-9a02-7356-8326-91423a326b20	流程编辑	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-im-lessen	flow-edit	/System/Workflow/components/EditWorkflow/index	\N	999	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-29 14:06:52.418209+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-29 14:07:39.582299+08	\N	0	t	{"crumbs": [{"path": "/system", "title": "系统管理"}, {"path": "/system/workflow", "title": "流程管理"}, {"title": "流程编辑"}]}
019bdfc5-b35d-7a55-b441-38f77a88036a	表单示例	019bdfc5-b32a-7c31-bbff-3992be5fff64	icon-module	form	/Example/Form/index	\N	0	\N	2026-01-21 16:57:14.333987+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:45:32.585188+08	\N	0	f	{}
019c08c3-1ba7-786f-b868-91132643c6ac	表单编辑	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-im-lessen	form-edit	/System/Workflow/components/EditForm/index	\N	999	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-29 15:58:50.279527+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-29 15:58:50.279527+08	\N	0	t	{"crumbs": [{"path": "/system", "title": "系统管理"}, {"path": "/system/workflow", "title": "流程管理"}, {"title": "表单编辑"}]}
019bdfc5-b362-70a0-8cbd-53f96e96c64c	用户管理	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-module	user	/System/User/index	\N	0	\N	2026-01-21 16:57:14.338025+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:41:55.678661+08	\N	0	f	{}
019bdfc5-b367-7e26-9c50-620660e13019	部门管理	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-module	dept	/System/Dept/index	\N	3	\N	2026-01-21 16:57:14.343989+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:42:13.985663+08	\N	0	f	{}
019bdfc5-b363-750f-8cd2-010b659463a8	系统配置	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-module	configured	System/Configured/index	\N	4	\N	2026-01-21 16:57:14.339985+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:42:20.123378+08	\N	0	f	{}
019bdfc5-b36a-7700-b8c3-7c251f1f79a2	字典管理	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-module	dict	/System/Dict/index	\N	5	\N	2026-01-21 16:57:14.346984+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:42:31.154823+08	\N	0	f	{}
019bdfc5-b36f-7e70-a79d-09c5facdf296	菜单管理	019bdfc5-b32c-74e9-90ac-0540954c4e4a	icon-module	menu	/System/Menu/index	\N	6	\N	2026-01-21 16:57:14.351982+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:42:35.344738+08	\N	0	f	{}
019bdfc5-b352-7d24-b5af-8d0a0042a4f9	定时任务	019bdfc5-b328-7de0-9e8c-2ac0cc51969e	icon-module	task	/Monitor/Task/index	\N	0	\N	2026-01-21 16:57:14.322981+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:45:09.60628+08	\N	0	f	{}
019bdfc5-b34d-74fd-8ad8-f2f7976634d1	服务监控	019bdfc5-b328-7de0-9e8c-2ac0cc51969e	icon-module	server	/Monitor/Server/index	\N	1	\N	2026-01-21 16:57:14.318993+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:45:14.070302+08	\N	0	f	{}
019bdfc5-b350-7168-84d6-ffaaf874b6fc	在线用户	019bdfc5-b328-7de0-9e8c-2ac0cc51969e	icon-module	online	/Monitor/Online/index	\N	2	\N	2026-01-21 16:57:14.320983+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:45:19.533978+08	\N	0	f	{}
019bdfc5-b355-701e-99f2-7012b17490de	缓存监控	019bdfc5-b328-7de0-9e8c-2ac0cc51969e	icon-module	cache	/Monitor/Cache/index	\N	3	\N	2026-01-21 16:57:14.326984+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:45:23.460981+08	\N	0	f	{}
019bdfc5-b358-7794-adf1-b5cc9a3d5883	数据监控	019bdfc5-b328-7de0-9e8c-2ac0cc51969e	icon-module	database	/Monitor/Database/index	\N	4	\N	2026-01-21 16:57:14.329987+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:45:27.022915+08	\N	0	f	{}
019bdfc5-b35a-7d3d-bf74-f903a795d7cd	列表示例	019bdfc5-b32a-7c31-bbff-3992be5fff64	icon-module	table	/Example/Table/index	\N	1	\N	2026-01-21 16:57:14.33098+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:45:37.654173+08	\N	0	f	{}
019bdfc5-b35e-71a2-9b71-c5ab8900f08f	图表示例	019bdfc5-b32a-7c31-bbff-3992be5fff64	icon-module	echarts	/Example/Echarts/index	\N	2	\N	2026-01-21 16:57:14.334981+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:45:41.006238+08	\N	0	f	{}
019bdfc5-b35c-76f0-b622-34e11d75dd27	Markdown	019bdfc5-b32a-7c31-bbff-3992be5fff64	icon-module	markdown	/Example/Markdown/index	\N	3	\N	2026-01-21 16:57:14.33298+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:45:44.724826+08	\N	0	f	{}
\.


--
-- Data for Name: sys_organization; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_organization (id, pid, name, code, type, path, remark, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfde-1c33-77d0-a4a7-4369459486c4	019bdfdd-b58d-7232-943f-af4141801ae3	云南分公司	4A07E7E44BD882D01750E6077A3ABE87	2	光谱平台/云南分公司	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:23:54.035175+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.923761+08	\N	3
019bdfde-395a-7389-8172-be898f21d020	019bdfdd-b58d-7232-943f-af4141801ae3	昆明分公司	A14F626FFE5A56928DA137200717401A	2	光谱平台/昆明分公司	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:24:01.498991+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.933785+08	\N	3
019bdfde-4fe0-7026-9c07-b5f3cdebfa38	019bdfdd-b58d-7232-943f-af4141801ae3	保山分公司	5A0C052C7A3D38102BDE26641E2F298F	2	光谱平台/保山分公司	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:24:07.264406+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.93476+08	\N	3
019bdfde-bef0-7e80-8af4-3cae506725bc	019bdfde-1c33-77d0-a4a7-4369459486c4	财务部	5C8D8CE2650B94DBBDC4A44FBAE20BE5	5	光谱平台/云南分公司/财务部	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:24:35.696784+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.93676+08	\N	3
019bdfde-dfd8-7366-823f-a527e3b1bf02	019bdfde-1c33-77d0-a4a7-4369459486c4	人事部	EF54E90128EA34F0D1FEE02C1CE99A8C	5	光谱平台/云南分公司/人事部	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:24:44.120535+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.938762+08	\N	3
019bdfdf-04cb-745f-89f1-1b25e43b5698	019bdfde-395a-7389-8172-be898f21d020	财务部	3194B8EF87466CE3914DAA4F509E991E	5	光谱平台/昆明分公司/财务部	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:24:53.579851+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.94076+08	\N	3
019bdfdf-2446-78ae-94e0-283ba2a8a563	019bdfde-395a-7389-8172-be898f21d020	人事部	7F9B8256D46B25C5954EC90FFAF19454	5	光谱平台/昆明分公司/人事部	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:25:01.638047+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.943763+08	\N	3
019bdfdf-55c2-7d73-b92c-057c3a8c6e7a	019bdfde-4fe0-7026-9c07-b5f3cdebfa38	调度小组	C8BB1EAC451144A524067D24DD051CF1	6	光谱平台/保山分公司/调度小组	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:25:14.306744+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.945769+08	\N	3
019bdfdf-72b0-714e-af05-78d48689864a	019bdfde-4fe0-7026-9c07-b5f3cdebfa38	测试小组	472C6E86C39491DF9AA7614641145065	6	光谱平台/保山分公司/测试小组	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:25:21.712246+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.94776+08	\N	3
019bdfdd-b58d-7232-943f-af4141801ae3	\N	光谱平台	5BC060C067A66715107B46D020F4471A	1	光谱平台	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:23:27.758657+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.789761+08	\N	3
019bdfdd-ee9a-7581-ab10-61319ec0753a	\N	系统运维	8143D9BB4521ACEA166E80DB69735CC6	0	系统运维	\N	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:23:42.362701+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:32:56.921763+08	\N	3
\.


--
-- Data for Name: sys_rel_role_authority; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_rel_role_authority (id, role_id, authority_id, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfb6-fd8e-7823-ba61-5727e999b7ca	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdf8f-6542-7b9b-8fc7-2eae5b1a4c94	\N	2026-01-21 16:41:10.287893+08	\N	2026-01-21 16:41:10.287893+08	\N	0
019bdfef-b0fb-7e21-a7c6-d218c51fa808	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdf8f-6542-7b9b-8fc7-2eae5b1a4c94	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:06.235573+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:06.235573+08	\N	0
019bdff0-0125-7607-8f56-67b1379f91b9	019bdfad-df4d-7133-b6db-199c0e86b72b	019bdf8f-6566-7223-87cc-0a6f50434579	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:26.757475+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:26.757475+08	\N	0
019bdff0-134d-7ace-b8db-e5ce5f306084	019bdfad-df4f-7110-b2ce-9eecc82bf46b	019bdf8f-6542-7b9b-8fc7-2eae5b1a4c94	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:31.405234+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:31.405234+08	\N	0
\.


--
-- Data for Name: sys_rel_role_menu; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_rel_role_menu (id, role_id, menu_id, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfc8-29b7-7477-b064-59868f07821a	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b350-7168-84d6-ffaaf874b6fc	\N	2026-01-21 16:59:55.706352+08	\N	2026-01-21 16:59:55.706352+08	\N	0
019bdfc8-29ca-7090-a603-b27f015c5bab	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b34b-7619-8f37-b052e64e4e27	\N	2026-01-21 16:59:55.72436+08	\N	2026-01-21 16:59:55.72436+08	\N	0
019bdfc8-29d5-7233-b0dd-7b5a34b44da1	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b355-701e-99f2-7012b17490de	\N	2026-01-21 16:59:55.73336+08	\N	2026-01-21 16:59:55.73336+08	\N	0
019bdfc8-29d8-7783-900a-5f127d42bd2f	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b352-7d24-b5af-8d0a0042a4f9	\N	2026-01-21 16:59:55.736347+08	\N	2026-01-21 16:59:55.736347+08	\N	0
019bdfc8-29db-7d28-b0ed-9a7c01074ebb	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b34d-74fd-8ad8-f2f7976634d1	\N	2026-01-21 16:59:55.740347+08	\N	2026-01-21 16:59:55.740347+08	\N	0
019bdfc8-29dd-7643-b0ca-bde57e4251e4	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b35f-74b6-abe3-3816db511129	\N	2026-01-21 16:59:55.741352+08	\N	2026-01-21 16:59:55.742346+08	\N	0
019bdfc8-29e0-7650-8eaf-2460e33f9f79	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b367-7e26-9c50-620660e13019	\N	2026-01-21 16:59:55.745357+08	\N	2026-01-21 16:59:55.745357+08	\N	0
019bdfc8-29e2-76ac-b369-154a4dbd6085	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b32c-74e9-90ac-0540954c4e4a	\N	2026-01-21 16:59:55.747348+08	\N	2026-01-21 16:59:55.747348+08	\N	0
019bdfc8-29e4-766e-96c0-5759a3dfea7b	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b35c-76f0-b622-34e11d75dd27	\N	2026-01-21 16:59:55.74835+08	\N	2026-01-21 16:59:55.74835+08	\N	0
019bdfc8-29e5-7ec0-b534-3c4368977902	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b220-7bd9-80d1-1a1db193c151	\N	2026-01-21 16:59:55.75035+08	\N	2026-01-21 16:59:55.75035+08	\N	0
019bdfc8-29e7-75ee-8300-defecd884795	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b35d-7a55-b441-38f77a88036a	\N	2026-01-21 16:59:55.751349+08	\N	2026-01-21 16:59:55.751349+08	\N	0
019bdfc8-29e8-71f0-810e-1f47ddbd0990	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b32a-7c31-bbff-3992be5fff64	\N	2026-01-21 16:59:55.752346+08	\N	2026-01-21 16:59:55.752346+08	\N	0
019bdfc8-29ea-7d68-ac0c-700dcd59250a	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b36a-7700-b8c3-7c251f1f79a2	\N	2026-01-21 16:59:55.754351+08	\N	2026-01-21 16:59:55.754351+08	\N	0
019bdfc8-29ec-7708-9004-e485a2835a12	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b36d-72df-b7a7-6c82d9199988	\N	2026-01-21 16:59:55.757351+08	\N	2026-01-21 16:59:55.757351+08	\N	0
019bdfc8-29ef-7d9b-857c-306d6686bbbb	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b370-70ca-a33c-25044878eeda	\N	2026-01-21 16:59:55.759356+08	\N	2026-01-21 16:59:55.759356+08	\N	0
019bdfc8-29f1-7dec-8861-aeaaf588987b	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b358-7794-adf1-b5cc9a3d5883	\N	2026-01-21 16:59:55.762357+08	\N	2026-01-21 16:59:55.762357+08	\N	0
019bdfc8-29f4-7f4d-b459-11f545137461	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b363-750f-8cd2-010b659463a8	\N	2026-01-21 16:59:55.765354+08	\N	2026-01-21 16:59:55.765354+08	\N	0
019bdfc8-29f6-7a55-8560-9c45214cecd5	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b328-7de0-9e8c-2ac0cc51969e	\N	2026-01-21 16:59:55.767351+08	\N	2026-01-21 16:59:55.767351+08	\N	0
019bdfc8-29f8-7d6c-9287-ab7d396cde85	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b347-75c0-bcac-98ed9e44cf93	\N	2026-01-21 16:59:55.76836+08	\N	2026-01-21 16:59:55.76836+08	\N	0
019bdfc8-29fb-7137-b476-f06d1b7b6d1b	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b36f-7e70-a79d-09c5facdf296	\N	2026-01-21 16:59:55.771349+08	\N	2026-01-21 16:59:55.771349+08	\N	0
019bdfc8-29fd-7ef9-9a2d-695223f87a0f	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b35a-7d3d-bf74-f903a795d7cd	\N	2026-01-21 16:59:55.773348+08	\N	2026-01-21 16:59:55.773348+08	\N	0
019bdfc8-29ff-78f2-ae6a-231711bf0f4a	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b31f-7020-b678-35fae63c432c	\N	2026-01-21 16:59:55.776354+08	\N	2026-01-21 16:59:55.776354+08	\N	0
019bdfc8-2a01-7443-bb0d-0a69388562f3	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b35e-71a2-9b71-c5ab8900f08f	\N	2026-01-21 16:59:55.777346+08	\N	2026-01-21 16:59:55.777346+08	\N	0
019bdfc8-2a04-7d68-9511-b72047e76a8e	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b365-7a12-b646-3a5c922bd6f9	\N	2026-01-21 16:59:55.780352+08	\N	2026-01-21 16:59:55.780352+08	\N	0
019bdfc8-2a06-7a16-aa07-f977441f851a	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	019bdfc5-b362-70a0-8cbd-53f96e96c64c	\N	2026-01-21 16:59:55.782356+08	\N	2026-01-21 16:59:55.782356+08	\N	0
019bdfef-b51b-73e0-942f-fd6df0ea29d8	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b350-7168-84d6-ffaaf874b6fc	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.291997+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.291997+08	\N	0
019bdfef-b51c-7147-afb8-fffec4197fe4	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b34b-7619-8f37-b052e64e4e27	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.292978+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.292978+08	\N	0
019bdfef-b51c-7147-afb9-c6a6dbdd3ca8	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b355-701e-99f2-7012b17490de	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.292978+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.292978+08	\N	0
019bdfef-b51d-71dc-8d78-06c89331660a	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b352-7d24-b5af-8d0a0042a4f9	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.293983+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.293983+08	\N	0
019bdfef-b51d-71dc-8d79-c1f2aa78f731	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b34d-74fd-8ad8-f2f7976634d1	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.293983+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.293983+08	\N	0
019bdfef-b51e-7877-9dad-e10745ba0766	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b35f-74b6-abe3-3816db511129	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.294996+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.294996+08	\N	0
019bdfef-b51e-7877-9dae-0fba5dbb7ab6	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b367-7e26-9c50-620660e13019	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.294996+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.294996+08	\N	0
019bdfef-b51e-7877-9daf-0a13583add21	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b32c-74e9-90ac-0540954c4e4a	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.294996+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.294996+08	\N	0
019bdfef-b51f-7a85-8d93-f2178118e59a	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b35c-76f0-b622-34e11d75dd27	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.295997+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.295997+08	\N	0
019bdfef-b51f-7a85-8d94-8b48f8354f06	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b220-7bd9-80d1-1a1db193c151	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.295997+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.295997+08	\N	0
019bdfef-b520-7e27-ac2e-f0ceb955afba	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b35d-7a55-b441-38f77a88036a	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.296974+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.296974+08	\N	0
019bdfef-b520-7e27-ac2f-b7bde89e0a9c	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b32a-7c31-bbff-3992be5fff64	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.296974+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.296974+08	\N	0
019bdfef-b520-7e27-ac30-ee032f22fcbe	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b370-70ca-a33c-25044878eeda	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.296974+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.296974+08	\N	0
019bdfef-b520-7e27-ac31-d80bd5e909f2	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b36a-7700-b8c3-7c251f1f79a2	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.296974+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.296974+08	\N	0
019bdfef-b520-7e27-ac32-37def9c7520c	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b36d-72df-b7a7-6c82d9199988	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.297979+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.297979+08	\N	0
019bdfef-b521-7ddc-b54f-587ba19bdac5	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b358-7794-adf1-b5cc9a3d5883	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.297979+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.297979+08	\N	0
019bdfef-b522-740b-99f1-da7b4458c24b	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b363-750f-8cd2-010b659463a8	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.298975+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.298975+08	\N	0
019bdfef-b523-7be7-8a9c-39004702c101	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b328-7de0-9e8c-2ac0cc51969e	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.299976+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.299976+08	\N	0
019bdfef-b524-73a4-8cad-f41717890d59	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b347-75c0-bcac-98ed9e44cf93	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.300977+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.300977+08	\N	0
019bdfef-b524-73a4-8cae-050428cbf225	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b36f-7e70-a79d-09c5facdf296	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.300977+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.300977+08	\N	0
019bdfef-b525-72b1-a1c0-b9c21f520d57	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b35a-7d3d-bf74-f903a795d7cd	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.301975+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.301975+08	\N	0
019bdfef-b525-72b1-a1c1-054389e72bce	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b31f-7020-b678-35fae63c432c	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.301975+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.301975+08	\N	0
019bdfef-b526-7a21-9245-862c79ebc18d	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b365-7a12-b646-3a5c922bd6f9	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.302975+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.302975+08	\N	0
019bdfef-b526-7a21-9246-3fde731524b6	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b35e-71a2-9b71-c5ab8900f08f	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.302975+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.302975+08	\N	0
019bdfef-b527-799c-8f0c-60aa0a1bb367	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfc5-b362-70a0-8cbd-53f96e96c64c	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.303975+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:07.303975+08	\N	0
019bdfef-fce6-7835-96c6-f9723c4dc32d	019bdfad-df4d-7133-b6db-199c0e86b72b	019bdfc5-b34b-7619-8f37-b052e64e4e27	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:25.670608+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:25.670608+08	\N	0
019bdfef-fce6-7835-96c7-178d9d36c39e	019bdfad-df4d-7133-b6db-199c0e86b72b	019bdfc5-b220-7bd9-80d1-1a1db193c151	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:25.670608+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:25.670608+08	\N	0
019bdfef-fce7-7d71-9738-7982364a614f	019bdfad-df4d-7133-b6db-199c0e86b72b	019bdfc5-b31f-7020-b678-35fae63c432c	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:25.671605+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:25.671605+08	\N	0
019bdfef-fce7-7d71-9739-46169cd5b55f	019bdfad-df4d-7133-b6db-199c0e86b72b	019bdfc5-b347-75c0-bcac-98ed9e44cf93	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:25.671605+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:25.671605+08	\N	0
019bdff0-1777-70b3-a6d0-e550d3d643ae	019bdfad-df4f-7110-b2ce-9eecc82bf46b	019bdfc5-b220-7bd9-80d1-1a1db193c151	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:32.471577+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:32.471577+08	\N	0
019bdff0-1779-7896-9585-f5aa23538a8d	019bdfad-df4f-7110-b2ce-9eecc82bf46b	019bdfc5-b347-75c0-bcac-98ed9e44cf93	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:32.473583+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:43:32.473583+08	\N	0
\.


--
-- Data for Name: sys_rel_user_role; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_rel_user_role (id, user_id, role_id, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfa5-e4f5-7134-b894-a6e6401e3b32	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	\N	2026-01-21 16:22:29.877802+08	\N	2026-01-21 16:22:29.877802+08	\N	0
019bdfec-3287-70f2-9f02-ac22040035dd	019bdfec-3202-764d-a1fc-d44c05c72db2	019bdfad-df4b-7c16-9f95-a68f9a38a51b	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:17.255107+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:17.255107+08	\N	0
019bdfec-d3a6-7f41-b394-7382a4ca71bc	019bdfec-d334-7278-985c-44cfaa9b1a68	019bdfad-df4d-7133-b6db-199c0e86b72b	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:58.503488+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:58.503488+08	\N	0
019bdfed-7f70-7bd0-a607-226070dca099	019bdfed-7ef5-7026-a8f0-c0c987653bee	019bdfad-df4f-7110-b2ce-9eecc82bf46b	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:40:42.480796+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:40:42.480796+08	\N	0
\.


--
-- Data for Name: sys_role; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_role (id, name, code, state, scope, builtin, remark, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfad-ded6-731e-b27f-c4e7ca7b0d9d	运维管理员	ROLE_DEV_OPS	t	0	t	\N	\N	2026-01-21 16:31:12.602568+08	\N	2026-01-21 16:31:12.617556+08	\N	0
019bdfad-df4b-7c16-9f95-a68f9a38a51b	系统管理员	ROLE_ADMIN_SYSTEM	t	0	t	\N	\N	2026-01-21 16:31:12.715561+08	\N	2026-01-21 16:31:12.715561+08	\N	0
019bdfad-df4d-7133-b6db-199c0e86b72b	用户	ROLE_USER	t	0	t	\N	\N	2026-01-21 16:31:12.717566+08	\N	2026-01-21 16:31:12.717566+08	\N	0
019bdfad-df4f-7110-b2ce-9eecc82bf46b	审计员	ROLE_AUDIT	t	0	t	\N	\N	2026-01-21 16:31:12.719561+08	\N	2026-01-21 16:31:12.719561+08	\N	0
\.


--
-- Data for Name: sys_role_data_scope; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_role_data_scope (id, role_id, scope_type, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
\.


--
-- Data for Name: sys_role_data_scope_target; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_role_data_scope_target (id, role_id, target_id, target_type, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
\.


--
-- Data for Name: sys_user; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_user (id, username, avatar, status, real_name, gender, birthday, phone, email, country, city, language, timezone, organization_id, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	运维人员	\N	0	运维人员	1	2026-01-21	13312344321	devops@devops00.com	中国	昆明	zh-CN	Asia/Shanghai	019bdfdd-ee9a-7581-ab10-61319ec0753a	\N	2026-01-21 16:22:29.631599+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:46:19.690388+08	\N	2
019bdfec-3202-764d-a1fc-d44c05c72db2	系统管理员	\N	0	系统管理员	1	2026-01-21	13312344321	admin@devops00.com	China	Kunming	zh-CN	Asia/Shanghai	019bdfdd-b58d-7232-943f-af4141801ae3	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:17.1221+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-22 10:51:04.435557+08	\N	1
019bdfed-7ef5-7026-a8f0-c0c987653bee	审计用户	\N	0	审计用户	1	2026-01-21	13312344321	audit@devops00.com	China	Kunming	zh-CN	Asia/Shanghai	019bdfdd-b58d-7232-943f-af4141801ae3	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:40:42.357797+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-22 10:51:07.595555+08	\N	1
019bdfec-d334-7278-985c-44cfaa9b1a68	普通用户	\N	0	普通用户	1	2026-01-21	13312344321	user@devops00.com	China	Kunming	zh-CN	Asia/Shanghai	019bdfde-1c33-77d0-a4a7-4369459486c4	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:58.388475+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-22 10:54:20.724+08	\N	1
\.


--
-- Data for Name: sys_user_data_scope; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_user_data_scope (id, user_id, scope_type, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019bdfea-afd6-7cd6-a6dc-17ff85687310	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	0	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:37:38.26379+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:46:19.70146+08	\N	1
019bdfec-3278-74f7-82ef-eefeb09c1378	019bdfec-3202-764d-a1fc-d44c05c72db2	3	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:17.240102+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-22 10:51:04.462513+08	\N	1
019bdfed-7f6d-7809-9995-be35b5945890	019bdfed-7ef5-7026-a8f0-c0c987653bee	3	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:40:42.477795+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-22 10:51:07.608557+08	\N	1
019bdfec-d3a3-7abc-9df3-17547746e4f0	019bdfec-d334-7278-985c-44cfaa9b1a68	4	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-21 17:39:58.500474+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-22 10:54:20.848036+08	\N	1
\.


--
-- Data for Name: sys_user_data_scope_target; Type: TABLE DATA; Schema: domain_core; Owner: -
--

COPY domain_core.sys_user_data_scope_target (id, user_id, target_id, target_type, created_by, created_at, updated_by, updated_at, deleted, version) FROM stdin;
019be39f-d2e5-7002-9a8f-83d9f544e9f6	019bdfec-d334-7278-985c-44cfaa9b1a68	019bdfde-1c33-77d0-a4a7-4369459486c4	0	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-22 10:54:20.902+08	019bdfa5-e3fc-7ec8-b49f-b2738e64ff21	2026-01-22 10:54:20.902+08	\N	0
\.


--
-- PostgreSQL database dump complete
--

\unrestrict Mb40bdRn1ZjjWpanZgbDbMqI3WpsfwISTfisoxRhG3MlAgtCbGhdnPMkmue4aCp

