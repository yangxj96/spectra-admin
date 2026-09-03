/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorAlertEvent;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorAlertRule;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorSample;
import com.devops00.spectra.core.system.mapper.ServiceMonitorAlertEventMapper;
import com.devops00.spectra.core.system.mapper.ServiceMonitorAlertRuleMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 服务监控告警使用 Core 内置通知能力。 */
class ServiceMonitorAlertServiceImplNotificationTest {

    @Test
    void notificationServiceMustBeConstructorDependency() {
        assertThat(Arrays.stream(ServiceMonitorAlertServiceImpl.class.getDeclaredConstructors())
                .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
                .anyMatch(NotificationService.class::equals))
                .isTrue();
    }

    @Test
    void shouldNotNotifyWhenNoOperatorsAreAssigned() {
        var ruleMapper = mock(ServiceMonitorAlertRuleMapper.class);
        var eventMapper = mock(ServiceMonitorAlertEventMapper.class);
        var securityRoleMapper = mock(SecurityRoleMapper.class);
        var roleAssignmentMapper = mock(RoleAssignmentMapper.class);
        var userMapper = mock(UserMapper.class);
        var timeMapper = mock(TimeMapper.class);
        var notificationService = mock(NotificationService.class);
        var rule = new ServiceMonitorAlertRule();
        rule.setId(UUID.randomUUID());
        rule.setCode("CPU_USAGE_HIGH");
        rule.setName("CPU 使用率过高");
        rule.setMetricCode("CPU_USAGE");
        rule.setOperatorCode("GTE");
        rule.setThresholdValue(80D);
        rule.setSeverity("WARNING");
        rule.setEnabled(true);
        rule.setConsecutiveFailures(1);
        rule.setCooldownSeconds(0);
        var sample = new ServiceMonitorSample();
        sample.setCpuUsage(90D);
        when(ruleMapper.selectList(any())).thenReturn(List.of(rule));
        when(eventMapper.selectOne(any())).thenReturn(null);
        when(securityRoleMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            invocation.getArgument(0, ServiceMonitorAlertEvent.class).setId(UUID.randomUUID());
            return 1;
        }).when(eventMapper).insert(any(ServiceMonitorAlertEvent.class));

        var service = new ServiceMonitorAlertServiceImpl(
                ruleMapper, eventMapper, securityRoleMapper, roleAssignmentMapper, userMapper, timeMapper,
                notificationService);

        service.evaluate(sample);

        var event = org.mockito.ArgumentCaptor.forClass(ServiceMonitorAlertEvent.class);
        verify(eventMapper).insert(event.capture());
        assertThat(event.getValue().getLastNotifiedAt()).isNull();
        verify(eventMapper, never()).updateById(any(ServiceMonitorAlertEvent.class));
        verify(securityRoleMapper).selectOne(any());
        verify(notificationService, never()).send(any());
    }
}
