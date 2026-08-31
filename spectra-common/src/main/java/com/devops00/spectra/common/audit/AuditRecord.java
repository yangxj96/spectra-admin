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

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 统一的不可变审计事件模型。
 *
 * <p>before/after 只接受结构化快照。调用方必须在创建事件前通过统一的
 * {@link AuditSanitizer} 清洗快照；本记录同时对快照做深度防御性复制，避免业务对象在提交后被修改。
 * 本模型不引用任何表、Mapper、Spring 类型或具体日志实现。</p>
 *
 * @param eventId    事件唯一 ID
 * @param category   事件分类
 * @param eventType  稳定事件类型
 * @param targetId   被操作主体 ID；系统级事件可以为空
 * @param result     操作结果
 * @param occurredAt 事件发生时间，使用 UTC instant
 * @param context    操作者、请求和客户端上下文
 * @param before     变更前脱敏快照
 * @param after      变更后脱敏快照
 * @param reason     操作原因
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public record AuditRecord(UUID eventId,
                          AuditCategory category,
                          String eventType,
                          UUID targetId,
                          Result result,
                          Instant occurredAt,
                          AuditContext context,
                          Map<String, Object> before,
                          Map<String, Object> after,
                          String reason) {

    public AuditRecord {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        category = Objects.requireNonNull(category, "审计事件分类不能为空");
        eventType = requireText(eventType, "审计事件类型不能为空");
        result = Objects.requireNonNull(result, "审计事件结果不能为空");
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        context = context == null ? AuditContext.empty() : context;
        before = immutableSnapshot(before);
        after = immutableSnapshot(after);
    }

    /**
     * 返回审计前快照的防御性副本。
     *
     * @return 不受记录内部状态影响的快照
     */
    @Override
    public Map<String, Object> before() {
        return immutableSnapshot(before);
    }

    /**
     * 返回审计后快照的防御性副本。
     *
     * @return 不受记录内部状态影响的快照
     */
    @Override
    public Map<String, Object> after() {
        return immutableSnapshot(after);
    }

    /**
     * 审计事件结果。
     */
    public enum Result {

        /** 高风险动作已开始但尚未完成。 */
        STARTED,

        /** 动作成功完成。 */
        SUCCEEDED,

        /** 动作执行失败。 */
        FAILED,

        /** 动作被安全策略拒绝。 */
        DENIED
    }

    /**
     * 校验并规范化文本字段。
     */
    private static String requireText(String value, String message) {
        Objects.requireNonNull(value, message);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    /**
     * 深度复制快照结构，不在公共模型中重复实现字段脱敏规则。
     */
    private static Map<String, Object> immutableSnapshot(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> copied = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key == null) {
                throw new IllegalArgumentException("审计快照字段名不能为空");
            }
            copied.put(key, immutableValue(value));
        });
        return Collections.unmodifiableMap(copied);
    }

    /**
     * 深度复制 JSON 友好的集合和数组结构。
     */
    private static Object immutableValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copied = new LinkedHashMap<>();
            map.forEach((key, nestedValue) -> {
                if (key == null) {
                    throw new IllegalArgumentException("审计快照字段名不能为空");
                }
                copied.put(String.valueOf(key), immutableValue(nestedValue));
            });
            return Collections.unmodifiableMap(copied);
        }
        if (value instanceof Collection<?> collection) {
            List<Object> copied = new ArrayList<>(collection.size());
            collection.forEach(item -> copied.add(immutableValue(item)));
            return Collections.unmodifiableList(copied);
        }
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> copied = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                copied.add(immutableValue(Array.get(value, index)));
            }
            return Collections.unmodifiableList(copied);
        }
        return value;
    }
}
