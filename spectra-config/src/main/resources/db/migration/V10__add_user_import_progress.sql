ALTER TABLE spectra_core.sys_user_import_task
    ADD COLUMN completed_rows INTEGER NOT NULL DEFAULT 0;

ALTER TABLE spectra_core.sys_user_import_task
    ADD CONSTRAINT ck_sys_user_import_task_completed_rows CHECK (completed_rows >= 0);
