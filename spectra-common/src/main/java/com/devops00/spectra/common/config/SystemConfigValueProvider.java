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

package com.devops00.spectra.common.config;

import java.util.Optional;

/**
 * 提供运行时系统配置的最小公共契约。
 * <p>
 * 业务模块只依赖该契约，不直接依赖 Core 的系统配置实现，允许配置在首次引导后动态从数据库读取。
 */
@FunctionalInterface
public interface SystemConfigValueProvider {

    /**
     * 按配置键读取非空值。
     *
     * @param key 配置键
     * @return 配置值
     */
    Optional<String> find(String key);
}
