package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import com.devops00.spectra.core.user.javabean.from.ChangePasswordFrom;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 验证实际序列化后的审计快照，不允许延迟展开的业务对象携带秘密。 */
class AuditSanitizationTest {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void configuredSanitizerRemovesSecretsFromFormsAndNestedRecords() {
        AuditSanitizer sanitizer = new AuditConfiguration().auditSanitizer(mapper);
        var form = new ChangePasswordFrom("synthetic-old", "synthetic-new", "synthetic-confirm");
        var nested = new Nested("visible-name", "synthetic-token", Map.of("client_secret", "synthetic-secret"));
        var snapshot = sanitizer.sanitize(Map.of("arguments", List.of(form, nested)));
        var json = mapper.writeValueAsString(snapshot);
        assertFalse(json.contains("synthetic-"));
        assertTrue(json.contains("visible-name"));
        assertTrue(json.contains(AuditSanitizer.REDACTED_VALUE));
        assertEquals("synthetic-new", form.getNewPassword());
    }

    @Test
    void commonBoundaryDoesNotKeepUnknownObjectsForLaterSerialization() {
        var form = new ChangePasswordFrom("synthetic-old", "synthetic-new", "synthetic-confirm");
        var snapshot = new DefaultAuditSanitizer().sanitize(Map.of("form", form));
        assertFalse(mapper.writeValueAsString(snapshot).contains("synthetic-"));
        var record = new AuditRecord(null, AuditCategory.SECURITY, "PASSWORD_CHANGED", null,
                AuditRecord.Result.SUCCEEDED, null, null, Map.of("form", form), Map.of(), null, null, null);
        form.setNewPassword("synthetic-after-construction");
        assertFalse(mapper.writeValueAsString(record.after()).contains("synthetic-"));
        assertFalse(mapper.writeValueAsString(record.before()).contains("synthetic-"));
    }

    @Test
    void failureReasonsRemoveInlineCredentialsAndUrlSecrets() {
        var resolver = new AuditFailureResolver(new DefaultAuditSanitizer());
        var failure = resolver.resolve(new IllegalArgumentException(
                "password=synthetic-password token=synthetic-token https://example.test/x?secret=synthetic-url"));
        assertFalse(mapper.writeValueAsString(failure).contains("synthetic-"));
    }

    @Test
    void jsonAliasesAndSerializerFailuresCannotBypassRedaction() {
        var sanitizer = new JacksonAuditSanitizer(mapper);
        assertFalse(mapper.writeValueAsString(sanitizer.sanitize(
                Map.of("alias", new Alias("synthetic-alias")))).contains("synthetic-"));
        var result = assertDoesNotThrow(() -> sanitizer.sanitize(Map.of("broken", new BrokenBean())));
        assertEquals(Map.of("snapshot", AuditSanitizer.REDACTED_VALUE), result);
    }

    @Test
    void commonSnapshotCyclesTerminateWithRedaction() {
        var cycle = new java.util.LinkedHashMap<String, Object>();
        cycle.put("self", cycle);
        cycle.put("password", "synthetic-cycle");
        var json = mapper.writeValueAsString(new DefaultAuditSanitizer().sanitize(cycle));
        assertFalse(json.contains("synthetic-"));
        assertTrue(json.contains(AuditSanitizer.REDACTED_VALUE));
    }

    @Test
    void inlineCredentialsAndAuditReasonAreSanitizedBeforeSerialization() {
        String text = "visible password=synthetic-password token:'synthetic-token' "
                + "{\"api_key\":\"synthetic-api-key\"} client_secret=synthetic-client-secret "
                + "x_private_key=synthetic-private-key Bearer synthetic-bearer";
        var snapshot = new DefaultAuditSanitizer().sanitize(Map.of("note", text));
        var record = new AuditRecord(null, AuditCategory.OPERATION, "SYNTHETIC", null,
                AuditRecord.Result.SUCCEEDED, null, null, snapshot, Map.of(), text, null, null);
        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("visible"));
        assertTrue(json.contains(AuditSanitizer.REDACTED_VALUE));
        assertFalse(json.contains("synthetic-"));
    }

    record Nested(String displayName, String accessToken, Map<String, String> settings) {
    }

    record Alias(@com.fasterxml.jackson.annotation.JsonProperty("password") String value) {
    }

    public static class BrokenBean {
        public String getValue() {
            throw new IllegalStateException("synthetic-getter-secret");
        }
    }
}
