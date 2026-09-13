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

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorSample;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorAlertRuleFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertEventVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertRuleVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertSummaryVO;

import java.util.List;
import java.util.UUID;

/**
 * 服务监控告警规则与事件服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface ServiceMonitorAlertService {

    /**
     * 根据最新采样评估规则并推进事件状态。
     *
     * @param sample 当前实例最新的 CPU、内存、磁盘等监控采样，用于与告警规则比较。
     */
    void evaluate(ServiceMonitorSample sample);

    /**
     * 根据最新采样评估规则，并返回规则级失败摘要。
     *
     * @param sample 当前实例最新的 CPU、内存、磁盘等监控采样，用于计算规则命中和恢复结果。
     * @return 返回服务监控规则评估结果；处理失败时抛出业务异常，不返回 null。
     */
    default ServiceMonitorRuleEvaluationResult evaluateWithResult(ServiceMonitorSample sample) {
        evaluate(sample);
        return ServiceMonitorRuleEvaluationResult.successful(0);
    }

    /**
     * 查询规则。
     *
     * @return 返回已配置的服务监控告警规则，包含指标、阈值、持续时间和启用状态；没有规则时返回空列表，不返回 null。
     */
    List<ServiceMonitorAlertRuleVO> listRules();

    /**
     * 修改规则。
     *
     * @param id   待修改告警规则的唯一标识。
     * @param from 告警指标、阈值、持续时间、启用状态和通知配置等规则字段。
     */
    void modifyRule(UUID id, ServiceMonitorAlertRuleFrom from);

    /**
     * 查询告警事件。
     *
     * @param activeOnly 是否只返回当前仍处于活动状态的告警事件；为 false 时同时包含已恢复和已关闭事件。
     * @return 返回由最新采样触发的服务监控告警事件，包含规则、当前状态和触发时间；没有匹配事件时返回空列表，不返回 null。
     */
    List<ServiceMonitorAlertEventVO> listEvents(boolean activeOnly);

    /**
     * 查询告警摘要。
     *
     * @return 返回当前服务监控告警的活动数量、已恢复数量和最近事件摘要；采集失败时抛出业务异常，不返回 null。
     */
    ServiceMonitorAlertSummaryVO getSummary();
}
