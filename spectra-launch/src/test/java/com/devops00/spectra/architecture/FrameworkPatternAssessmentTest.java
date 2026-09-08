/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 后端设计模式评估矩阵和采纳边界契约测试。 */
class FrameworkPatternAssessmentTest {

    private static final String MATRIX = "docs/superpowers/specs/2026-09-04-backend-pattern-assessment-matrix.md";

    private static final List<String> CANDIDATES = List.of(
            "Session backend Strategy/Factory",
            "Session operation Strategy",
            "Cookie/CSRF 与 rate-limit subject Policy/Strategy",
            "request/response security pipeline",
            "captcha Factory/Strategy",
            "data-scope SQL Specification/Policy",
            "Session use-case command object",
            "Java25 `ScopedValue` 替代自维护上下文",
            "NameLookup/NameFillExecutor");

    @Test
    void matrixMustRecordEveryRequiredCandidateAndDecision() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        Path matrix = backend.getParent().resolve(MATRIX);
        assertThat(Files.exists(matrix)).as("Task17 模式评估矩阵必须存在").isTrue();
        String source = Files.readString(matrix);

        for (String candidate : CANDIDATES) {
            assertThat(source).as("矩阵必须评估候选: " + candidate).contains("| " + candidate + " |");
        }
        assertThat(source).contains("**采纳**：唯一达到标准且收益可测的候选");
        assertThat(source).contains("Session operation Strategy");
        assertThat(source).contains("每项有真实变体、风险和收益说明");
    }

    @Test
    void adoptedSessionStrategyMustReplaceTheOriginalModeBranches() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        String source = Files.readString(backend.resolve(
                "spectra-framework/src/main/java/com/devops00/spectra/framework/security/session/SecuritySessionIssueService.java"));

        assertThat(source).contains("SessionConcurrencyStrategyResolver")
                .contains("concurrencyStrategyResolver.resolve(policy.concurrencyMode())")
                .doesNotContain("policy.concurrencyMode() == SessionConcurrencyMode.KICK_OLD")
                .doesNotContain("policy.concurrencyMode() == SessionConcurrencyMode.REJECT_NEW");
    }

    @Test
    void adoptedResolverMustBeSpringManagedWithoutStaticRegistry() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        String source = Files.readString(backend.resolve(
                "spectra-framework/src/main/java/com/devops00/spectra/framework/security/session/concurrency/SessionConcurrencyStrategyResolver.java"));

        assertThat(source).contains("@Component")
                .contains("Map.copyOf")
                .doesNotContain("static Map")
                .doesNotContain("static final Map");
    }
}
