package com.devops00.spectra.security.base.exception;


/**
 * 登录异常
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/2/19 23:14
 */
public class LoginException extends RuntimeException {

    public LoginException(String message) {
        super(message);
    }
}
