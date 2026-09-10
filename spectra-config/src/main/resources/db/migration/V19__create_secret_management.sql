/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

-- 密钥定义和密钥版本使用独立安全 Schema；数据库只保存密文、随机数和摘要，不保存任何明文。
CREATE TABLE spectra_security.sec_secret_definition (
    id uuid DEFAULT uuidv7() CONSTRAINT __canonical_sec_secret_definition_id_not_null NOT NULL,
    code character varying(160) CONSTRAINT __canonical_sec_secret_definition_code_not_null NOT NULL,
    name character varying(200) CONSTRAINT __canonical_sec_secret_definition_name_not_null NOT NULL,
    category character varying(40) CONSTRAINT __canonical_sec_secret_definition_category_not_null NOT NULL,
    value_type character varying(40) CONSTRAINT __canonical_sec_secret_definition_value_type_not_null NOT NULL,
    owner_module character varying(100) CONSTRAINT __canonical_sec_secret_definition_owner_module_not_null NOT NULL,
    description character varying(500),
    mutable boolean DEFAULT true CONSTRAINT __canonical_sec_secret_definition_mutable_not_null NOT NULL,
    hot_reload boolean DEFAULT true CONSTRAINT __canonical_sec_secret_definition_hot_reload_not_null NOT NULL,
    exportable boolean DEFAULT true CONSTRAINT __canonical_sec_secret_definition_exportable_not_null NOT NULL,
    created_by uuid,
    created_at timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP CONSTRAINT __canonical_sec_secret_definition_created_at_not_null NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP CONSTRAINT __canonical_sec_secret_definition_updated_at_not_null NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0 CONSTRAINT __canonical_sec_secret_definition_version_not_null NOT NULL,
    CONSTRAINT pk_sec_secret_definition PRIMARY KEY (id),
    CONSTRAINT ck_sec_secret_definition_code CHECK ((code)::text ~ '^[a-z][a-z0-9.-]{2,159}$'::text),
    CONSTRAINT ck_sec_secret_definition_category CHECK ((category)::text = ANY (ARRAY['APPLICATION_CRYPTO'::character varying, 'BUSINESS_CREDENTIAL'::character varying, 'SECURITY_SIGNING'::character varying]::text[])),
    CONSTRAINT ck_sec_secret_definition_value_type CHECK ((value_type)::text = ANY (ARRAY['TEXT'::character varying, 'BASE64'::character varying, 'PEM'::character varying, 'JSON'::character varying]::text[]))
);

COMMENT ON TABLE spectra_security.sec_secret_definition IS '密钥定义元数据表，不保存密钥明文';
COMMENT ON COLUMN spectra_security.sec_secret_definition.id IS '主键ID，UUID v7';
COMMENT ON COLUMN spectra_security.sec_secret_definition.code IS '后端注册的稳定密钥编码';
COMMENT ON COLUMN spectra_security.sec_secret_definition.name IS '密钥显示名称';
COMMENT ON COLUMN spectra_security.sec_secret_definition.category IS '密钥分类：应用加密、业务服务凭据或安全签名';
COMMENT ON COLUMN spectra_security.sec_secret_definition.value_type IS '密钥值类型：TEXT、BASE64、PEM 或 JSON';
COMMENT ON COLUMN spectra_security.sec_secret_definition.owner_module IS '所属模块';
COMMENT ON COLUMN spectra_security.sec_secret_definition.description IS '密钥用途说明，不得包含密钥值';
COMMENT ON COLUMN spectra_security.sec_secret_definition.mutable IS '是否允许创建新版本';
COMMENT ON COLUMN spectra_security.sec_secret_definition.hot_reload IS '发布新版本后是否支持运行时刷新';
COMMENT ON COLUMN spectra_security.sec_secret_definition.exportable IS '是否允许纳入受保护导出包';
COMMENT ON COLUMN spectra_security.sec_secret_definition.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_secret_definition.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_secret_definition.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_secret_definition.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_secret_definition.deleted IS '删除时间，NULL表示未删除';
COMMENT ON COLUMN spectra_security.sec_secret_definition.version IS '乐观锁版本号，默认0';

