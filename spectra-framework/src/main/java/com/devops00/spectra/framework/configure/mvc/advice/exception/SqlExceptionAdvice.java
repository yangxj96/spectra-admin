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

package com.devops00.spectra.framework.configure.mvc.advice.exception;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.response.R;
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
import java.util.Objects;
import java.util.regex.Pattern;

/// SQL相关错误处理
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/7/28 00:00
@Slf4j
@NullMarked
@Order(-100)
@RestControllerAdvice
public class SqlExceptionAdvice {

    /// 处理唯一键冲突异常
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
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

    /// 处理 SQL 语法错误
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
    @ExceptionHandler(BadSqlGrammarException.class)
    public R<Object> handleBadSqlGrammarException(BadSqlGrammarException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}SQL语法错误: SQL=[{}], 错误=[{}]", LogPrefix.PERSISTENCE.p(), e.getSql(), e.getMessage(), e);
        return R.failure("请求的数据查询异常,请联系管理员");
    }

    /// 处理其他数据完整性违规（如外键、非空等）
    @ExceptionHandler(DataIntegrityViolationException.class)
    public R<Object> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error("{}数据完整性违规: {}", LogPrefix.PERSISTENCE.p(), e.getMessage(), e);
        return R.failure("数据不符合规则,请检查输入内容");
    }

    /// 处理未分类的 JDBC 异常（如连接问题、驱动错误等）
    @ExceptionHandler(UncategorizedSQLException.class)
    public R<Object> handleUncategorizedSQLException(UncategorizedSQLException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error(
                "{}未分类SQL异常: SQL状态=[{}], 错误码=[{}], 原因=[{}]",
                LogPrefix.PERSISTENCE.p(),
                Objects.requireNonNull(e.getSQLException()).getSQLState(),
                e.getSQLException().getErrorCode(),
                e.getSQLException().getMessage(),
                e
        );
        return R.failure("数据库操作异常,请稍后重试");
    }


    /// 【兜底】捕获所有Spring数据访问异常（包括未显式处理的）
    @ExceptionHandler(DataAccessException.class)
    public R<Object> handleDataAccessException(DataAccessException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error("{}数据访问异常（兜底）: {}", LogPrefix.PERSISTENCE.p(), e.getMessage(), e);
        return R.failure("系统内部错误,请联系管理员");
    }

    /// 【兜底】捕获原始SQLException（如未通过Spring异常翻译的场景）
    ///
    /// 注意：大多数情况下,Spring会将SQLException翻译为DataAccessException
    @ExceptionHandler(SQLException.class)
    public R<Object> handleSQLException(SQLException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error(
                "{}原始SQLException: SQL状态=[{}], 错误码=[{}], 信息=[{}]",
                LogPrefix.PERSISTENCE.p(),
                e.getSQLState(),
                e.getErrorCode(),
                e.getMessage(), e
        );
        return R.failure("数据库操作失败,请稍后重试");
    }

}
