-- Operation log explanations may include a resolved SpEL description and an
-- exception summary. Keep the complete diagnostic context instead of
-- truncating it in the listener or failing the original request's log write.

ALTER TABLE spectra_core.sys_log
    ALTER COLUMN explain TYPE TEXT;

COMMENT ON COLUMN spectra_core.sys_log.explain IS '日志说明（文本）';
