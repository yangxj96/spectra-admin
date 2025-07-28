package com.yangxj96.spectra.framework.advice.exception;

import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.yangxj96.spectra.common.annotation.ULog;
import com.yangxj96.spectra.common.response.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.LoginException;

/**
 * 权限错误相关处理
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@RestControllerAdvice
public class AuthExceptionAdvice {

    private static final String PREFIX = "[权限错误相关处理]:";


    /**
     * 无权限异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常响应返回
     */
    @ULog("无权操作")
    @ExceptionHandler(NotPermissionException.class)
    public R<Object> notPermissionException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        log.atError().log(PREFIX + "无权限异常,{}", e.getMessage(), e);
        return R.failure(HttpStatus.FORBIDDEN, "无权操作");
    }

    /**
     * 未登录异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常响应返回
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Object> notLoginException(NotLoginException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        log.atError().log(PREFIX + "未登录异常,{}", e.getMessage(), e);
        if (e.getCode() == SaErrorCode.CODE_11016) {
            return R.failure(HttpStatus.UNAUTHORIZED, "您的会话已过期，请重新登录以继续。");
        }
        return R.failure(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    /**
     * 登录异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常响应返回
     */
    @ExceptionHandler(LoginException.class)
    public R<Object> loginException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log(PREFIX + "登录异常,{}", e.getMessage(), e);
        return R.failure(e.getMessage());
    }

}
