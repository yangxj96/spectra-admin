/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorAlertEvent;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorAlertRule;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorSample;
import com.devops00.spectra.core.system.javabean.enums.ServiceMonitorAlertMetric;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorAlertRuleFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertEventVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertRuleVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertSummaryVO;
import com.devops00.spectra.core.system.mapper.ServiceMonitorAlertEventMapper;
import com.devops00.spectra.core.system.mapper.ServiceMonitorAlertRuleMapper;
import com.devops00.spectra.core.system.service.ServiceMonitorAlertService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** 服务监控告警规则与事件服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceMonitorAlertServiceImpl implements ServiceMonitorAlertService {

    private static final String ACTIVE = "ACTIVE";
    private static final String RECOVERED = "RECOVERED";
    private static final String ROLE_DEV_OPS = "ROLE_DEV_OPS";

    private final ServiceMonitorAlertRuleMapper ruleMapper;
    private final ServiceMonitorAlertEventMapper eventMapper;
    private final SecurityRoleMapper securityRoleMapper;
    private final RoleAssignmentMapper roleAssignmentMapper;
    private final UserMapper userMapper;
    private final TimeMapper timeMapper;
    private Optional<NotificationGateway> notificationGateway = Optional.empty();

    @Autowired(required = false)
    public void setNotificationGateway(NotificationGateway notificationGateway) {
        this.notificationGateway = Optional.ofNullable(notificationGateway);
    }

    @Override
    public void evaluate(ServiceMonitorSample sample) {
        if (sample == null) {
            return;
        }
        var rules = ruleMapper.selectList(new LambdaQueryWrapper<ServiceMonitorAlertRule>()
                .isNull(ServiceMonitorAlertRule::getDeleted)
                .eq(ServiceMonitorAlertRule::getEnabled, true)
                .orderByAsc(ServiceMonitorAlertRule::getCode));
        for (var rule : rules) {
            try {
                evaluateRule(rule, sample);
            } catch (RuntimeException exception) {
                log.warn("服务监控告警规则评估失败: ruleCode={}", rule.getCode(), exception);
            }
        }
    }

    private void evaluateRule(ServiceMonitorAlertRule rule, ServiceMonitorSample sample) {
        var observation = observe(rule, sample);
        if (observation == null) {
            return;
        }
        var active = findActiveEvent(rule.getId());
        if (observation.violated()) {
            var event = active == null ? createEvent(rule, observation) : updateActiveEvent(active, observation);
            if (shouldNotify(event, rule)) {
                notifyOperators(event);
                event.setLastNotifiedAt(Instant.now());
                eventMapper.updateById(event);
            }
            return;
        }
        if (active != null) {
            active.setState(RECOVERED);
            active.setRecoveredAt(Instant.now());
            active.setLastOccurredAt(Instant.now());
            active.setMessage("指标已恢复正常");
            eventMapper.updateById(active);
        }
    }

    private Observation observe(ServiceMonitorAlertRule rule, ServiceMonitorSample sample) {
        var metric = ServiceMonitorAlertMetric.fromCode(rule.getMetricCode());
        if (metric == null) {
            return null;
        }
        if (metric.isNumeric()) {
            if ((metric == ServiceMonitorAlertMetric.ERROR_RATE || metric == ServiceMonitorAlertMetric.P95_RESPONSE_MS)
                    && !sample.isRequestMetricsAvailable()) {
                return null;
            }
            var value = switch (metric) {
                case CPU_USAGE -> sample.getCpuUsage();
                case SYSTEM_MEMORY_USAGE -> sample.getSystemMemoryUsage();
                case JVM_HEAP_USAGE -> sample.getJvmHeapUsage();
                case ERROR_RATE -> sample.getErrorRate();
                case P95_RESPONSE_MS -> sample.getP95ResponseMs();
                default -> 0D;
            };
            var violated = compare(value, rule.getThresholdValue(), rule.getOperatorCode());
            var current = String.format("%.2f", value);
            var threshold = rule.getThresholdValue() == null ? "未设置" : String.format("%.2f", rule.getThresholdValue());
            var unit = metricUnit(metric);
            var thresholdDisplay = "未设置".equals(threshold) ? threshold : threshold + unit;
            return new Observation(violated, current + unit, thresholdDisplay, null,
                    metric.getLabel() + (violated ? "超过阈值" : "已恢复") + "：当前 " + current + unit + "，阈值 "
                            + thresholdDisplay);
        }
        var current = dependencyStatus(sample, metric);
        if (current == null) {
            return null;
        }
        var violated = compare(current, rule.getExpectedValue(), rule.getOperatorCode());
        return new Observation(violated, current, null, rule.getExpectedValue(),
                metric.getLabel() + (violated ? "异常" : "已恢复") + "：当前状态 " + current);
    }

    private static String dependencyStatus(ServiceMonitorSample sample, ServiceMonitorAlertMetric metric) {
        return switch (metric) {
            case DATABASE_STATUS -> normalizeDependencyStatus(sample.getDatabaseStatus());
            case REDIS_STATUS -> normalizeDependencyStatus(sample.getRedisStatus());
            default -> null;
        };
    }

    private static String normalizeDependencyStatus(String status) {
        return status == null || status.isBlank() ? "UNKNOWN" : status;
    }

    private static boolean compare(double value, Double threshold, String operator) {
        if (threshold == null) {
            return false;
        }
        return switch (operator == null ? "GTE" : operator.toUpperCase()) {
            case "GT" -> value > threshold;
            case "LTE" -> value <= threshold;
            case "LT" -> value < threshold;
            case "EQ" -> value == threshold;
            case "NE" -> value != threshold;
            default -> value >= threshold;
        };
    }

    private static boolean compare(String value, String expected, String operator) {
        if (expected == null) {
            return false;
        }
        var same = expected.equalsIgnoreCase(value);
        var normalizedOperator = operator == null ? "EQ" : operator;
        return "NE".equalsIgnoreCase(normalizedOperator) ? !same : "EQ".equalsIgnoreCase(normalizedOperator) && same;
    }

    private static String metricUnit(ServiceMonitorAlertMetric metric) {
        return switch (metric) {
            case CPU_USAGE, SYSTEM_MEMORY_USAGE, JVM_HEAP_USAGE, ERROR_RATE -> "%";
            case P95_RESPONSE_MS -> " ms";
            default -> "";
        };
    }

    private ServiceMonitorAlertEvent findActiveEvent(UUID ruleId) {
        return eventMapper.selectOne(new LambdaQueryWrapper<ServiceMonitorAlertEvent>()
                .eq(ServiceMonitorAlertEvent::getRuleId, ruleId)
                .eq(ServiceMonitorAlertEvent::getState, ACTIVE)
                .isNull(ServiceMonitorAlertEvent::getDeleted)
                .orderByDesc(ServiceMonitorAlertEvent::getLastOccurredAt)
                .last("LIMIT 1"));
    }

    private ServiceMonitorAlertEvent createEvent(ServiceMonitorAlertRule rule, Observation observation) {
        var now = Instant.now();
        var event = new ServiceMonitorAlertEvent();
        event.setRuleId(rule.getId());
        event.setRuleCode(rule.getCode());
        event.setRuleName(rule.getName());
        event.setMetricCode(rule.getMetricCode());
        event.setSeverity(rule.getSeverity());
        event.setState(ACTIVE);
        event.setCurrentValue(observation.currentValue());
        event.setThresholdValue(rule.getThresholdValue());
        event.setExpectedValue(observation.expectedValue());
        event.setMessage(observation.message());
        event.setFirstOccurredAt(now);
        event.setLastOccurredAt(now);
        event.setOccurrenceCount(1);
        eventMapper.insert(event);
        return event;
    }

    private ServiceMonitorAlertEvent updateActiveEvent(ServiceMonitorAlertEvent event, Observation observation) {
        event.setCurrentValue(observation.currentValue());
        event.setMessage(observation.message());
        event.setLastOccurredAt(Instant.now());
        event.setOccurrenceCount((event.getOccurrenceCount() == null ? 0 : event.getOccurrenceCount()) + 1);
        eventMapper.updateById(event);
        return event;
    }

    private boolean shouldNotify(ServiceMonitorAlertEvent event, ServiceMonitorAlertRule rule) {
        var consecutiveFailures = Math.max(rule.getConsecutiveFailures() == null ? 1 : rule.getConsecutiveFailures(), 1);
        var occurrenceCount = event.getOccurrenceCount() == null ? 0 : event.getOccurrenceCount();
        if (occurrenceCount < consecutiveFailures) {
            return false;
        }
        var last = event.getLastNotifiedAt();
        var cooldown = Math.max(rule.getCooldownSeconds() == null ? 300 : rule.getCooldownSeconds(), 0);
        return last == null || Duration.between(last, Instant.now()).getSeconds() >= cooldown;
    }

    private void notifyOperators(ServiceMonitorAlertEvent event) {
        if (notificationGateway.isEmpty() || event.getId() == null) {
            return;
        }
        var role = securityRoleMapper.selectOne(new LambdaQueryWrapper<SecurityRole>()
                .eq(SecurityRole::getCode, ROLE_DEV_OPS)
                .eq(SecurityRole::getState, ACTIVE)
                .isNull(SecurityRole::getDeleted));
        if (role == null) {
            return;
        }
        var now = Instant.now();
        var assignments = roleAssignmentMapper.selectList(new LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getRoleId, role.getId())
                .eq(RoleAssignment::getState, ACTIVE)
                .isNull(RoleAssignment::getDeleted)
                .and(wrapper -> wrapper.isNull(RoleAssignment::getValidFrom)
                        .or()
                        .le(RoleAssignment::getValidFrom, now))
                .and(wrapper -> wrapper.isNull(RoleAssignment::getValidUntil)
                        .or()
                        .gt(RoleAssignment::getValidUntil, now)));
        var userIds = assignments.stream().map(RoleAssignment::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return;
        }
        var recipients = userMapper.selectList(new LambdaQueryWrapper<User>()
                .select(User::getId)
                .in(User::getId, userIds)
                .eq(User::getStatus, UserStatus.ACTIVE)
                .isNull(User::getDeleted))
                .stream()
                .map(User::getId)
                .toList();
        if (recipients.isEmpty()) {
            return;
        }
        var title = "服务监控告警：" + event.getRuleName();
        var content = event.getMessage() + "，请在服务监控中查看。";
        notificationGateway.get()
                .enqueue(NotificationRequest.inApp(
                        "service-monitor-alert:" + event.getId() + ":" + event.getOccurrenceCount(),
                        NotificationPurpose.SYSTEM_NOTICE, recipients, "service-monitor-alert", title, content,
                        "SERVICE_MONITOR_ALERT", event.getId().toString(), "spectra-core", null));
    }

    @Override
    public List<ServiceMonitorAlertRuleVO> listRules() {
        return ruleMapper.selectList(new LambdaQueryWrapper<ServiceMonitorAlertRule>()
                .isNull(ServiceMonitorAlertRule::getDeleted)
                .orderByAsc(ServiceMonitorAlertRule::getCode))
                .stream()
                .map(this::toRuleVO)
                .toList();
    }

    @Override
    public void modifyRule(UUID id, ServiceMonitorAlertRuleFrom from) {
        var rule = ruleMapper.selectOne(new LambdaQueryWrapper<ServiceMonitorAlertRule>()
                .eq(ServiceMonitorAlertRule::getId, id)
                .isNull(ServiceMonitorAlertRule::getDeleted));
        if (rule == null) {
            throw new DataException("服务监控告警规则不存在");
        }
        if (from.getExpectedVersion() == null || !from.getExpectedVersion().equals(rule.getVersion())) {
            throw new DataException("服务监控告警规则已发生变化，请刷新后重试");
        }
        validateRule(from, rule.getMetricCode());
        var wasEnabled = Boolean.TRUE.equals(rule.getEnabled());
        rule.setVersion(from.getExpectedVersion());
        if (from.getName() != null)
            rule.setName(from.getName().trim());
        if (from.getOperatorCode() != null)
            rule.setOperatorCode(from.getOperatorCode().trim().toUpperCase());
        rule.setThresholdValue(from.getThresholdValue());
        rule.setExpectedValue(from.getExpectedValue());
        if (from.getSeverity() != null)
            rule.setSeverity(from.getSeverity().trim().toUpperCase());
        if (from.getEnabled() != null)
            rule.setEnabled(from.getEnabled());
        if (from.getConsecutiveFailures() != null)
            rule.setConsecutiveFailures(from.getConsecutiveFailures());
        if (from.getCooldownSeconds() != null)
            rule.setCooldownSeconds(from.getCooldownSeconds());
        rule.setRemark(from.getRemark());
        if (ruleMapper.updateById(rule) != 1) {
            throw new DataException("服务监控告警规则保存失败，请刷新后重试");
        }
        if (wasEnabled && !Boolean.TRUE.equals(rule.getEnabled())) {
            recoverActiveEventWhenRuleDisabled(rule.getId());
        }
    }

    private void recoverActiveEventWhenRuleDisabled(UUID ruleId) {
        var event = findActiveEvent(ruleId);
        if (event == null) {
            return;
        }
        event.setState(RECOVERED);
        event.setRecoveredAt(Instant.now());
        event.setMessage("告警规则已停用");
        eventMapper.updateById(event);
    }

    private static void validateRule(ServiceMonitorAlertRuleFrom from, String metricCode) {
        var metric = ServiceMonitorAlertMetric.fromCode(metricCode);
        if (metric == null)
            throw new DataException("告警指标无效");
        var operator = from.getOperatorCode() == null ? "GTE" : from.getOperatorCode().toUpperCase();
        if (!List.of("GTE", "GT", "LTE", "LT", "EQ", "NE").contains(operator)) {
            throw new DataException("告警比较方式无效");
        }
        if (metric.isNumeric() && from.getThresholdValue() == null) {
            throw new DataException("数值告警必须设置阈值");
        }
        if (!metric.isNumeric()) {
            if (from.getExpectedValue() == null) {
                throw new DataException("状态告警必须设置期望状态");
            }
            if (!List.of("EQ", "NE").contains(operator)) {
                throw new DataException("状态告警只支持等于或不等于");
            }
        }
        if (from.getConsecutiveFailures() != null && (from.getConsecutiveFailures() < 1 || from.getConsecutiveFailures() > 10)) {
            throw new DataException("连续触发次数必须在 1 到 10 之间");
        }
        if (from.getCooldownSeconds() != null && (from.getCooldownSeconds() < 0 || from.getCooldownSeconds() > 86400)) {
            throw new DataException("通知冷却时间必须在 0 到 86400 秒之间");
        }
    }

    @Override
    public List<ServiceMonitorAlertEventVO> listEvents(boolean activeOnly) {
        var query = new LambdaQueryWrapper<ServiceMonitorAlertEvent>()
                .isNull(ServiceMonitorAlertEvent::getDeleted)
                .orderByDesc(ServiceMonitorAlertEvent::getLastOccurredAt)
                .last("LIMIT 100");
        if (activeOnly)
            query.eq(ServiceMonitorAlertEvent::getState, ACTIVE);
        return eventMapper.selectList(query).stream().map(this::toEventVO).toList();
    }

    @Override
    public ServiceMonitorAlertSummaryVO getSummary() {
        var active = eventMapper.selectList(new LambdaQueryWrapper<ServiceMonitorAlertEvent>()
                .select(ServiceMonitorAlertEvent::getSeverity)
                .eq(ServiceMonitorAlertEvent::getState, ACTIVE)
                .isNull(ServiceMonitorAlertEvent::getDeleted));
        var recoveredToday = eventMapper.selectCount(new LambdaQueryWrapper<ServiceMonitorAlertEvent>()
                .eq(ServiceMonitorAlertEvent::getState, RECOVERED)
                .ge(ServiceMonitorAlertEvent::getRecoveredAt, LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC))
                .isNull(ServiceMonitorAlertEvent::getDeleted));
        return ServiceMonitorAlertSummaryVO.builder()
                .activeCount(active.size())
                .warningCount(active.stream().filter(item -> "WARNING".equals(item.getSeverity())).count())
                .criticalCount(active.stream().filter(item -> "CRITICAL".equals(item.getSeverity())).count())
                .recoveredTodayCount(recoveredToday)
                .build();
    }

    private ServiceMonitorAlertRuleVO toRuleVO(ServiceMonitorAlertRule rule) {
        var metric = ServiceMonitorAlertMetric.fromCode(rule.getMetricCode());
        return ServiceMonitorAlertRuleVO.builder()
                .id(rule.getId())
                .code(rule.getCode())
                .name(rule.getName())
                .metricCode(rule.getMetricCode())
                .metricLabel(metric == null ? rule.getMetricCode() : metric.getLabel())
                .operatorCode(rule.getOperatorCode())
                .thresholdValue(rule.getThresholdValue())
                .expectedValue(rule.getExpectedValue())
                .severity(rule.getSeverity())
                .enabled(rule.getEnabled())
                .consecutiveFailures(rule.getConsecutiveFailures())
                .cooldownSeconds(rule.getCooldownSeconds())
                .remark(rule.getRemark())
                .version(rule.getVersion())
                .updatedAt(toLocalDateTime(rule.getUpdatedAt()))
                .build();
    }

    private ServiceMonitorAlertEventVO toEventVO(ServiceMonitorAlertEvent event) {
        return ServiceMonitorAlertEventVO.builder()
                .id(event.getId())
                .ruleId(event.getRuleId())
                .ruleCode(event.getRuleCode())
                .ruleName(event.getRuleName())
                .metricCode(event.getMetricCode())
                .severity(event.getSeverity())
                .state(event.getState())
                .currentValue(event.getCurrentValue())
                .thresholdValue(event.getThresholdValue())
                .expectedValue(event.getExpectedValue())
                .message(event.getMessage())
                .firstOccurredAt(toLocalDateTime(event.getFirstOccurredAt()))
                .lastOccurredAt(toLocalDateTime(event.getLastOccurredAt()))
                .recoveredAt(toLocalDateTime(event.getRecoveredAt()))
                .occurrenceCount(event.getOccurrenceCount())
                .lastNotifiedAt(toLocalDateTime(event.getLastNotifiedAt()))
                .build();
    }

    private java.time.LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : timeMapper.toLocalDateTime(instant);
    }

    private record Observation(boolean violated, String currentValue, String thresholdValue,
                               String expectedValue, String message) {
    }
}
