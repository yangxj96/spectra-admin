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

package com.devops00.spectra.framework.configure.security.ratelimit;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 当前 1.0.0 API 的固定限流策略目录。
 *
 * <p>策略只描述需要保护的接口，不对 Actuator、健康检查和普通业务查询设置通用限流。
 * 路径以应用 context path 之外的 API 路径表示。</p>
 *
 * @param name             稳定策略名称
 * @param window           固定窗口长度
 * @param maxRequests      窗口内最大请求数
 * @param subjectDimension 限流主体维度
 * @param endpoints        受保护的请求方法和路径
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/1
 */
public record RateLimitPolicy(
                              String name,
                              Duration window,
                              int maxRequests,
                              SubjectDimension subjectDimension,
                              List<Endpoint> endpoints) {

    private static final List<RateLimitPolicy> DEFAULT_POLICIES = List.of(
            new RateLimitPolicy("authentication-login", Duration.ofMinutes(1), 10, SubjectDimension.IP,
                    List.of(new Endpoint("POST", "/security/authentication/login"))),
            new RateLimitPolicy("authentication-code", Duration.ofMinutes(10), 5, SubjectDimension.IP,
                    List.of(new Endpoint("POST", "/security/authentication/sms"),
                            new Endpoint("POST", "/security/authentication/email"))),
            new RateLimitPolicy("file-upload", Duration.ofMinutes(1), 30, SubjectDimension.IP_AND_USER,
                    List.of(new Endpoint("*", "/file/uploads/**"))),
            new RateLimitPolicy("notification-provider-callback", Duration.ofMinutes(1), 120, SubjectDimension.IP,
                    List.of(new Endpoint("POST", "/notification/provider/callback/**"))),
            new RateLimitPolicy("service-diagnostic", Duration.ofMinutes(1), 10, SubjectDimension.USER,
                    List.of(new Endpoint("GET", "/service/monitor/diagnostics/runtime"),
                            new Endpoint("POST", "/service/monitor/diagnostics/tasks"))));

    /**
     * 创建并校验限流策略。
     */
    public RateLimitPolicy {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("限流策略名称不能为空");
        }
        Objects.requireNonNull(window, "限流窗口不能为空");
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("限流窗口必须为正数");
        }
        if (maxRequests < 1) {
            throw new IllegalArgumentException("限流请求数必须为正数");
        }
        Objects.requireNonNull(subjectDimension, "限流主体维度不能为空");
        endpoints = List.copyOf(endpoints);
        if (endpoints.isEmpty()) {
            throw new IllegalArgumentException("限流策略至少需要一个接口匹配项");
        }
    }

    /**
     * 从固定目录解析请求策略。
     *
     * @param method HTTP 方法
     * @param path   去除 context path 后的请求路径
     * @return 匹配到的策略
     */
    public static Optional<RateLimitPolicy> resolve(String method, String path) {
        Objects.requireNonNull(method, "HTTP 方法不能为空");
        Objects.requireNonNull(path, "请求路径不能为空");
        return DEFAULT_POLICIES.stream().filter(policy -> policy.matches(method, path)).findFirst();
    }

    /**
     * 返回不可变的默认策略目录。
     *
     * @return 默认策略
     */
    public static List<RateLimitPolicy> defaults() {
        return DEFAULT_POLICIES;
    }

    /**
     * 判断请求是否命中策略。
     */
    public boolean matches(String method, String path) {
        return endpoints.stream().anyMatch(endpoint -> endpoint.matches(method, path));
    }

    /** 限流主体维度。 */
    public enum SubjectDimension {
        /** 使用客户端地址。 */
        IP,
        /** 使用认证用户；未认证请求退化为按客户端地址隔离。 */
        USER,
        /** 同时使用客户端地址和认证用户。 */
        IP_AND_USER
    }

    /** 请求方法和路径匹配项。 */
    public record Endpoint(String method, String pathPattern) {

        /**
         * 标准化请求方法和路径。
         */
        public Endpoint {
            Objects.requireNonNull(method, "HTTP 方法不能为空");
            Objects.requireNonNull(pathPattern, "请求路径不能为空");
            method = method.trim().toUpperCase();
            pathPattern = pathPattern.trim();
            if (method.isBlank()) {
                throw new IllegalArgumentException("HTTP 方法不能为空");
            }
            if (pathPattern.isBlank()) {
                throw new IllegalArgumentException("请求路径不能为空");
            }
            if (!pathPattern.startsWith("/")) {
                pathPattern = "/" + pathPattern;
            }
        }

        /**
         * 匹配固定路径或末尾 {@code /**} 路径。
         */
        public boolean matches(String requestMethod, String requestPath) {
            if (!"*".equals(method) && !method.equalsIgnoreCase(requestMethod)) {
                return false;
            }
            if (pathPattern.endsWith("/**")) {
                String prefix = pathPattern.substring(0, pathPattern.length() - 3);
                return requestPath.equals(prefix) || requestPath.startsWith(prefix + "/");
            }
            return pathPattern.equals(requestPath);
        }
    }

    /**
     * 当前请求参与限流的主体；只在内存中保留原始值，Redis Key 使用方会进一步摘要。
     *
     * @param clientIp 客户端地址
     * @param userId   当前认证用户 ID；匿名请求为空
     */
    public record Subject(String clientIp, @Nullable String userId) {

        /**
         * 标准化主体值。
         */
        public Subject {
            Objects.requireNonNull(clientIp, "客户端地址不能为空");
            clientIp = clientIp.trim();
            if (clientIp.isBlank()) {
                throw new IllegalArgumentException("客户端地址不能为空");
            }
            if (userId != null) {
                userId = userId.trim();
                if (userId.isBlank()) {
                    userId = null;
                }
            }
        }

        /**
         * 返回主体维度的原始组合值，调用方不得直接持久化该值。
         */
        public String key(SubjectDimension dimension) {
            return switch (dimension) {
                case IP -> "ip:" + clientIp;
                case USER -> "user:" + userOrAnonymous();
                case IP_AND_USER -> "ip:" + clientIp + "|user:" + userOrAnonymous();
            };
        }

        private String userOrAnonymous() {
            return userId == null ? "anonymous@" + clientIp : userId;
        }
    }
}
