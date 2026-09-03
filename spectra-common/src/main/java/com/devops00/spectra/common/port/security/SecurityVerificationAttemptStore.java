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

package com.devops00.spectra.common.port.security;

import java.time.Duration;

/**
 * 一次性验证码失败尝试计数端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
public interface SecurityVerificationAttemptStore {

    /** 原子递增尝试次数，并在首次写入时设置 TTL。 */
    long increment(String key, Duration ttl);
}
