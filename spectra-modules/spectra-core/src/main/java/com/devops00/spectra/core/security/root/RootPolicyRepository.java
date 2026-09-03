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

package com.devops00.spectra.core.security.root;

/**
 * Root 策略持久化端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public interface RootPolicyRepository {

    /**
     * 在当前事务中锁定 singleton 策略行。
     */
    RootPolicy lock();

    /**
     * 使用 expectedVersion 更新策略。
     */
    void update(RootPolicy policy, long expectedVersion);

    /**
     * 统计当前 ACTIVE、具备有效 Root Assignment 且仍有可用认证标识的主体。
     */
    long countEffectiveDevOpsUsers();
}
