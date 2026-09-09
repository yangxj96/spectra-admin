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

package com.devops00.spectra.core.quartz;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** 旧自研调度内核运行时引用清理契约。 */
class QuartzLegacyReferenceContractTest {

    private static final List<String> LEGACY_REFERENCES = List.of(
            "@Scheduled",
            "@EnableScheduling",
            "ScheduledJobHandler",
            "ScheduledLoopHandler",
            "SchedulerKernel",
            "LoopController",
            "SingletonLoopLeaseService",
            "scheduler_job",
            "scheduler_execution",
            "scheduler_loop_runtime",
            "scheduler_control_command",
            "scheduler_loop_error");

    @Test
    void runtimeJavaSourceMustNotReferenceTheLegacyScheduler() throws IOException {
        var matches = runtimeJavaFiles()
                .flatMap(path -> read(path).flatMap(text -> LEGACY_REFERENCES.stream()
                        .filter(text::contains)
                        .map(reference -> path + " contains " + reference)))
                .toList();

        assertThat(matches).as("运行时仍存在旧自研调度引用").isEmpty();
    }

    private Stream<Path> runtimeJavaFiles() throws IOException {
        return Files.walk(QuartzTestSource.backendRoot())
                .filter(path -> path.toString().replace('\\', '/').contains("/src/main/java/"))
                .filter(path -> path.toString().endsWith(".java"));
    }

    private Stream<String> read(Path path) {
        try {
            return Stream.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("读取运行时源码失败: " + path, exception);
        }
    }
}
