/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.service;

import java.util.List;

/** 服务监控规则评估结果，保留规则级失败而不是把失败规则当成成功。 */
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

    /** 单条规则的失败摘要。 */
    public record RuleFailure(String ruleCode, String message) {
    }
}
