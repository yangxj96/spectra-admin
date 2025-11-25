package io.github.yangxj96.spectra.common.exception;


/**
 * 未实现错误
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/11/25 14:55
 */
public class NotImplementedException extends SpectraException {

    public NotImplementedException() {
        super();
    }

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplementedException(Throwable cause) {
        super(cause);
    }

    protected NotImplementedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
