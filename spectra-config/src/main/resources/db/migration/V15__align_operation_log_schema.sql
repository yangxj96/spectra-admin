-- Align the persisted operation log table with the current OperationLog entity.
--
-- V1 created the legacy log shape (action/request_params/response_result/
-- duration). The application now writes the structured log shape below.
-- Legacy columns are intentionally retained so existing log data remains
-- recoverable; new writes use only the current entity columns.

ALTER TABLE spectra_core.sys_log
    ADD COLUMN IF NOT EXISTS type       INTEGER,
    ADD COLUMN IF NOT EXISTS explain    VARCHAR(500),
    ADD COLUMN IF NOT EXISTS status     SMALLINT,
    ADD COLUMN IF NOT EXISTS method     VARCHAR(32),
    ADD COLUMN IF NOT EXISTS url        VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS args       JSONB,
    ADD COLUMN IF NOT EXISTS result     JSONB,
    ADD COLUMN IF NOT EXISTS time_cost  BIGINT;

COMMENT ON COLUMN spectra_core.sys_log.type IS '日志类型：0 常规，1 安全，2 系统异常，3 自动化';
COMMENT ON COLUMN spectra_core.sys_log.explain IS '日志说明';
COMMENT ON COLUMN spectra_core.sys_log.status IS '请求响应状态码';
COMMENT ON COLUMN spectra_core.sys_log.method IS '请求方法';
COMMENT ON COLUMN spectra_core.sys_log.url IS '请求 URL';
COMMENT ON COLUMN spectra_core.sys_log.args IS '脱敏后的请求参数 JSON';
COMMENT ON COLUMN spectra_core.sys_log.result IS '脱敏后的响应结果 JSON';
COMMENT ON COLUMN spectra_core.sys_log.time_cost IS '请求耗时（毫秒）';
