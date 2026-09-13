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

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.SpectraException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * 从业务异常中提取可供审计的失败码、异常类型和安全说明。
 *
 * <p>未知异常和数据库异常使用固定说明，不把底层 SQL、参数或堆栈写入审计事实。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Component
@RequiredArgsConstructor
public class AuditFailureResolver {

    private static final int MAX_REASON_LENGTH = 2000;

    private static final Pattern INLINE_CREDENTIAL = Pattern.compile(
            "(?i)\\b(password|passwd|pwd|access[_-]?token|refresh[_-]?token|token|secret|api[_-]?key|authorization|cookie)\\s*([=:])\\s*(\"[^\"]*\"|'[^']*'|[^\\s,;&]+)");

    private static final Pattern URL_QUERY = Pattern.compile("(?i)(https?://[^\\s?]+|/[^\\s?]+)\\?[^\\s]*");

    private static final Set<Class<?>> WRAPPER_TYPES = Set.of(
            CompletionException.class,
            ExecutionException.class,
            java.lang.reflect.InvocationTargetException.class);

    private final AuditSanitizer auditSanitizer;

    /**
     * 解析业务异常中的稳定失败信息，并清洗后限制为 2000 个字符。
     *
     * @param failure 原始业务异常
     * @return 可持久化的失败详情
     */
    public AuditRecord.Failure resolve(Throwable failure) {
        Throwable root = rootCause(failure);
        String code = errorCode(root);
        String reason = reason(root);
        String type = root.getClass().getSimpleName();
        if (type.isBlank()) {
            type = "Exception";
        }
        return new AuditRecord.Failure(code, type, sanitize(reason));
    }

    /**
     * 处理审计失败相关数据。
     */
    private static Throwable rootCause(Throwable failure) {
        if (failure == null) {
            return new IllegalStateException("操作执行失败");
        }
        Throwable current = failure;
        var visited = new IdentityHashMap<Throwable, Boolean>();
        while (current.getCause() != null
                && !visited.containsKey(current.getCause())
                && (current.getMessage() == null
                        || WRAPPER_TYPES.contains(current.getClass())
                        || current instanceof DataAccessException)) {
            visited.put(current, Boolean.TRUE);
            current = current.getCause();
        }
        return current;
    }

    /**
     * 处理错误编码相关数据。
     */
    private static String errorCode(Throwable failure) {
        if (failure instanceof AccessDeniedException) {
            return "ACCESS_DENIED";
        }
        if (failure instanceof DataNotExistException) {
            return "DATA_NOT_FOUND";
        }
        if (failure instanceof DataExistException) {
            return "DATA_CONFLICT";
        }
        if (failure instanceof IllegalArgumentException) {
            return "INVALID_ARGUMENT";
        }
        return null;
    }

    /**
     * 处理原因相关数据。
     */
    private static String reason(Throwable failure) {
        if (hasDatabaseCause(failure)) {
            return "数据存储操作失败";
        }
        if (failure instanceof AccessDeniedException) {
            return "权限不足";
        }
        if (failure instanceof SpectraException || failure instanceof IllegalArgumentException) {
            String message = failure.getMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
        }
        return "操作执行失败";
    }

    /**
     * 对审计失败执行脱敏处理。
     */
    private String sanitize(String reason) {
        String cleaned = URL_QUERY.matcher(reason).replaceAll("$1?[REDACTED]");
        cleaned = INLINE_CREDENTIAL.matcher(cleaned).replaceAll("$1$2[REDACTED]");
        try {
            Object sanitized = auditSanitizer.sanitize(Map.of("failureReason", cleaned)).get("failureReason");
            if (sanitized instanceof String text && !text.isBlank()) {
                cleaned = text;
            }
        } catch (RuntimeException ignored) {
            return "操作执行失败";
        }
        if (cleaned.length() > MAX_REASON_LENGTH) {
            cleaned = cleaned.substring(0, MAX_REASON_LENGTH);
        }
        return cleaned.isBlank() ? "操作执行失败" : cleaned;
    }

    /**
     * 判断审计失败。
     */
    private static boolean hasDatabaseCause(Throwable failure) {
        Throwable current = failure;
        var visited = new IdentityHashMap<Throwable, Boolean>();
        while (current != null && !visited.containsKey(current)) {
            if (current instanceof SQLException || current instanceof DataAccessException) {
                return true;
            }
            visited.put(current, Boolean.TRUE);
            current = current.getCause();
        }
        return false;
    }
}
