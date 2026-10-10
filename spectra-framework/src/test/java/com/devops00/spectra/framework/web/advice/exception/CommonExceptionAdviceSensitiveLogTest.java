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
package com.devops00.spectra.framework.web.advice.exception;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 验证校验异常的拒绝值不会进入 HTTP 边界日志。 */
class CommonExceptionAdviceSensitiveLogTest {

    @Test
    void rejectedPasswordValueDoesNotReachLogMessageOrThrowable() throws NoSuchMethodException {
        String secret = "synthetic-secret-password";
        var binding = new BeanPropertyBindingResult(new Object(), "form");
        binding.addError(new FieldError("form", "password", secret, false,
                null, null, "密码格式无效"));
        var parameter = new MethodParameter(getClass().getDeclaredMethod("invalid", String.class), 0);
        var failure = new MethodArgumentNotValidException(parameter, binding);
        assertTrue(failure.getMessage().contains(secret));

        var logger = (Logger) LoggerFactory.getLogger(CommonExceptionAdvice.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            var response = new MockHttpServletResponse();
            new CommonExceptionAdvice().methodArgumentNotValidException(failure, response);
            assertEquals(400, response.getStatus());
            assertFalse(appender.list.isEmpty());
            assertFalse(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains(secret) || event.getThrowableProxy() != null));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private void invalid(String password) {
        // 仅用于构造 MethodParameter。
    }
}
