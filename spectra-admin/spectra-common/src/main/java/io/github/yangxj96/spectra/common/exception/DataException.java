package io.github.yangxj96.spectra.common.exception;

/**
 * 系统数据异常
 */
public class DataException extends SpectraException {

    public DataException() {
        super("系统数据异常");
    }

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataException(Throwable cause) {
        super(cause);
    }

    protected DataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
