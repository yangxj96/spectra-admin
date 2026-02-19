package io.github.yangxj96.spectra.security.starter.advice;

import io.github.yangxj96.spectra.common.constant.LogPrefix;
import io.github.yangxj96.spectra.common.response.R;
import io.github.yangxj96.spectra.security.base.exception.LoginException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/// 业务异常拦截
@Slf4j
@Order(0)
@RestControllerAdvice
public class LoginExceptionAdvice {

    @ExceptionHandler(LoginException.class)
    public R<Object> handleLoginException(LoginException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        log.warn("{}登录失败: {}", LogPrefix.SECURITY.p(), e.getMessage(), e);
        return R.failure(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}