CREATE TABLE spectra_security.sec_secret_version (
    id uuid DEFAULT uuidv7() CONSTRAINT __canonical_sec_secret_version_id_not_null NOT NULL,
    secret_definition_id uuid CONSTRAINT __canonical_sec_secret_version_definition_id_not_null NOT NULL,
    version_no integer CONSTRAINT __canonical_sec_secret_version_no_not_null NOT NULL,
    state character varying(20) DEFAULT 'PENDING'::character varying CONSTRAINT __canonical_sec_secret_version_state_not_null NOT NULL,
    cipher_algorithm character varying(40),
    nonce bytea,
    ciphertext bytea,
    fingerprint character varying(64) CONSTRAINT __canonical_sec_secret_version_fingerprint_not_null NOT NULL,
    source character varying(20) DEFAULT 'MANUAL'::character varying CONSTRAINT __canonical_sec_secret_version_source_not_null NOT NULL,
    effective_at timestamp(6) with time zone,
    retired_at timestamp(6) with time zone,
    created_by uuid,
    created_at timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP CONSTRAINT __canonical_sec_secret_version_created_at_not_null NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP CONSTRAINT __canonical_sec_secret_version_updated_at_not_null NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0 CONSTRAINT __canonical_sec_secret_version_version_not_null NOT NULL,
    CONSTRAINT pk_sec_secret_version PRIMARY KEY (id),
    CONSTRAINT fk_sec_secret_version_definition FOREIGN KEY (secret_definition_id) REFERENCES spectra_security.sec_secret_definition(id),
    CONSTRAINT ck_sec_secret_version_no CHECK ((version_no > 0)),
    CONSTRAINT ck_sec_secret_version_state CHECK ((state)::text = ANY (ARRAY['PENDING'::character varying, 'ACTIVE'::character varying, 'RETIRED'::character varying, 'REVOKED'::character varying, 'DESTROYED'::character varying]::text[])),
    CONSTRAINT ck_sec_secret_version_source CHECK ((source)::text = ANY (ARRAY['MANUAL'::character varying, 'IMPORT'::character varying, 'MIGRATION'::character varying, 'GENERATED'::character varying]::text[])),
    CONSTRAINT ck_sec_secret_version_ciphertext CHECK (state = 'DESTROYED' OR (cipher_algorithm IS NOT NULL AND nonce IS NOT NULL AND ciphertext IS NOT NULL)),
    CONSTRAINT ck_sec_secret_version_destroyed_payload CHECK (state <> 'DESTROYED' OR (cipher_algorithm IS NULL AND nonce IS NULL AND ciphertext IS NULL))
);

COMMENT ON TABLE spectra_security.sec_secret_version IS '密钥版本表，只保存根密钥加密后的密文';
COMMENT ON COLUMN spectra_security.sec_secret_version.id IS '主键ID，UUID v7';
COMMENT ON COLUMN spectra_security.sec_secret_version.secret_definition_id IS '密钥定义ID';
COMMENT ON COLUMN spectra_security.sec_secret_version.version_no IS '同一密钥定义下的业务版本号';
COMMENT ON COLUMN spectra_security.sec_secret_version.state IS '版本状态：PENDING、ACTIVE、RETIRED、REVOKED 或 DESTROYED';
COMMENT ON COLUMN spectra_security.sec_secret_version.cipher_algorithm IS '密文算法，例如AES-256-GCM';
COMMENT ON COLUMN spectra_security.sec_secret_version.nonce IS '加密随机数；销毁后清空';
COMMENT ON COLUMN spectra_security.sec_secret_version.ciphertext IS '根密钥保护后的密文；销毁后清空';
COMMENT ON COLUMN spectra_security.sec_secret_version.fingerprint IS '密钥明文SHA-256摘要，用于识别版本，不可逆';
COMMENT ON COLUMN spectra_security.sec_secret_version.source IS '版本来源：MANUAL、IMPORT、MIGRATION 或 GENERATED';
COMMENT ON COLUMN spectra_security.sec_secret_version.effective_at IS '版本开始生效时间';
COMMENT ON COLUMN spectra_security.sec_secret_version.retired_at IS '版本退役时间';
COMMENT ON COLUMN spectra_security.sec_secret_version.created_by IS '创建人';
COMMENT ON COLUMN spectra_security.sec_secret_version.created_at IS '创建时间';
COMMENT ON COLUMN spectra_security.sec_secret_version.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_security.sec_secret_version.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_security.sec_secret_version.deleted IS '删除时间，NULL表示未删除';
COMMENT ON COLUMN spectra_security.sec_secret_version.version IS '乐观锁版本号，默认0';

CREATE UNIQUE INDEX uk_sec_secret_definition_code
    ON spectra_security.sec_secret_definition (code)
    WHERE deleted IS NULL;
CREATE INDEX idx_sec_secret_definition_category
    ON spectra_security.sec_secret_definition (category, owner_module)
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX uk_sec_secret_version_definition_version
    ON spectra_security.sec_secret_version (secret_definition_id, version_no)
    WHERE deleted IS NULL;
