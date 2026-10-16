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

package com.devops00.spectra.framework.health.readiness;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ReadinessHealthIndicator} 健康状态反馈的行为测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class ReadinessHealthIndicatorTest {

    @Test
    void readyReportsUp() {
        var state = new DependencyReadinessState();
        assertEquals("UP", new ReadinessHealthIndicator(state).health().getStatus().getCode());
    }

    @Test
    void unreadyReportsOutOfServiceWithReason() {
        var state = new DependencyReadinessState();
        state.markUnready("SQLException");
        var health = new ReadinessHealthIndicator(state).health();
        assertEquals("OUT_OF_SERVICE", health.getStatus().getCode());
        assertEquals("SQLException", health.getDetails().get("reason"));
    }
}
