package com.yangxj96.spectra.framework.advice.exception;

import com.yangxj96.spectra.common.response.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL相关错误处理
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@Order(Integer.MIN_VALUE)
@RestControllerAdvice
public class SqlExceptionAdvice {

    private static final String PREFIX = "[SQL相关错误处理]:";


    /**
     * SQL语法错误
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常响应返回
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public R<Object> duplicateKeyException(DuplicateKeyException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        String message = e.getMessage();
        Pattern pattern = Pattern.compile("键值\"\\(name\\)=\\((?<value>[^)]+)\\)\" 已经存在");
        Matcher matcher = pattern.matcher(message);
        String errorMessage = "数据重复，请检查输入内容";
        if (matcher.find()) {
            String value = matcher.group("value");

            return R.failure("\"%s\"已存在,请更换名称".formatted(value));
        }
        // 记录日志（这里假设你有 log 对象）
        log.atError().log(PREFIX + errorMessage + ", detail: {}", message, e);
        return R.failure(errorMessage);
    }

    /**
     * SQL语法错误
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常响应返回
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public R<Object> badSqlGrammarException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log(PREFIX + "SQL语法错误,{}", e.getMessage(), e);
        return R.failure();
    }

    /**
     * SQL异常保底
     *
     * @param e        异常
     * @param response 响应
     * @return 返回保底处理
     */
    @ExceptionHandler(SQLException.class)
    public R<Object> sqlException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log(PREFIX + "SQL错误保底捕获,{}", e.getMessage(), e);
        return R.failure();
    }

}
