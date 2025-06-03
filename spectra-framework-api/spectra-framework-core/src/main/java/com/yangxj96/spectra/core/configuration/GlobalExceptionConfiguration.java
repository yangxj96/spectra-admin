package com.yangxj96.spectra.core.configuration;

import com.yangxj96.spectra.core.exception.DataExistException;
import com.yangxj96.spectra.core.exception.DataNotExistException;
import com.yangxj96.spectra.core.response.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.LoginException;

/**
 * 全局异常处理
 *
 * @author 杨新杰
 * @since 2025/5/26 17:07
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionConfiguration {


    /**
     * 登录异常
     */
    @ExceptionHandler(LoginException.class)
    public R<Object> loginException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log("登录异常,{}", e.getMessage(), e);
        return R.failure(e.getMessage());
    }


    /**
     * 数据已存在异常
     */
    @ExceptionHandler(NotImplementedException.class)
    public R<Object> notImplementedException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log("未进行功能实现异常,{}", e.getMessage(), e);
        return R.failure();
    }

    /**
     * 数据已存在异常
     */
    @ExceptionHandler(DataExistException.class)
    public R<Object> dataExistException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.CONFLICT.value());
        log.atError().log("数据已存在异常,{}", e.getMessage(), e);
        return R.failure(HttpStatus.CONFLICT);
    }

    /**
     * 数据不存在异常
     */
    @ExceptionHandler(DataNotExistException.class)
    public R<Object> dataNotExistException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.NO_CONTENT.value());
        log.atError().log("数据不存在异常,{} ", e.getMessage(), e);
        return R.failure(HttpStatus.NO_CONTENT);
    }

    /**
     * 参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Object> methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletResponse response) {
        log.atError().log("参数验证异常,{} ", e.getMessage(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        var errors = e.getBindingResult().getAllErrors();
        if (!errors.isEmpty()) {
            return R.failure(HttpStatus.BAD_REQUEST, errors.get(0).getDefaultMessage());
        } else {
            return R.failure(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public R<Object> handleException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log("兜底异常处理,{}", e.getMessage(), e);
        return R.failure();
    }

}
