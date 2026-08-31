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

package com.devops00.spectra.common.health;

import java.time.Duration;
import java.time.Instant;

/**
 * 单个健康 contributor 的不可变检查结果。
 *
 * <p>{@code safeSummary} 只能包含面向运维的脱敏摘要，禁止放入连接串、凭据、原始异常消息或堆栈。
 * {@code checkedAt} 使用 UTC {@link Instant}，延迟使用 {@link Duration}，避免各模块自行约定单位。</p>
 *
 * @param contributorName contributor 唯一名称
 * @param moduleName      所属模块名称
 * @param dependencyType  被检查依赖类型，例如 DATABASE、REDIS、OBJECT_STORAGE 或 MODULE
 * @param status          统一健康状态
 * @param latency         检查耗时
 * @param checkedAt       检查完成时间（UTC）
 * @param errorCode       稳定、可检索的错误码；正常时为空
 * @param safeSummary     脱敏后的安全摘要；不得包含凭据和堆栈
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public record DependencyHealthResult(String contributorName,
                                     String moduleName,
                                     String dependencyType,
                                     DependencyHealthStatus status,
                                     Duration latency,
                                     Instant checkedAt,
                                     String errorCode,
                                     String safeSummary) {

    private static final int MAX_SUMMARY_LENGTH = 512;

    public DependencyHealthResult {
        contributorName = requireText(contributorName, "contributorName");
        moduleName = requireText(moduleName, "moduleName");
        dependencyType = requireText(dependencyType, "dependencyType");
        if (status == null) {
            throw new IllegalArgumentException("health status 不能为空");
        }
        if (latency == null) {
            latency = Duration.ZERO;
        }
        if (latency.isNegative()) {
            throw new IllegalArgumentException("health latency 不能为负数");
        }
        if (checkedAt == null) {
            checkedAt = Instant.now();
        }
        errorCode = normalize(errorCode);
        safeSummary = normalizeSummary(safeSummary);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " 不能为空");
        }
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalizeSummary(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace('\r', ' ').replace('\n', ' ').trim();
        return normalized.length() <= MAX_SUMMARY_LENGTH
                ? normalized
                : normalized.substring(0, MAX_SUMMARY_LENGTH);
    }
}
