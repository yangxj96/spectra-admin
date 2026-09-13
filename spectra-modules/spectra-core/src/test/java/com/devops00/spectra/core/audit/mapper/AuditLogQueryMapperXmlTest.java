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

package com.devops00.spectra.core.audit.mapper;

import com.devops00.spectra.framework.persistence.mybatis.handler.UUIDTypeHandler;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.type.InstantTypeHandler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 {@code AuditLogQueryMapperXmlTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class AuditLogQueryMapperXmlTest {

    @Test
    void pageAndCountMustApplyVisibilityAndUserFiltersUsingBoundParameters() throws Exception {
        Configuration configuration = parseMapperXml();
        UUID viewerId = UUID.randomUUID();
        Map<String, Object> criteria = Map.ofEntries(
                Map.entry("canViewHighRisk", false),
                Map.entry("canViewAllNonHighRisk", false),
                Map.entry("viewerId", viewerId),
                Map.entry("category", "SECURITY"),
                Map.entry("eventTypePattern", "%PASSWORD%"),
                Map.entry("operatorId", viewerId),
                Map.entry("operatorPattern", "%admin%"),
                Map.entry("targetId", UUID.randomUUID()),
                Map.entry("result", "FAILED"),
                Map.entry("from", Instant.parse("2026-09-01T00:00:00Z")),
                Map.entry("to", Instant.parse("2026-09-13T00:00:00Z")));
        Map<String, Object> parameters = Map.of("criteria", criteria, "limit", 15, "offset", 30L);

        String countSql = normalize(configuration.getMappedStatement(namespace() + ".countPage")
                .getBoundSql(parameters)
                .getSql());
        String pageSql = normalize(configuration.getMappedStatement(namespace() + ".selectPage")
                .getBoundSql(parameters)
                .getSql());

        assertThat(countSql)
                .contains("category <> 'SECURITY' OR NOT")
                .contains("category <> 'SECURITY' OR operator_id = ? OR target_id = ?")
                .contains("event_type ILIKE ? OR reason ILIKE ?")
                .contains("audit_operator.real_name ILIKE ?")
                .contains("operator_id = ? OR EXISTS")
                .contains("target_id = ?")
                .contains("occurred_at >= ?")
                .contains("occurred_at < ?");
        assertThat(pageSql)
                .contains("ORDER BY occurred_at DESC, event_id DESC")
                .contains("LIMIT ? OFFSET ?");
        assertThat(configuration.getMappedStatement(namespace() + ".selectPage")
                .getBoundSql(parameters)
                .getParameterMappings())
                .extracting(mapping -> mapping.getProperty())
                .contains("limit", "offset");
    }

    @Test
    void detailMustIncludeBothEventIdAndExactPartitionTimestamp() throws Exception {
        Configuration configuration = parseMapperXml();
        Map<String, Object> parameters = Map.of(
                "criteria", Map.of("canViewHighRisk", true, "canViewAllNonHighRisk", true),
                "eventId", UUID.randomUUID(),
                "occurredAt", Instant.parse("2026-09-13T03:53:16.442Z"));

        String sql = normalize(configuration.getMappedStatement(namespace() + ".selectDetail")
                .getBoundSql(parameters)
                .getSql());

        assertThat(sql).contains("event_id = ?").contains("occurred_at = ?");
    }

    /**
     * 解析XML。
     */
    private Configuration parseMapperXml() throws Exception {
        Configuration configuration = new Configuration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(Instant.class, InstantTypeHandler.class);
        try (InputStream mapperXml = getClass().getResourceAsStream("/mapper/audit/AuditLogQueryMapper.xml")) {
            assertThat(mapperXml).as("AuditLogQueryMapper XML resource").isNotNull();
            new XMLMapperBuilder(mapperXml, configuration, "mapper/audit/AuditLogQueryMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        return configuration;
    }

    /**
     * 处理审计日志查询XML相关数据。
     */
    private String namespace() {
        return "com.devops00.spectra.core.audit.mapper.AuditLogQueryMapper";
    }

    /**
     * 规范化审计日志查询XML。
     */
    private String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
