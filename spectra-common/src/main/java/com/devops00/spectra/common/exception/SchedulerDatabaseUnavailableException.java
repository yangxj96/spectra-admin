package com.devops00.spectra.common.exception;

/** 调度 PostgreSQL 不可用时的 fail-closed 异常。 */
public final class SchedulerDatabaseUnavailableException extends DataException {

    public static final String CODE = "SCHEDULER_DATABASE_UNAVAILABLE";

    public SchedulerDatabaseUnavailableException(Throwable cause) {
        super(CODE, cause);
    }
}
