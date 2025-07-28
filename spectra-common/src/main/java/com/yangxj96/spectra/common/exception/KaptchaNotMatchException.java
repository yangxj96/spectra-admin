package com.yangxj96.spectra.common.exception;

/**
 * 验证码不匹配
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
public class KaptchaNotMatchException extends RuntimeException {

    public KaptchaNotMatchException() {
        super();
    }

    public KaptchaNotMatchException(String message) {
        super(message);
    }

}
