package com.devops00.spectra.core.audit.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLogQueryVisibilitySqlTest {

    private static final Pattern HIGH_RISK_FILTER = Pattern.compile(
            "<if test=\"!criteria\\.canViewHighRisk\">(?<body>.*?)</if>", Pattern.DOTALL);

    @Test
    void queryDetailAndExportShareAHighRiskFilterIndependentOfCategory() throws IOException {
        var resource = getClass().getResourceAsStream("/mapper/audit/AuditLogQueryMapper.xml");
        try (resource) {
            var xml = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            var matcher = HIGH_RISK_FILTER.matcher(xml);

            assertTrue(matcher.find(), "the shared visibility predicate must contain a high-risk filter");
            var predicate = matcher.group("body");
            assertTrue(predicate.contains("UPPER(event_type) LIKE '%SECURITY%'"));
            assertTrue(predicate.contains("UPPER(event_type) LIKE '%PASSWORD%'"));
            assertFalse(predicate.contains("category &lt;&gt; 'SECURITY' OR NOT"),
                    "high-risk operation events must not bypass the event-type filter");
            assertTrue(xml.contains("<include refid=\"VisibilityAndFilterConditions\"/>"),
                    "page, detail, count, and export must all include the shared visibility predicate");
        }
    }
}
