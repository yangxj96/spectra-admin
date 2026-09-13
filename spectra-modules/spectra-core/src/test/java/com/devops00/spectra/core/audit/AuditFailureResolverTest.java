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

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.access.AccessDeniedException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 验证 {@code AuditFailureResolverTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class AuditFailureResolverTest {

    private final AuditFailureResolver resolver = new AuditFailureResolver(new DefaultAuditSanitizer());

    @Test
    void domainFailureMustKeepReadableReasonAndRemoveCredentialsAndQueryParameters() {
        AuditRecord.Failure failure = resolver.resolve(
                new IllegalArgumentException("Invalid access_token=private at /users?account=personal"));

        assertEquals("INVALID_ARGUMENT", failure.code());
        assertEquals("IllegalArgumentException", failure.type());
        assertEquals("Invalid access_token=[REDACTED] at /users?[REDACTED]", failure.reason());
        assertFalse(failure.reason().contains("private"));
        assertFalse(failure.reason().contains("account=personal"));
    }

    @Test
    void databaseFailureMustNotExposeSqlOrDriverMessage() {
        var sqlFailure = new SQLException("insert into users values ('sensitive')", "23505");
        AuditRecord.Failure failure = resolver.resolve(
                new DataAccessResourceFailureException("Database failed", sqlFailure));

        assertEquals("数据存储操作失败", failure.reason());
        assertEquals("SQLException", failure.type());
    }

    @Test
    void unknownFailureMustUseGenericReasonAndAuthorizationMustUseStableCode() {
        AuditRecord.Failure unknown = resolver.resolve(new IllegalStateException("token=private"));
        AuditRecord.Failure denied = resolver.resolve(new AccessDeniedException("untrusted message"));

        assertEquals("操作执行失败", unknown.reason());
        assertEquals("ACCESS_DENIED", denied.code());
        assertEquals("权限不足", denied.reason());
    }

    @Test
    void failureReasonMustBeLimitedToTwoThousandCharacters() {
        AuditRecord.Failure failure = resolver.resolve(new IllegalArgumentException("x".repeat(2500)));

        assertEquals(2000, failure.reason().length());
    }
}
