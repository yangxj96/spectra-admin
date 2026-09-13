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

package com.devops00.spectra.common.security.authorization;

import java.util.UUID;

/**
 * 一次资源操作的最小安全上下文。
 *
 * @param subjectId    当前操作者
 * @param resourceCode 资源编码，例如 {@code oa:meeting}
 * @param operation    资源动作
 * @param permission   显式 Permission；为空时由 resourceCode + operation 推导
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record ExecutionContext(UUID subjectId, String resourceCode, ResourceOperation operation, String permission) {

    public ExecutionContext {
        if (subjectId == null) {
            throw new IllegalArgumentException("subjectId 不能为空");
        }
        if (resourceCode == null || resourceCode.isBlank()) {
            throw new IllegalArgumentException("resourceCode 不能为空");
        }
        if (operation == null) {
            throw new IllegalArgumentException("operation 不能为空");
        }
        permission = permission == null || permission.isBlank()
                ? resourceCode + ":" + operation.code()
                : permission;
    }

    /**
     * 创建或构建目标数据（{@code of}）。
     */
    public static ExecutionContext of(UUID subjectId, String resourceCode, ResourceOperation operation) {
        return new ExecutionContext(subjectId, resourceCode, operation, null);
    }
}
