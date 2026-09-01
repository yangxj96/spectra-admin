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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * 当前线程的请求或后台任务链路上下文。
 *
 * <p>该类型位于 common，供 framework、core 和可选业务模块共享；它不依赖 Servlet 或具体业务实现，
 * 也不包含租户字段。HTTP 入口由 framework 负责创建上下文，后台 worker 必须显式创建任务级上下文并在 finally
 * 中关闭。</p>
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
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private RequestCorrelationContext() {
    }

    /**
     * 读取当前上下文；没有上下文时返回空对象，不自动伪造请求 ID。
     *
     * @return 当前上下文或空上下文
     */
    public static Context current() {
        var context = CURRENT.get();
        return context == null ? Context.empty() : context;
    }

    /**
     * 创建 HTTP 请求上下文。非法或缺失的外部标识会被丢弃，并使用 UUID 作为请求 ID；
     * 缺失或非法的关联 ID 默认跟随请求 ID。
     *
     * @param requestIdHeader     外部请求 ID
     * @param correlationIdHeader 外部关联 ID
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
     * 在当前线程安装上下文，并在关闭时恢复之前的上下文。
     *
     * @param context 要安装的上下文
     * @return 可关闭的作用域
     */
    public static Scope open(Context context) {
        var previous = CURRENT.get();
        CURRENT.set(context == null ? Context.empty() : context);
        return new Scope(previous, null, null, false);
    }

    /**
     * 安装上下文并同步设置 MDC；关闭时恢复原有 MDC，适用于 HTTP、worker 和异步任务边界。
     *
     * @param context 要安装的上下文
     * @return 可关闭的作用域
     */
    public static Scope openWithMdc(Context context) {
        var previousRequestId = MDC.get(REQUEST_ID_MDC_KEY);
        var previousCorrelationId = MDC.get(CORRELATION_ID_MDC_KEY);
        var previous = CURRENT.get();
        CURRENT.set(context == null ? Context.empty() : context);
        setMdc(REQUEST_ID_MDC_KEY, current().requestId());
        setMdc(CORRELATION_ID_MDC_KEY, current().correlationId());
        return new Scope(previous, previousRequestId, previousCorrelationId, true);
    }

    /**
     * 打开后台任务级上下文并同步 MDC。
     *
     * @param taskId 已有任务标识；非法或缺失时自动生成 UUID
     * @return 可关闭的作用域
     */
    public static Scope openTask(String taskId) {
        return openWithMdc(forTask(taskId));
    }

    /** 清理当前线程上下文，主要供测试和线程边界使用。 */
    public static void clear() {
        CURRENT.remove();
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

    /** 当前线程的链路上下文。 */
    public record Context(String requestId, String correlationId) {

        public Context {
            requestId = sanitize(requestId);
            correlationId = sanitize(correlationId);
        }

        /** @return 空上下文 */
        public static Context empty() {
            return new Context(null, null);
        }

        /** @return 当前上下文是否没有任何标识 */
        public boolean isEmpty() {
            return requestId == null && correlationId == null;
        }
    }

    /** 可恢复的线程上下文作用域。 */
    public static final class Scope implements AutoCloseable {

        private final Context previous;
        private final String previousRequestId;
        private final String previousCorrelationId;
        private final boolean mdcManaged;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Scope(Context previous, String previousRequestId, String previousCorrelationId, boolean mdcManaged) {
            this.previous = previous;
            this.previousRequestId = previousRequestId;
            this.previousCorrelationId = previousCorrelationId;
            this.mdcManaged = mdcManaged;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                if (previous == null) {
                    CURRENT.remove();
                } else {
                    CURRENT.set(previous);
                }
                if (mdcManaged) {
                    setMdc(REQUEST_ID_MDC_KEY, previousRequestId);
                    setMdc(CORRELATION_ID_MDC_KEY, previousCorrelationId);
                }
            }
        }
    }

    private static void setMdc(String key, String value) {
        if (value == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, value);
        }
    }
}
