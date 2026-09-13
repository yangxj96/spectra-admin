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

package com.devops00.spectra.common.audit;

import org.slf4j.MDC;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * 当前执行作用域内的请求或后台任务链路上下文。
 *
 * <p>该类型位于 common，供 framework、core 和可选业务模块共享；它不依赖 Servlet 或具体业务实现，
 * 也不包含租户字段。HTTP 入口由 framework 负责创建上下文，后台 worker 必须显式创建任务级上下文并通过
 * callback 作用域建立；作用域结束后由运行时自动恢复。ScopedValue 负责作用域内的上下文隔离，MDC 只作为
 * 日志框架适配层在同一 callback 期间同步维护。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/1
 */
public final class RequestCorrelationContext {

    /** 请求 ID 的 MDC 键。 */
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    /** 关联 ID 的 MDC 键。 */
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    /** 外部请求 ID 请求头。 */
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    /** 外部关联 ID 请求头。 */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    /** 请求 ID 的 Servlet attribute 键。 */
    public static final String REQUEST_ID_ATTRIBUTE = RequestCorrelationContext.class.getName() + ".requestId";
    /** 关联 ID 的 Servlet attribute 键。 */
    public static final String CORRELATION_ID_ATTRIBUTE = RequestCorrelationContext.class.getName() + ".correlationId";

    private static final int MAX_ID_LENGTH = 128;
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    private static final ScopedValue<Context> CURRENT = ScopedValue.newInstance();

    private RequestCorrelationContext() {
    }

    /**
     * 读取当前上下文；没有上下文时返回空对象，不自动伪造请求 ID。
     *
     * @return 当前上下文或空上下文
     */
    public static Context current() {
        return CURRENT.orElse(Context.empty());
    }

    /**
     * 创建 HTTP 请求上下文。非法或缺失的外部标识会被丢弃，并使用 UUID 作为请求 ID；
     * 缺失或非法的关联 ID 默认跟随请求 ID。
     *
     * @param requestIdHeader     客户端传入的请求标识，用于在当前 HTTP 请求范围内定位一次请求
     * @param correlationIdHeader 客户端传入的关联标识，用于把当前请求与跨请求业务链路关联起来
     * @return 已清洗的 HTTP 上下文
     */
    public static Context forHttp(String requestIdHeader, String correlationIdHeader) {
        var requestId = sanitize(requestIdHeader);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        var correlationId = sanitize(correlationIdHeader);
        return new Context(requestId, correlationId == null ? requestId : correlationId);
    }

    /**
     * 创建后台任务上下文。任务没有 HTTP requestId，但必须拥有可用于日志和事件关联的 task-level correlationId。
     *
     * @param taskId 已有任务 ID；缺失或非法时自动生成 UUID
     * @return 任务级上下文
     */
    public static Context forTask(String taskId) {
        var correlationId = sanitize(taskId);
        return new Context(null, correlationId == null ? UUID.randomUUID().toString() : correlationId);
    }

    /**
     * 在 callback 作用域内安装上下文并同步设置 MDC；callback 结束时恢复进入前的 MDC 值。
     *
     * @param context 要在 callback 期间读取的链路上下文；为空时使用空上下文
     * @param action  在该链路上下文和 MDC 中执行的业务动作；动作抛出的受检异常原样传播
     * @param <T>     动作返回值类型
     * @return action 产生的业务结果；action 返回 null 时原样返回 null
     * @throws Exception action 抛出的受检异常
     */
    public static <T> T callWithMdc(Context context, Callable<T> action) throws Exception {
        Objects.requireNonNull(action, "action");
        var previousRequestId = MDC.get(REQUEST_ID_MDC_KEY);
        var previousCorrelationId = MDC.get(CORRELATION_ID_MDC_KEY);
        Context effectiveContext = context == null ? Context.empty() : context;
        return ScopedValue.where(CURRENT, effectiveContext).call(() -> {
            setMdc(REQUEST_ID_MDC_KEY, effectiveContext.requestId());
            setMdc(CORRELATION_ID_MDC_KEY, effectiveContext.correlationId());
            try {
                return action.call();
            } finally {
                setMdc(REQUEST_ID_MDC_KEY, previousRequestId);
                setMdc(CORRELATION_ID_MDC_KEY, previousCorrelationId);
            }
        });
    }

    /**
     * 在 callback 作用域内安装上下文并同步设置 MDC，适用于不返回受检异常的同步动作。
     *
     * @param context 要在 callback 期间读取的链路上下文；为空时使用空上下文
     * @param action  在该链路上下文和 MDC 中执行的同步动作
     */
    public static void runWithMdc(Context context, Runnable action) {
        Objects.requireNonNull(action, "action");
        var previousRequestId = MDC.get(REQUEST_ID_MDC_KEY);
        var previousCorrelationId = MDC.get(CORRELATION_ID_MDC_KEY);
        Context effectiveContext = context == null ? Context.empty() : context;
        ScopedValue.where(CURRENT, effectiveContext).run(() -> {
            setMdc(REQUEST_ID_MDC_KEY, effectiveContext.requestId());
            setMdc(CORRELATION_ID_MDC_KEY, effectiveContext.correlationId());
            try {
                action.run();
            } finally {
                setMdc(REQUEST_ID_MDC_KEY, previousRequestId);
                setMdc(CORRELATION_ID_MDC_KEY, previousCorrelationId);
            }
        });
    }

    /**
     * 在 callback 作用域内建立后台任务上下文并同步设置 MDC。
     *
     * @param taskId 已有任务标识；非法或缺失时自动生成 UUID，requestId 始终为空
     * @param action 在任务链路上下文中执行的动作
     * @param <T>    动作返回值类型
     * @return action 产生的业务结果；action 返回 null 时原样返回 null
     * @throws Exception action 抛出的受检异常
     */
    public static <T> T callTask(String taskId, Callable<T> action) throws Exception {
        return callWithMdc(forTask(taskId), action);
    }

    /**
     * 清洗外部链路标识。仅允许有限字符集，避免控制字符、空格、分隔符和超长输入进入响应头、MDC 或日志。
     *
     * @param candidate 外部候选值
     * @return 合法的规范化值；非法时返回 {@code null}
     */
    public static String sanitize(String candidate) {
        if (candidate == null) {
            return null;
        }
        var normalized = candidate.trim();
        return normalized.length() <= MAX_ID_LENGTH && SAFE_ID.matcher(normalized).matches()
                ? normalized
                : null;
    }

    /**
     * 当前执行作用域内的链路上下文。
     *
     * HTTP 请求 ID；后台任务上下文中为 {@code null}。
     *
     * @param requestId     HTTP 请求标识；后台任务或空上下文为 {@code null}
     * @param correlationId 跨请求、任务和事件传递的关联标识；空上下文为 {@code null}
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public record Context(String requestId, String correlationId) {

        public Context {
            requestId = sanitize(requestId);
            correlationId = sanitize(correlationId);
        }

        /**
         * 判断上下文。
         */
        public static Context empty() {
            return new Context(null, null);
        }

        /**
         * 判断上下文。
         */
        public boolean isEmpty() {
            return requestId == null && correlationId == null;
        }
    }

    /**
     * 设置MDC。
     */
    private static void setMdc(String key, String value) {
        if (value == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, value);
        }
    }
}
