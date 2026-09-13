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

package com.devops00.spectra.core.upload.service;

import com.devops00.spectra.common.port.file.FileReferenceCommand;
import com.devops00.spectra.common.port.file.FileReferenceKey;
import com.devops00.spectra.common.port.file.FileReferenceService;
import com.devops00.spectra.common.port.file.FileReferenceView;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.upload.javabean.entity.FileReference;
import com.devops00.spectra.core.upload.mapper.FileReferenceMapper;
import com.devops00.spectra.core.upload.security.FileReferencePermissionResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 定义文件引用申请相关的应用服务契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@RequiredArgsConstructor
public class FileReferenceApplicationService implements FileReferenceService {

    private final FileReferenceMapper referenceMapper;
    private final FileAssetApplicationService assetService;
    private final SecurityContextAccessor securityContextAccessor;
    private final FileReferencePermissionResolver permissionResolver;

    @Override
    @Transactional
    public FileReferenceView register(FileReferenceCommand command) {
        requireBusinessPermission(command.referenceType(), command.referenceId());
        assetService.requireReadyForReference(command.fileAssetId(), null);
        FileReference existing = referenceMapper.findByKey(command.fileAssetId(), command.referenceType(), command.referenceId(), command.purpose());
        if (existing != null)
            return view(existing);
        var reference = new FileReference();
        reference.setFileAssetId(command.fileAssetId());
        reference.setReferenceType(command.referenceType());
        reference.setReferenceId(command.referenceId());
        reference.setPurpose(command.purpose());
        reference.setDisplayName(command.displayName());
        referenceMapper.insert(reference);
        return view(reference);
    }

    @Override
    @Transactional
    public void remove(FileReferenceKey key) {
        requireBusinessPermission(key.referenceType(), key.referenceId());
        referenceMapper.softDeleteByBusinessKeyAndPurpose(key.referenceType(), key.referenceId(), key.purpose());
    }

    @Override
    @Transactional
    public void removeById(UUID referenceId) {
        FileReference reference = referenceMapper.selectById(referenceId);
        if (reference == null || reference.getDeleted() != null)
            return;
        requireBusinessPermission(reference.getReferenceType(), reference.getReferenceId());
        referenceMapper.softDeleteById(referenceId);
    }

    @Override
    @Transactional
    public void removeByReference(String referenceType, UUID referenceId) {
        requireBusinessPermission(referenceType, referenceId);
        referenceMapper.softDeleteByBusinessKey(referenceType, referenceId);
    }

    /**
     * 处理视图相关数据。
     */
    private FileReferenceView view(FileReference reference) {
        return new FileReferenceView(reference.getId(), reference.getFileAssetId(), reference.getReferenceType(),
                reference.getReferenceId(), reference.getPurpose(), reference.getDisplayName());
    }

    /**
     * 校验业务权限。
     */
    private void requireBusinessPermission(String referenceType, UUID referenceId) {
        UUID userId = securityContextAccessor.currentUserId();
        permissionResolver.requireReadable(referenceType, referenceId, userId);
    }
}
