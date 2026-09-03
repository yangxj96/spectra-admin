/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.notification.controller;

import com.devops00.spectra.common.notification.NotificationChannel;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Provider 回执入口的 API 版本契约测试。 */
class NotificationProviderCallbackControllerTest {

    @Test
    void callbackUsesCurrentApiVersion() throws NoSuchMethodException {
        var method = NotificationProviderCallbackController.class.getMethod(
                "callback", NotificationChannel.class, String.class, String.class);
        var mapping = method.getAnnotation(PostMapping.class);

        assertEquals("1.0.0", mapping.version());
    }
}
