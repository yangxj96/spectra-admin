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

package com.devops00.spectra.framework.web.advice.exception;

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.exception.BusinessConflictException;
import com.devops00.spectra.common.exception.SpectraException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExceptionResponseResolverConflictTest {

    private final ExceptionResponseResolver resolver = new ExceptionResponseResolver();

    @Test
    void mapsBusinessStateConflictToHttp409() {
        var result = resolver.resolve(new BusinessConflictException("拒绝移除最后一个有效 DEV_OPS"));
        assertEquals(HttpStatus.CONFLICT, result.status());
        assertEquals("拒绝移除最后一个有效 DEV_OPS", result.message());
    }

    @Test
    void keepsUnknownProjectFailureAsServerError() {
        var result = resolver.resolve(new SpectraException("Root 策略不可用"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.status());
    }

    @Test
    void auditStorageFailureKeepsSecretBearingCauseOutOfBoundaryLogs() {
        var failure = new AuditService.AuditRecordingException("统一审计记录写入失败",
                new IllegalStateException("synthetic-secret SQL parameter"));
        var result = resolver.resolve(failure);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.status());
        assertEquals("审计服务暂不可用", result.message());
        assertFalse(result.logStackTrace());
    }
}
