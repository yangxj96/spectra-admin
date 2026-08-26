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

package com.devops00.spectra.common.scheduler;

/**
 * OPS/SYSTEM 离散任务处理器契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/26
 */
public interface ScheduledJobHandler {

    /**
     * 返回代码注册描述。
     *
     * @return 任务描述
     */
    ScheduledJobDescriptor descriptor();

    /**
     * 执行一次离散任务。
     *
     * @param context 执行上下文
     * @return 脱敏执行结果
     */
    ScheduledJobResult execute(ScheduledJobContext context);
}
