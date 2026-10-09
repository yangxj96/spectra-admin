/*
 * Copyright 2018-2026 yangxj96
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * 按实际 JSON 字段展开业务对象，再交由公共规则脱敏。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/07
 */
public final class JacksonAuditSanitizer implements AuditSanitizer {

    private static final TypeReference<Map<String, Object>> SNAPSHOT_TYPE = new TypeReference<>() { };
    private final ObjectMapper mapper;
    private final AuditSanitizer delegate = new DefaultAuditSanitizer();

    public JacksonAuditSanitizer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<String, Object> sanitize(Map<String, ?> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return Map.of();
        }
        try {
            return delegate.sanitize(mapper.convertValue(snapshot, SNAPSHOT_TYPE));
        } catch (RuntimeException ignored) {
            // Getter、循环结构或自定义序列化器失败时不保留原对象，也不传播含入参的异常。
            return Map.of("snapshot", REDACTED_VALUE);
        }
    }
}
