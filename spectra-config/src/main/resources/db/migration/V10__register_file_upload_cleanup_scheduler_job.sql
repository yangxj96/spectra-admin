/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

-- 文件上传清理由统一调度内核驱动，不再使用业务 Spring 定时注解。
INSERT INTO spectra_core.scheduler_job (
    id, job_key, name, module, description, handler_key, job_type, run_scope, definition_status, desired_state,
    schedule_kind, cron_expression, fixed_delay_ms, initial_delay_ms, next_fire_at, misfire_policy, concurrency_policy,
    execution_policy, parameters, revision, created_at, updated_at, version
) VALUES
    ('00000000-0000-0000-0000-000000000406', 'file.upload.cleanup', '文件上传清理', 'upload',
     '扫描并清理过期上传会话、临时对象和无引用文件资产。', 'file.upload.cleanup',
     'SYSTEM', 'SINGLETON', 'REGISTERED', 'ENABLED', 'FIXED_DELAY', NULL, 300000, 0, NULL, 'FIRE_ONCE', 'FORBID',
     '{"timeoutMs":300000,"leaseDurationMs":600000,"maxAttempts":1}'::jsonb, '{}'::jsonb, 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
