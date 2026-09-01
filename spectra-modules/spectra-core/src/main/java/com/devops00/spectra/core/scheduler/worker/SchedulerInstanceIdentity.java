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

package com.devops00.spectra.core.scheduler.worker;

import org.springframework.stereotype.Component;

import java.util.UUID;

/** 单体应用进程级实例身份；不使用主机名或可变网络地址。 */
@Component
public final class SchedulerInstanceIdentity {

    private final String value;

    public SchedulerInstanceIdentity() {
        this(UUID.randomUUID().toString());
    }

    public SchedulerInstanceIdentity(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("调度实例 ID 不能为空");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
