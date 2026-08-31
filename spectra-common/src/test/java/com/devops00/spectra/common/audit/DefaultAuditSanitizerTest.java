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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultAuditSanitizerTest {

    @Test
    void shouldRecursivelyRedactSensitiveFieldsWithoutChangingSource() {
        Map<String, Object> source = Map.of(
                "username", "tester",
                "password", "plain-text",
                "nested", List.of(Map.of("refresh_token", "refresh-value", "business_code", "OA-001")));

        Map<String, Object> sanitized = DefaultAuditSanitizer.INSTANCE.sanitize(source);

        assertEquals("***", sanitized.get("password"));
        @SuppressWarnings("unchecked")
        Map<String, Object> nested = (Map<String, Object>) ((List<?>) sanitized.get("nested")).getFirst();
        assertEquals("***", nested.get("refresh_token"));
        assertEquals("OA-001", nested.get("business_code"));
        assertEquals("plain-text", source.get("password"));
    }

    @Test
    void shouldRedactCredentialVariantsAndEmbeddedTokens() {
        Map<String, Object> sanitized = DefaultAuditSanitizer.INSTANCE.sanitize(Map.of(
                "accessToken", "access-value",
                "client_secret", "secret-value",
                "private-key", "private-value",
                "public_key", "public-value",
                "url", "https://s3.example/object?X-Amz-Signature=signature-value",
                "header", "Authorization: Bearer token.value"));

        assertEquals("***", sanitized.get("accessToken"));
        assertEquals("***", sanitized.get("client_secret"));
        assertEquals("***", sanitized.get("private-key"));
        assertEquals("public-value", sanitized.get("public_key"));
        assertEquals("https://s3.example/object?X-Amz-Signature=***", sanitized.get("url"));
        assertEquals("Authorization: Bearer ***", sanitized.get("header"));
    }
}
