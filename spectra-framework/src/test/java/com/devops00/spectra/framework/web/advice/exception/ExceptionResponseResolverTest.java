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

import com.devops00.spectra.common.exception.BusinessConflictException;
import com.devops00.spectra.common.exception.BusinessRuleViolationException;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.exception.SecuritySecretUnavailableException;
import com.devops00.spectra.framework.web.response.WebErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ExceptionResponseResolver} 稳定错误码映射的验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class ExceptionResponseResolverTest {

    private final ExceptionResponseResolver resolver = new ExceptionResponseResolver();

    @Test
    void dataNotFoundMapsToNotFound() {
        var resolution = resolver.resolve(new DataNotExistException("缺失"));
        assertEquals(HttpStatus.NOT_FOUND, resolution.status());
        assertEquals(WebErrorCode.DATA_NOT_FOUND, resolution.errorCode());
    }

    @Test
    void dataExistMapsToConflict() {
        var resolution = resolver.resolve(new DataExistException("重复"));
        assertEquals(HttpStatus.CONFLICT, resolution.status());
        assertEquals(WebErrorCode.DATA_EXIST, resolution.errorCode());
    }

    @Test
    void entityUpdateMapsToVersionConflict() {
        var resolution = resolver.resolve(new EntityUpdateException("更新冲突"));
        assertEquals(HttpStatus.CONFLICT, resolution.status());
        assertEquals(WebErrorCode.ENTITY_UPDATE_CONFLICT, resolution.errorCode());
    }

    @Test
    void businessConflictMapsToConflict() {
        var resolution = resolver.resolve(new BusinessConflictException("状态冲突"));
        assertEquals(HttpStatus.CONFLICT, resolution.status());
        assertEquals(WebErrorCode.BUSINESS_CONFLICT, resolution.errorCode());
    }

    @Test
    void businessRuleMapsToBadRequest() {
        var resolution = resolver.resolve(new BusinessRuleViolationException("规则"));
        assertEquals(HttpStatus.BAD_REQUEST, resolution.status());
        assertEquals(WebErrorCode.BUSINESS_RULE_VIOLATION, resolution.errorCode());
    }

    @Test
    void securityRedisMapsToServiceUnavailable() {
        var resolution = resolver.resolve(new SecurityRedisUnavailableException("redis", null));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resolution.status());
        assertEquals(WebErrorCode.SECURITY_REDIS_UNAVAILABLE, resolution.errorCode());
    }

    @Test
    void securitySecretMapsToServiceUnavailable() {
        var resolution = resolver.resolve(new SecuritySecretUnavailableException("secret", null));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resolution.status());
        assertEquals(WebErrorCode.SECURITY_SECRET_UNAVAILABLE, resolution.errorCode());
    }

    @Test
    void accessDeniedMapsToForbidden() {
        var resolution = resolver.resolve(new AccessDeniedException("denied"));
        assertEquals(HttpStatus.FORBIDDEN, resolution.status());
        assertEquals(WebErrorCode.ACCESS_DENIED, resolution.errorCode());
    }

    @Test
    void badCredentialsMapsToUnauthorized() {
        var resolution = resolver.resolve(new BadCredentialsException("bad"));
        assertEquals(HttpStatus.UNAUTHORIZED, resolution.status());
        assertEquals(WebErrorCode.AUTHENTICATION_FAILED, resolution.errorCode());
    }

    @Test
    void unknownMapsToInternalErrorWithStackTrace() {
        var resolution = resolver.resolve(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resolution.status());
        assertEquals(WebErrorCode.INTERNAL_ERROR, resolution.errorCode());
        assertEquals(true, resolution.logStackTrace());
    }
}
