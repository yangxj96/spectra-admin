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

package com.devops00.spectra.core.upload.security;

import java.util.UUID;

/**
 * 解析文件业务引用的读取权限。
 *
 * <p>Resolver 只负责协调业务模块提供的权限检查器，不负责文件资产存在性、管理员权限或文件流读取。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
public interface FileReferencePermissionResolver {

    /**
     * 判断用户是否可以读取指定业务引用关联的文件。
     *
     * @param referenceType 业务引用类型；为空或空白时权限判定失败
     * @param referenceId   业务引用 ID；为 null 时权限判定失败
     * @param userId        当前登录用户 ID；为 null 时权限判定失败
     * @return 至少一个支持该引用类型的检查器明确允许时返回 true；其他情况均返回 false
     */
    boolean canRead(String referenceType, UUID referenceId, UUID userId);

    /**
     * 要求用户可以读取指定业务引用，否则抛出统一文件上传权限异常。
     *
     * @param referenceType 业务引用类型；为空或空白时拒绝访问
     * @param referenceId   业务引用 ID；为 null 时拒绝访问
     * @param userId        当前登录用户 ID；为 null 时拒绝访问
     * @throws com.devops00.spectra.core.upload.api.FileUploadException 权限不足、无匹配检查器或检查器执行失败时抛出
     */
    void requireReadable(String referenceType, UUID referenceId, UUID userId);
}
