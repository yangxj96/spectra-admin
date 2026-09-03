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

package com.devops00.spectra.core.security.change;

import java.util.UUID;

/**
 * 高风险授权写的安全版本门禁。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public interface AuthorizationEpochGuard {

    /**
     * 要求版本仍为 expectedVersion。
     */
    void assertCurrent(UUID userId, long expectedVersion);

    /**
     * 以 compare-and-set 方式递增版本；失败表示并发安全变更，必须回滚当前事务。
     */
    void advance(UUID userId, long expectedVersion);
}
