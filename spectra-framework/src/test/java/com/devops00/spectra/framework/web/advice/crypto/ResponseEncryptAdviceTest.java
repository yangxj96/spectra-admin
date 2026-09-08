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

package com.devops00.spectra.framework.web.advice.crypto;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 响应加密安全边界回归测试。 */
class ResponseEncryptAdviceTest {

    @Test
    void shouldBypassBinaryAndResourceResponses() throws Exception {
        var keyManager = mock(CryptoKeyManager.class);
        when(keyManager.isEnabled()).thenReturn(true);
        when(keyManager.isConfiguredEnabled()).thenReturn(true);
        var advice = new ResponseEncryptAdvice(keyManager, new ObjectMapper());

        assertFalse(advice.supports(parameter("binary"), ByteArrayHttpMessageConverter.class));
        assertFalse(advice.supports(parameter("resource"), ResourceHttpMessageConverter.class));

        var resource = new ByteArrayResource(new byte[]{1, 2, 3});
        var request = mock(ServerHttpRequest.class);
        var response = mock(ServerHttpResponse.class);
        assertSame(resource, advice.beforeBodyWrite(resource, parameter("resource"), MediaType.APPLICATION_OCTET_STREAM,
                ResourceHttpMessageConverter.class, request, response));
        var entity = ResponseEntity.ok(resource);
        assertSame(entity, advice.beforeBodyWrite(entity, parameter("entityResource"), MediaType.APPLICATION_OCTET_STREAM,
                ResourceHttpMessageConverter.class, request, response));
    }

    @Test
    void shouldFailClosedWhenExplicitResponseEncryptionHasNoKeys() throws Exception {
        var keyManager = mock(CryptoKeyManager.class);
        when(keyManager.isEnabled()).thenReturn(false);
        when(keyManager.isConfiguredEnabled()).thenReturn(true);
        when(keyManager.getClientPublicKey()).thenReturn(null);
        when(keyManager.getServerPrivateKey()).thenReturn(null);
        var advice = new ResponseEncryptAdvice(keyManager, new ObjectMapper());

        assertTrue(advice.supports(parameter("encrypted"), StringHttpMessageConverter.class));
        assertThrows(EncryptException.class,
                () -> advice.beforeBodyWrite(new Payload(), parameter("encrypted"), MediaType.APPLICATION_JSON,
                        StringHttpMessageConverter.class, mock(ServerHttpRequest.class), mock(ServerHttpResponse.class)));
    }

    private static MethodParameter parameter(String name) throws NoSuchMethodException {
        Method method = Endpoint.class.getDeclaredMethod(name);
        return new MethodParameter(method, -1);
    }

    private static final class Endpoint {

        @Encrypt(response = true)
        Payload encrypted() {
            return new Payload();
        }

        byte[] binary() {
            return new byte[]{1};
        }

        Resource resource() {
            return new ByteArrayResource(new byte[]{1});
        }

        ResponseEntity<Resource> entityResource() {
            return ResponseEntity.ok(resource());
        }
    }

    private static final class Payload {
        @SuppressWarnings("unused")
        private String name = "spectra";
    }
}
