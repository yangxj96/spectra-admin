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

package com.devops00.spectra.core.system.controller;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.system.javabean.from.ConfiguredFrom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 防止系统配置秘密值进入审计快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class ConfiguredControllerAuditTest {

    @Test
    void shouldNotCaptureConfiguredValuesInAuditSnapshots() throws NoSuchMethodException {
        var method = ConfiguredController.class.getDeclaredMethod("modify", ConfiguredFrom.class);
        var audit = method.getAnnotation(Audit.class);

        assertFalse(audit.captureArguments());
    }
}
