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

package com.devops00.spectra.security.base.root;

/**
 * Root 生命周期策略快照。
 *
 * @param minEffectiveDevOpsUsers 允许的最少有效 DEV_OPS 数量
 * @param maxDevOpsUsers          允许的最多 DEV_OPS 数量
 * @param version                 乐观锁版本
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public record RootPolicy(int minEffectiveDevOpsUsers, int maxDevOpsUsers, long version) {

    public static final int DEFAULT_MIN_EFFECTIVE_DEV_OPS_USERS = 1;
    public static final int DEFAULT_MAX_DEV_OPS_USERS = 3;

    public RootPolicy {
        if (minEffectiveDevOpsUsers < 1) {
            throw new IllegalArgumentException("至少必须保留一个有效 DEV_OPS");
        }
        if (maxDevOpsUsers < minEffectiveDevOpsUsers) {
            throw new IllegalArgumentException("DEV_OPS 最大数量不能小于最小数量");
        }
        if (version < 0) {
            throw new IllegalArgumentException("RootPolicy 版本不能为负数");
        }
    }

    /**
     * 查询或获取目标数据（{@code defaults}）。
     */
    public static RootPolicy defaults() {
        return new RootPolicy(DEFAULT_MIN_EFFECTIVE_DEV_OPS_USERS, DEFAULT_MAX_DEV_OPS_USERS, 0L);
    }
}
