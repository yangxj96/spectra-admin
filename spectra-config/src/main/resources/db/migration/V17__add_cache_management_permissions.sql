/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

-- 缓存监控、普通缓存清理和安全运行态维护的系统权限种子。
INSERT INTO spectra_security.sec_permission
    (id, code, name, resource_code, action_code, allowed_scope_modes, state, system_managed,
     created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('a2b17410-2ca7-43a7-8c31-1d31f00a0001', 'system:cache:read', '缓存监控查看',
     'system:cache', 'read', 'NONE', 'ACTIVE', true,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0),
    ('a2b17410-2ca7-43a7-8c31-1d31f00a0002', 'system:cache:clear', '普通缓存清理',
     'system:cache', 'clear', 'NONE', 'ACTIVE', true,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0),
    ('a2b17410-2ca7-43a7-8c31-1d31f00a0003', 'security:verification:manage', '验证码状态维护',
     'security:verification', 'manage', 'NONE', 'ACTIVE', true,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0),
    ('a2b17410-2ca7-43a7-8c31-1d31f00a0004', 'security:login-failure:manage', '登录失败锁定维护',
     'security:login-failure', 'manage', 'NONE', 'ACTIVE', true,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0),
    ('a2b17410-2ca7-43a7-8c31-1d31f00a0005', 'security:replay:manage', 'Web 防重放维护',
     'security:replay', 'manage', 'NONE', 'ACTIVE', true,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0);
