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

import java.util.List;

/**
 * 定义规则结果相关的应用服务契约。
 *
 * @param evaluatedRuleCount 本次检查评估的规则数量
 * @param failures           未通过校验的规则及其原因集合
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record ServiceMonitorRuleEvaluationResult(int evaluatedRuleCount, List<RuleFailure> failures) {

    public ServiceMonitorRuleEvaluationResult {
        if (evaluatedRuleCount < 0) {
            throw new IllegalArgumentException("规则评估数量不能为负数");
        }
        failures = List.copyOf(failures);
    }

    public static ServiceMonitorRuleEvaluationResult successful(int evaluatedRuleCount) {
        return new ServiceMonitorRuleEvaluationResult(evaluatedRuleCount, List.of());
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    /**
     * 定义规则失败相关的应用服务契约。
     *
     * @param ruleCode 规则编码
     * @param message  处理结果或异常原因的说明
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public record RuleFailure(String ruleCode, String message) {
    }
}
