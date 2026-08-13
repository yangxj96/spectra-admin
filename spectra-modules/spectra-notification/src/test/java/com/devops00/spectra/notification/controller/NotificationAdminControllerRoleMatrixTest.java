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

package com.devops00.spectra.notification.controller;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.javabean.from.NotificationAdminQueryFrom;
import com.devops00.spectra.notification.service.NotificationAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 管理端角色矩阵的可执行授权回归测试。
 */
class NotificationAdminControllerRoleMatrixTest {

    private final ExpressionParser parser = new SpelExpressionParser();

    private final DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();

    @Test
    void shouldAllowBothOperationsToDevOpsButOnlyReadToAudit() throws Exception {
        var controller = new NotificationAdminController(mock(NotificationAdminService.class));
        var query = NotificationAdminController.class.getMethod("pageRequests",
                com.devops00.spectra.common.base.javabean.from.PageFrom.class, NotificationAdminQueryFrom.class);
        var retry = NotificationAdminController.class.getMethod("retry", java.util.UUID.class);

        assertTrue(evaluate(query, controller, authentication("ROLE_DEV_OPS")));
        assertTrue(evaluate(retry, controller, authentication("ROLE_DEV_OPS")));
        assertTrue(evaluate(query, controller, authentication("ROLE_AUDIT")));
        assertFalse(evaluate(retry, controller, authentication("ROLE_AUDIT")));
    }

    @Test
    void shouldDenyAllAdminOperationsToOrdinaryUsers() throws Exception {
        var controller = new NotificationAdminController(mock(NotificationAdminService.class));
        var channel = NotificationAdminController.class.getMethod("availability", NotificationChannel.class);
        var cancel = NotificationAdminController.class.getMethod("cancel", java.util.UUID.class);

        var ordinary = authentication("ROLE_USER");
        assertFalse(evaluate(channel, controller, ordinary));
        assertFalse(evaluate(cancel, controller, ordinary));
    }

    private boolean evaluate(Method method, NotificationAdminController controller, Authentication authentication) {
        var invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getThis()).thenReturn(controller);
        when(invocation.getArguments()).thenReturn(new Object[method.getParameterCount()]);
        var annotation = method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
        var context = handler.createEvaluationContext(() -> authentication, invocation);
        return Boolean.TRUE.equals(parser.parseExpression(annotation.value()).getValue(context, Boolean.class));
    }

    private Authentication authentication(String role) {
        return new UsernamePasswordAuthenticationToken("test-user",
                "test-token", List.of(new SimpleGrantedAuthority(role)));
    }
}
