-- 角色名称在角色目录中必须唯一，服务层校验用于提供友好提示，数据库约束用于兜底并发写入。
ALTER TABLE spectra_security.sec_role
    ADD CONSTRAINT uk_sec_role_name UNIQUE (name);
