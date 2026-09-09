/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.framework.web.advice.exception;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.SchedulerDatabaseUnavailableException;
import com.devops00.spectra.framework.web.response.R;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * SQL相关错误处理
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/7/28 00:00
 */
@Slf4j
@NullMarked
@Order(-100)
@RestControllerAdvice
public class SqlExceptionAdvice {

    /**
     * 处理唯一键冲突异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回数据重复统一失败响应；响应对象始终非 null且不向客户端暴露数据库原始信息。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public R<Object> handleDuplicateKeyException(DuplicateKeyException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        var message = e.getMessage();
        var pattern = Pattern.compile("键值\"\\(name\\)=\\((?<value>[^)]+)\\)\" 已经存在");
        var matcher = pattern.matcher(message);
        var errorMessage = "数据重复,请检查输入内容";
        if (matcher.find()) {
            var value = matcher.group("value");

            return R.failure("\"%s\"已存在,请更换名称".formatted(value));
        }
        // 记录日志（这里假设你有 log 对象）
        log.error("{}{}, detail: {}", LogPrefix.PERSISTENCE.p(), errorMessage, message, e);
        return R.failure(errorMessage);
    }

    /**
     * 处理 SQL 语法错误
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 SQL 查询失败统一失败响应；响应对象始终非 null且不向客户端暴露 SQL 文本。
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public R<Object> handleBadSqlGrammarException(BadSqlGrammarException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}SQL语法错误: SQL=[{}], 错误=[{}]", LogPrefix.PERSISTENCE.p(), e.getSql(), e.getMessage(), e);
        return R.failure("请求的数据查询异常,请联系管理员");
    }

    /**
     * 处理其他数据完整性违规（如外键、非空等）
     *
     * @param e        待转换为统一响应的异常对象。
     * @param response 当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @return 返回数据完整性校验失败统一响应；响应对象始终非 null且不暴露约束细节。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public R<Object> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error("{}数据完整性违规: {}", LogPrefix.PERSISTENCE.p(), e.getMessage(), e);
        return R.failure("数据不符合规则,请检查输入内容");
    }

    /**
     * 处理未分类的 JDBC 异常（如连接问题、驱动错误等）
     *
     * @param e        待转换为统一响应的异常对象。
     * @param response 当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @return 返回数据库操作异常统一响应；响应对象始终非 null且不暴露 SQL 状态或错误码。
     */
    @ExceptionHandler(UncategorizedSQLException.class)
    public R<Object> handleUncategorizedSQLException(UncategorizedSQLException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        var sqlException = e.getSQLException();
        if (sqlException == null) {
            log.error("{}未分类SQL异常: 未提供底层 SQLException", LogPrefix.PERSISTENCE.p(), e);
            return R.failure("数据库操作异常,请稍后重试");
        }
        log.error("{}未分类SQL异常: SQL状态=[{}], 错误码=[{}], 原因=[{}]", LogPrefix.PERSISTENCE.p(), sqlException.getSQLState(),
                sqlException.getErrorCode(), sqlException.getMessage(), e);
        return R.failure("数据库操作异常,请稍后重试");
    }

    /**
     * 【兜底】捕获所有Spring数据访问异常（包括未显式处理的）
     *
     * @param e        待转换为统一响应的异常对象。
     * @param response 当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @return 返回 Spring 数据访问异常统一响应；响应对象始终非 null且不暴露底层异常细节。
     */
    @ExceptionHandler(DataAccessException.class)
    public R<Object> handleDataAccessException(DataAccessException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error("{}数据访问异常（兜底）: {}", LogPrefix.PERSISTENCE.p(), e.getMessage(), e);
        return R.failure("系统内部错误,请联系管理员");
    }

    /**
     * 调度内核数据库不可用时必须明确返回 503，不能被普通 SQL 兜底吞掉。
     *
     * @param e        待转换为统一响应的异常对象。
     * @param response 当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @return 返回 HTTP 503 的调度数据库不可用响应，错误码为 {@code SCHEDULER_DATABASE_UNAVAILABLE}；响应对象始终非 null。
     */
    @ExceptionHandler(SchedulerDatabaseUnavailableException.class)
    public R<Object> handleSchedulerDatabaseUnavailable(SchedulerDatabaseUnavailableException e,
                                                        HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        log.warn("{}调度 PostgreSQL 不可用，请求被拒绝: {}", LogPrefix.PERSISTENCE.p(), e.getMessage(), e);
        return R.failure(HttpStatus.SERVICE_UNAVAILABLE, SchedulerDatabaseUnavailableException.CODE);
    }

    /**
     * 安全 Redis 不可用时必须停止 Token、Session、验证码和防重放相关请求，不能返回普通 500 或继续降级。
     *
     * @param e        待转换为统一响应的异常对象。
     * @param response 当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @return 返回 HTTP 503 的安全 Redis 暂不可用响应；响应对象始终非 null且不允许请求继续按普通失败降级。
     */
    @ExceptionHandler(SecurityRedisUnavailableException.class)
    public R<Object> handleSecurityRedisUnavailable(SecurityRedisUnavailableException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        log.warn("{}安全 Redis 不可用，请求被拒绝: {}", LogPrefix.PERSISTENCE.p(), e.getMessage(), e);
        return R.failure(HttpStatus.SERVICE_UNAVAILABLE, "安全会话服务暂不可用");
    }

    /**
     * 【兜底】捕获原始SQLException（如未通过Spring异常翻译的场景）
     * <p>
     * 注意：大多数情况下,Spring会将SQLException翻译为DataAccessException
     *
     * @param e        待转换为统一响应的异常对象。
     * @param response 当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @return 返回数据库操作失败统一响应；响应对象始终非 null且不暴露 SQL 状态、错误码或原始消息。
     */
    @ExceptionHandler(SQLException.class)
    public R<Object> handleSQLException(SQLException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error("{}原始SQLException: SQL状态=[{}], 错误码=[{}], 信息=[{}]", LogPrefix.PERSISTENCE.p(), e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
        return R.failure("数据库操作失败,请稍后重试");
    }
}
