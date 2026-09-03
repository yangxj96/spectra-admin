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

package com.devops00.spectra.framework.configure.security.redis;

/**
 * 安全 Redis 固定命名空间。
 *
 * <p>命名空间属于安全数据格式的一部分，不允许通过运行时配置随意改变；变更必须配套迁移和回滚方案。</p>
 */
public final class SecurityRedisNamespace {

    /** 安全 Redis Key 的固定前缀。 */
    public static final String PREFIX = "sec:";

    private SecurityRedisNamespace() {
    }
}
