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

import com.devops00.spectra.common.port.file.FileReferencePermissionChecker;
import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 通过业务模块提供的 Checker 列表解析文件引用权限。
 *
 * <p>Checker 按 Spring 注入顺序逐个检查；任何输入不完整、没有支持者、全部拒绝或 Checker 异常都只能得到拒绝结果。
 * 该实现不缓存权限结果，也不执行额外数据库访问。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
@Component
public final class DefaultFileReferencePermissionResolver implements FileReferencePermissionResolver {

    private static final String DENIED_MESSAGE = "业务引用无权访问";

    private final List<FileReferencePermissionChecker> permissionCheckers;

    /**
     * 注入业务模块提供的文件引用权限检查器，并固定本次应用实例的检查顺序。
     *
     * @param permissionCheckers 支持不同业务引用类型的权限检查器；为 null 时按无检查器处理
     */
    public DefaultFileReferencePermissionResolver(List<FileReferencePermissionChecker> permissionCheckers) {
        this.permissionCheckers = permissionCheckers == null ? List.of() : List.copyOf(permissionCheckers);
    }

    @Override
    public boolean canRead(String referenceType, UUID referenceId, UUID userId) {
        if (referenceType == null || referenceType.isBlank() || referenceId == null || userId == null) {
            return false;
        }
        for (FileReferencePermissionChecker checker : permissionCheckers) {
            try {
                if (checker.supports(referenceType) && checker.canRead(referenceType, referenceId, userId)) {
                    return true;
                }
            } catch (RuntimeException exception) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void requireReadable(String referenceType, UUID referenceId, UUID userId) {
        if (!canRead(referenceType, referenceId, userId)) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, DENIED_MESSAGE);
        }
    }
}