CREATE UNIQUE INDEX uk_sec_secret_version_active
    ON spectra_security.sec_secret_version (secret_definition_id)
    WHERE deleted IS NULL AND state = 'ACTIVE';
CREATE INDEX idx_sec_secret_version_definition_state
    ON spectra_security.sec_secret_version (secret_definition_id, state, version_no DESC)
    WHERE deleted IS NULL;

-- 注册密钥定义只建立可管理的元数据；初始值由生成、导入或一次性迁移流程写入。
INSERT INTO spectra_security.sec_secret_definition
    (id, code, name, category, value_type, owner_module, description, mutable, hot_reload, exportable)
VALUES
    (uuidv7(), 'crypto.server.public-key', '服务端加密公钥', 'APPLICATION_CRYPTO', 'PEM', 'security', '接口加密服务端公钥', true, true, true),
    (uuidv7(), 'crypto.server.private-key', '服务端加密私钥', 'APPLICATION_CRYPTO', 'PEM', 'security', '接口加密服务端私钥', true, true, true),
    (uuidv7(), 'crypto.client.public-key', '客户端加密公钥', 'APPLICATION_CRYPTO', 'PEM', 'security', '接口加密客户端公钥', true, true, true),
    (uuidv7(), 'crypto.client.private-key', '客户端加密私钥', 'APPLICATION_CRYPTO', 'PEM', 'security', '接口加密客户端私钥', true, true, true),
    (uuidv7(), 'notification.address-encryption-key', '通知地址加密密钥', 'APPLICATION_CRYPTO', 'BASE64', 'notification', '通知地址字段加密密钥', true, true, true),
    (uuidv7(), 'notification.sensitive-payload-key', '通知敏感载荷密钥', 'APPLICATION_CRYPTO', 'BASE64', 'notification', '通知敏感载荷保护密钥', true, true, true),
    (uuidv7(), 'notification.provider.sms.secret', '短信服务凭据', 'BUSINESS_CREDENTIAL', 'JSON', 'notification', '短信供应商访问凭据', true, true, true),
    (uuidv7(), 'notification.provider.email.secret', '邮件服务凭据', 'BUSINESS_CREDENTIAL', 'JSON', 'notification', '邮件供应商访问凭据', true, true, true),
    (uuidv7(), 'security.verification-code-hmac', '验证码签名密钥', 'SECURITY_SIGNING', 'BASE64', 'security', '验证码数据签名密钥', true, true, true),
    (uuidv7(), 'security.authorization-change-token-hmac', '授权变更令牌签名密钥', 'SECURITY_SIGNING', 'BASE64', 'security', '授权变更令牌签名密钥', true, true, true),
    (uuidv7(), 'security.request-integrity-hmac', '请求完整性签名密钥', 'SECURITY_SIGNING', 'BASE64', 'security', '请求报文完整性校验密钥', true, true, true),
    (uuidv7(), 'security.csrf-signing-key', '防伪签名密钥', 'SECURITY_SIGNING', 'BASE64', 'security', '防伪令牌签名密钥', true, true, true);

-- 将既有菜单改名为正式的密钥管理菜单；权限判断由新 Controller 强制限定 ROLE_DEV_OPS。
UPDATE spectra_core.sys_menu
SET name = '密钥管理', route_name = 'DevopsSecretManagement'
WHERE route_name = 'DevopsEncryptionKey' AND deleted IS NULL;

-- 密钥管理属于最高权限运维能力：移除其他角色的菜单关系，只保留 ROLE_DEV_OPS。
UPDATE spectra_security.sec_role_menu relation
SET deleted = CURRENT_TIMESTAMP
WHERE relation.menu_id = (
          SELECT menu.id
          FROM spectra_core.sys_menu menu
          WHERE menu.route_name = 'DevopsSecretManagement' AND menu.deleted IS NULL
      )
  AND relation.deleted IS NULL
  AND relation.role_id NOT IN (
      SELECT role.id FROM spectra_security.sec_role role
      WHERE role.code = 'ROLE_DEV_OPS' AND role.deleted IS NULL
  );

INSERT INTO spectra_security.sec_role_menu (id, role_id, menu_id, created_by, created_at, updated_by, updated_at, deleted, version)
SELECT uuidv7(), role.id, menu.id, '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0
FROM spectra_security.sec_role role
JOIN spectra_core.sys_menu menu ON menu.route_name = 'DevopsSecretManagement' AND menu.deleted IS NULL
WHERE role.code = 'ROLE_DEV_OPS' AND role.deleted IS NULL
ON CONFLICT (role_id, menu_id) DO NOTHING;
