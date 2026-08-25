/*
 * 将用户登录用户名与联系方式从 sys_user 中拆分。
 *
 * username 承接原 sys_user.email 的登录用途；手机号和邮箱作为认证/通知联系方式
 * 迁移到 sec_user_contact，并同步建立 SMS/EMAIL 认证身份。旧列在本迁移中直接删除，
 * 不保留兼容读取路径。
 */

ALTER TABLE spectra_core.sys_user
    ADD COLUMN username varchar(100);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM spectra_core.sys_user
        WHERE email IS NULL OR btrim(email) = ''
    ) THEN
        RAISE EXCEPTION 'sys_user.email 存在空值，无法迁移为登录用户名';
    END IF;

    IF EXISTS (
        SELECT lower(btrim(email))
        FROM spectra_core.sys_user
        WHERE deleted IS NULL
        GROUP BY lower(btrim(email))
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'sys_user.email 存在重复值，无法迁移为登录用户名';
    END IF;

    IF EXISTS (
        SELECT lower(btrim(phone))
        FROM spectra_core.sys_user
        WHERE deleted IS NULL
          AND phone IS NOT NULL
          AND btrim(phone) <> ''
        GROUP BY lower(btrim(phone))
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'sys_user.phone 存在重复值，无法迁移为联系方式';
    END IF;
END $$;

UPDATE spectra_core.sys_user
SET username = btrim(email);

ALTER TABLE spectra_core.sys_user
    ALTER COLUMN username SET NOT NULL,
    ADD CONSTRAINT ck_sys_user_username_not_blank CHECK (btrim(username) <> '');

COMMENT ON COLUMN spectra_core.sys_user.username IS '独立登录用户名；由原邮箱迁移而来，大小写不敏感且唯一';

CREATE UNIQUE INDEX uk_sys_user_username
    ON spectra_core.sys_user (lower(btrim(username)))
    WHERE deleted IS NULL;

CREATE TABLE spectra_security.sec_user_contact (
    id uuid DEFAULT uuidv7() NOT NULL,
    user_id uuid NOT NULL,
    contact_type varchar(16) NOT NULL,
    contact_value varchar(255) NOT NULL,
    state varchar(16) DEFAULT 'ACTIVE' NOT NULL,
    verified_at timestamp(6) with time zone,
    created_by uuid,
    created_at timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by uuid,
    updated_at timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted timestamp(6) with time zone,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT pk_sec_user_contact PRIMARY KEY (id),
    CONSTRAINT fk_sec_user_contact_user_id FOREIGN KEY (user_id)
        REFERENCES spectra_core.sys_user(id) ON DELETE RESTRICT,
    CONSTRAINT ck_sec_user_contact_type CHECK (contact_type IN ('PHONE', 'EMAIL')),
    CONSTRAINT ck_sec_user_contact_state CHECK (state IN ('ACTIVE', 'DISABLED', 'REVOKED')),
    CONSTRAINT ck_sec_user_contact_value_not_blank CHECK (btrim(contact_value) <> '')
);

COMMENT ON TABLE spectra_security.sec_user_contact IS '用户认证与通知联系方式';
COMMENT ON COLUMN spectra_security.sec_user_contact.user_id IS '用户 ID';
COMMENT ON COLUMN spectra_security.sec_user_contact.contact_type IS '联系方式类型：PHONE/EMAIL';
COMMENT ON COLUMN spectra_security.sec_user_contact.contact_value IS '联系方式原始值，仅限安全域服务读取';
COMMENT ON COLUMN spectra_security.sec_user_contact.state IS '联系方式状态：ACTIVE/DISABLED/REVOKED';
COMMENT ON COLUMN spectra_security.sec_user_contact.verified_at IS '验证时间';

INSERT INTO spectra_security.sec_user_contact
        (user_id, contact_type, contact_value, state, verified_at, created_at, updated_at)
SELECT id, 'PHONE', btrim(phone), 'ACTIVE', created_at, created_at, updated_at
FROM spectra_core.sys_user
WHERE deleted IS NULL
  AND phone IS NOT NULL
  AND btrim(phone) <> '';

INSERT INTO spectra_security.sec_user_contact
        (user_id, contact_type, contact_value, state, verified_at, created_at, updated_at)
SELECT id, 'EMAIL', lower(btrim(email)), 'ACTIVE', created_at, created_at, updated_at
FROM spectra_core.sys_user
WHERE deleted IS NULL
  AND email IS NOT NULL
  AND btrim(email) <> '';

CREATE UNIQUE INDEX uk_sec_user_contact_user_type_active
    ON spectra_security.sec_user_contact (user_id, contact_type)
    WHERE deleted IS NULL AND state = 'ACTIVE';

CREATE UNIQUE INDEX uk_sec_user_contact_value_active
    ON spectra_security.sec_user_contact (contact_type, lower(btrim(contact_value)))
    WHERE deleted IS NULL AND state = 'ACTIVE';

CREATE INDEX idx_sec_user_contact_user_state
    ON spectra_security.sec_user_contact (user_id, state)
    WHERE deleted IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM spectra_core.sys_user u
        JOIN spectra_security.sec_authentication_identity i
          ON i.method_code = 'SMS'
         AND i.provider_code = 'LOCAL'
         AND i.identifier_hash = encode(public.digest(lower(btrim(u.phone)), 'sha256'), 'hex')
         AND i.state = 'ACTIVE'
         AND i.deleted IS NULL
        WHERE u.deleted IS NULL
          AND u.phone IS NOT NULL
          AND btrim(u.phone) <> ''
          AND i.user_id <> u.id
    ) THEN
        RAISE EXCEPTION 'sys_user.phone 与现有 SMS 认证身份冲突';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM spectra_core.sys_user u
        JOIN spectra_security.sec_authentication_identity i
          ON i.method_code = 'EMAIL'
         AND i.provider_code = 'LOCAL'
         AND i.identifier_hash = encode(public.digest(lower(btrim(u.email)), 'sha256'), 'hex')
         AND i.state = 'ACTIVE'
         AND i.deleted IS NULL
        WHERE u.deleted IS NULL
          AND u.email IS NOT NULL
          AND btrim(u.email) <> ''
          AND i.user_id <> u.id
    ) THEN
        RAISE EXCEPTION 'sys_user.email 与现有 EMAIL 认证身份冲突';
    END IF;
END $$;

INSERT INTO spectra_security.sec_authentication_identity
        (user_id, method_code, provider_code, identifier_hash, state, verified_at, created_at, updated_at)
SELECT id, 'SMS', 'LOCAL', encode(public.digest(lower(btrim(phone)), 'sha256'), 'hex'),
       'ACTIVE', created_at, created_at, updated_at
FROM spectra_core.sys_user
WHERE deleted IS NULL
  AND phone IS NOT NULL
  AND btrim(phone) <> ''
ON CONFLICT (method_code, provider_code, identifier_hash) DO NOTHING;

INSERT INTO spectra_security.sec_authentication_identity
        (user_id, method_code, provider_code, identifier_hash, state, verified_at, created_at, updated_at)
SELECT id, 'EMAIL', 'LOCAL', encode(public.digest(lower(btrim(email)), 'sha256'), 'hex'),
       'ACTIVE', created_at, created_at, updated_at
FROM spectra_core.sys_user
WHERE deleted IS NULL
  AND email IS NOT NULL
  AND btrim(email) <> ''
ON CONFLICT (method_code, provider_code, identifier_hash) DO NOTHING;

ALTER TABLE spectra_core.sys_user
    DROP COLUMN phone,
    DROP COLUMN email;
