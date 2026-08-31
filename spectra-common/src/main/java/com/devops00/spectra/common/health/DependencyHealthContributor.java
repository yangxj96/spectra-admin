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

package com.devops00.spectra.common.health;

import java.time.Duration;

/**
 * 单个依赖或明确模块能力的同步健康检查 contributor。
 *
 * <p>一个 contributor 只负责一个依赖或一个清晰的模块能力，不负责聚合其他 contributor。检查调用
 * 必须是同步的，并且实现应在 {@link #timeout()} 声明的预算内返回；超时控制由 framework/Core 调用方
 * 负责，超时结果必须映射为 {@link DependencyHealthStatus#DOWN}，无法可靠判断时才使用 UNKNOWN。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public interface DependencyHealthContributor {

    /**
     * 返回系统内唯一的 contributor 名称；不同模块不得注册同名 contributor。
     *
     * @return contributor 名称
     */
    String contributorName();

    /**
     * 返回所属模块名称，例如 core、notification 或 upload。
     *
     * @return 模块名称
     */
    String moduleName();

    /**
     * 返回被检查依赖的稳定类型，不得包含连接串或实例凭据。
     *
     * @return 依赖类型
     */
    String dependencyType();

    /**
     * 返回单次同步检查的最大时间预算。
     *
     * @return 正的超时时间
     */
    Duration timeout();

    /**
     * 同步执行检查并返回统一结果。不得返回明文凭据、连接串、原始异常消息或堆栈。
     *
     * @return 健康检查结果
     */
    DependencyHealthResult check();
}
