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

package com.devops00.spectra.common.event;

import java.util.UUID;

/**
 * 文件上传完成后在模块边界传递的数据载体。
 *
 * <p>该类型只表达已经完成上传的文件标识，不继承 Spring 事件类型；具体发布机制由使用方的
 * framework 或 core 适配器负责，因此 common 不需要依赖事件框架。</p>
 *
 * @param fileId 已完成上传文件的持久化标识；事件本身不生成、不校验该标识
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/15 17:21
 */
public record FileUploadFinishEvent(UUID fileId) {
}
