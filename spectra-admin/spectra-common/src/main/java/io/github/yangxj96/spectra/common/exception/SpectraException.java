package io.github.yangxj96.spectra.common.exception;

import java.io.Serial;

/**
 * 光谱平台基础异常
 */
public class SpectraException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SpectraException() {
        super();
    }

    public SpectraException(String message) {
        super(message);
    }

    public SpectraException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpectraException(Throwable cause) {
        super(cause);
    }

    protected SpectraException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
