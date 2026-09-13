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

import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.core.upload.javabean.constant.UploadSessionStatus;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.javabean.entity.FileType;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadPart;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.core.upload.mapper.FileAssetMapper;
import com.devops00.spectra.core.upload.mapper.FileTypeMapper;
import com.devops00.spectra.core.upload.mapper.FileUploadPartMapper;
import com.devops00.spectra.core.upload.mapper.FileUploadSessionMapper;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.FileStorageProviderRegistry;
import com.devops00.spectra.core.upload.storage.StorageMultipart;
import com.devops00.spectra.core.upload.storage.StorageObject;
import com.devops00.spectra.core.upload.storage.StoredPart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * 独立的上传校验 Worker，负责对象存储确认、流式摘要校验和失败收敛。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
public class UploadVerificationWorker {

    private final FileUploadProperties properties;
    private final FileTypeMapper fileTypeMapper;
    private final FileAssetMapper fileAssetMapper;
    private final FileUploadSessionMapper sessionMapper;
    private final FileUploadPartMapper partMapper;
    private final FileStorageProviderRegistry providerRegistry;

    public UploadVerificationWorker(FileUploadProperties properties,
                                    FileTypeMapper fileTypeMapper,
                                    FileAssetMapper fileAssetMapper,
                                    FileUploadSessionMapper sessionMapper,
                                    FileUploadPartMapper partMapper,
                                    FileStorageProviderRegistry providerRegistry) {
        this.properties = properties;
        this.fileTypeMapper = fileTypeMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.sessionMapper = sessionMapper;
        this.partMapper = partMapper;
        this.providerRegistry = providerRegistry;
    }

    /** 执行一次上传会话校验；重复投递或非校验状态直接返回。 */
    @Transactional
    public void verify(UUID uploadId) {
        FileUploadSession session = sessionMapper.selectForUpdate(uploadId);
        if (session == null || session.getStatus() != UploadSessionStatus.VERIFYING) {
            return;
        }

        FileStorageProvider provider = null;
        StorageMultipart multipart = toMultipart(session);
        try {
            provider = providerRegistry.require(session.getStorageProvider());
            List<StoredPart> parts = partMapper.findBySessionId(uploadId)
                    .stream()
                    .map(this::toStoredPart)
                    .toList();
            provider.completeMultipart(multipart, parts);

            String actualHash;
            long actualSize;
            try (StorageObject object = provider.open(session.getStorageContainer(), session.getStagingKey(), null, null)) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] buffer = new byte[1024 * 1024];
                long processed = 0;
                int read;
                while ((read = object.stream().read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                    processed += read;
                    sessionMapper.updateVerificationProgress(uploadId, processed);
                }
                actualHash = HexFormat.of().formatHex(digest.digest());
                actualSize = processed;
            }
            if (actualSize != session.getSize() || !actualHash.equalsIgnoreCase(session.getContentSha256())) {
                provider.delete(session.getStorageContainer(), session.getStagingKey());
                sessionMapper.markFailed(uploadId, FileErrorCode.FILE_UPLOAD_HASH_MISMATCH.name(),
                        Instant.now().plus(properties.getRecordRetention()));
                return;
            }

            FileAsset ready = fileAssetMapper.findReady(session.getContentSha256(), session.getSize());
            UUID assetId;
            if (ready != null) {
                provider.delete(session.getStorageContainer(), session.getStagingKey());
                assetId = ready.getId();
            } else {
                assetId = createAsset(session, actualHash, actualSize);
            }
            sessionMapper.markReady(uploadId, assetId, Instant.now());
        } catch (IOException | NoSuchAlgorithmException | RuntimeException exception) {
            abortQuietly(provider, multipart);
            sessionMapper.markFailed(uploadId, FileErrorCode.FILE_STORAGE_UNAVAILABLE.name(),
                    Instant.now().plus(properties.getRecordRetention()));
        }
    }

    /**
     * 构建资产。
     */
    private UUID createAsset(FileUploadSession session, String actualHash, long actualSize) {
        FileType fileType = fileTypeMapper.findEnabledByContentType(session.getDeclaredContentType());
        if (fileType == null) {
            throw new FileUploadException(FileErrorCode.FILE_PART_INVALID, "校验期间文件类型策略已失效");
        }
        var asset = new FileAsset();
        asset.setFileTypeId(fileType.getId());
        asset.setOriginalName(session.getOriginalName());
        asset.setContentSha256(actualHash);
        asset.setSize(actualSize);
        asset.setContentType(session.getDeclaredContentType());
        asset.setStorageProvider(session.getStorageProvider());
        asset.setStorageContainer(session.getStorageContainer());
        asset.setStorageKey(session.getStagingKey());
        asset.setStatus(FileAssetStatus.READY);
        asset.setCompletedAt(Instant.now());
        asset.setCleanupAttempts(0);
        if (fileAssetMapper.insert(asset) != 1 || asset.getId() == null) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_CONFLICT, "文件资产主键生成失败");
        }
        return asset.getId();
    }

    /**
     * 转换分片。
     */
    private StoredPart toStoredPart(FileUploadPart part) {
        return new StoredPart(part.getPartNumber(), part.getUploadedSize() == null ? 0 : part.getUploadedSize(),
                part.getActualSha256(), part.getProviderEtag());
    }

    /**
     * 转换分片上传。
     */
    private StorageMultipart toMultipart(FileUploadSession session) {
        return new StorageMultipart(session.getStorageContainer(), session.getStagingKey(), session.getProviderUploadId());
    }

    /**
     * 取消上传。
     */
    private void abortQuietly(FileStorageProvider provider, StorageMultipart multipart) {
        if (provider == null) {
            return;
        }
        try {
            provider.abortMultipart(multipart);
        } catch (RuntimeException ignored) {
            // next_cleanup_at 负责后续重试清理。
        }
    }
}
