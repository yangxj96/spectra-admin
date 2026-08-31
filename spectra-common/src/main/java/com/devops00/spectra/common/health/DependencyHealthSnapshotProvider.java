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

/**
 * 统一健康聚合结果提供者。
 *
 * <p>该契约位于 common，使 framework 的 Actuator 适配器可以消费 core 的聚合实现，同时保持依赖方向向下。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public interface DependencyHealthSnapshotProvider {

    /**
     * 执行一次统一健康聚合。
     *
     * @return 不可变健康快照
     */
    DependencyHealthSnapshot snapshot();
}
