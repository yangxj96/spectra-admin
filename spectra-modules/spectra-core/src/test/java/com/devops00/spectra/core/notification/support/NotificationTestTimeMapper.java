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

package com.devops00.spectra.core.notification.support;

import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 通知单元测试统一使用 UTC 用户时区的时间转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public final class NotificationTestTimeMapper {

    private NotificationTestTimeMapper() {
    }

    public static TimeMapper create() {
        var accessor = mock(SecurityContextAccessor.class);
        when(accessor.currentUserZoneId()).thenReturn("UTC");
        return new TimeMapper(accessor);
    }
}
