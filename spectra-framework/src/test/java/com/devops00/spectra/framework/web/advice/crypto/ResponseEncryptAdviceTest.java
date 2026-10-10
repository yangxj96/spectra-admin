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
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.StringHttpMessageConverter;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 响应加密注解的三态决策和方法级优先级。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/10
 */
class ResponseEncryptAdviceTest {

    @Test
    void methodAnnotationOverridesClassDecision() throws NoSuchMethodException {
        var manager = mock(CryptoKeyManager.class);
        when(manager.isConfiguredEnabled()).thenReturn(true);
        var advice = new ResponseEncryptAdvice(manager, mock(ObjectMapper.class));

        assertTrue(supports(advice, ClassDisabledController.class, "enabled"));
        assertFalse(supports(advice, ClassEnabledController.class, "disabled"));
        assertFalse(supports(advice, ClassDisabledController.class, "inheritedDisabled"));
        assertTrue(supports(advice, ClassEnabledController.class, "inheritedEnabled"));
        assertFalse(supports(advice, PlainType.class, "unannotated"));
    }

    @Test
    void configuredOffRejectsExplicitEncryption() throws NoSuchMethodException {
        var manager = mock(CryptoKeyManager.class);
        when(manager.isConfiguredEnabled()).thenReturn(false);
        var advice = new ResponseEncryptAdvice(manager, mock(ObjectMapper.class));

        assertFalse(supports(advice, ClassEnabledController.class, "inheritedEnabled"));
    }

    private static boolean supports(ResponseEncryptAdvice advice, Class<?> owner, String methodName)
            throws NoSuchMethodException {
        Method method = owner.getDeclaredMethod(methodName);
        return advice.supports(new MethodParameter(method, -1), StringHttpMessageConverter.class);
    }

    @Encrypt(response = false)
    private static class ClassDisabledController {
        @Encrypt
        public Object enabled() {
            return null;
        }

        public Object inheritedDisabled() {
            return null;
        }
    }

    @Encrypt
    private static class ClassEnabledController {
        @Encrypt(response = false)
        public Object disabled() {
            return null;
        }

        public Object inheritedEnabled() {
            return null;
        }
    }

    private static class PlainType {
        public Object unannotated() {
            return null;
        }
    }
}
