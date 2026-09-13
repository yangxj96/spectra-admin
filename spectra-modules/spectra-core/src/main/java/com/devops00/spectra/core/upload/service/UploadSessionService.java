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
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.javabean.constant.TransportMode;
import com.devops00.spectra.core.upload.javabean.constant.UploadPartStatus;
import com.devops00.spectra.core.upload.javabean.constant.UploadSessionStatus;
import com.devops00.spectra.core.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.javabean.entity.FileType;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadPart;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.core.upload.javabean.from.CreateUploadRequest;
import com.devops00.spectra.core.upload.javabean.vo.UploadSessionVO;
import com.devops00.spectra.core.upload.mapper.FileAssetMapper;
import com.devops00.spectra.core.upload.mapper.FileTypeMapper;
import com.devops00.spectra.core.upload.mapper.FileUploadSessionMapper;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.properties.S3Properties;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.FileStorageProviderRegistry;
import com.devops00.spectra.core.upload.storage.StorageMultipart;
import com.devops00.spectra.core.upload.validator.FileDeclarationValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * 负责上传会话创建、幂等和状态转换。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
public class UploadSessionService {

    private final FileUploadProperties properties;
    private final S3Properties s3Properties;
    private final FileTypeMapper fileTypeMapper;
    private final FileAssetMapper fileAssetMapper;
    private final FileUploadSessionMapper sessionMapper;
    private final FileStorageProviderRegistry providerRegistry;
    private final FileDeclarationValidator declarationValidator;
    private final UploadPartService partService;
    private final FileUploadConverter fileUploadConverter;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public UploadSessionService(FileUploadProperties properties,
                                S3Properties s3Properties,
                                FileTypeMapper fileTypeMapper,
                                FileAssetMapper fileAssetMapper,
                                FileUploadSessionMapper sessionMapper,
                                FileStorageProviderRegistry providerRegistry,
                                FileDeclarationValidator declarationValidator,
                                UploadPartService partService,
                                FileUploadConverter fileUploadConverter) {
        this.properties = properties;
        this.s3Properties = s3Properties;
        this.fileTypeMapper = fileTypeMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.sessionMapper = sessionMapper;
        this.providerRegistry = providerRegistry;
        this.declarationValidator = declarationValidator;
        this.partService = partService;
        this.fileUploadConverter = fileUploadConverter;
    }

    /** 创建新的上传会话，或返回当前用户可恢复的会话。 */
    @Transactional
    public UploadSessionVO create(CreateUploadRequest request, UUID userId) {
        String sha256 = request.getContentSha256().toLowerCase(Locale.ROOT);
        String contentType = request.getContentType().toLowerCase(Locale.ROOT);
        FileType type = fileTypeMapper.findEnabledByCode(request.getFileTypeCode());
        declarationValidator.validate(request, type);
        if (request.getSize() > properties.getMaxFileSize()) {
            throw invalid("文件大小超过系统限制");
        }
        validateChunkConfiguration();
        Instant now = Instant.now();
        FileAsset ready = fileAssetMapper.findReady(sha256, request.getSize());
        if (ready != null) {
            return readyResponse(ready);
        }
        FileUploadSession resumable = sessionMapper.findResumable(userId, sha256, request.getSize(), now,
                now.minus(properties.getIdleTimeout()));
        if (resumable != null) {
            return toView(resumable, "RESUMABLE");
        }
        if (sessionMapper.countActiveByOwner(userId) >= properties.getMaxConcurrentTasksPerUser()) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_CONCURRENCY_LIMIT, "当前用户的活动上传任务已达到上限");
        }

        long chunkSize = properties.getChunkSize();
        int totalParts = (int) Math.max(1, (request.getSize() + chunkSize - 1) / chunkSize);
        if (totalParts > properties.getMaxParts()) {
            throw invalid("文件分片数量超过系统限制");
        }
        StorageProviderType providerType = properties.getDefaultStorage();
        TransportMode transportMode = providerType == StorageProviderType.S3
                ? TransportMode.PRESIGNED
                : TransportMode.LOCAL_PROXY;
        FileStorageProvider provider = providerRegistry.require(providerType);

        // storageSessionId 只用于存储 Key 和 Provider 会话，数据库实体主键由 MetaObjectHandler 生成。
        UUID storageSessionId = UUID.randomUUID();
        String container = providerType == StorageProviderType.S3 ? s3Properties.getBucket() : "local";
        String key = "assets/" + storageSessionId + "/content.bin";
        StorageMultipart multipart;
        try {
            multipart = provider.createMultipart(storageSessionId, container, key, totalParts);
        } catch (FileUploadException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new FileUploadException(FileErrorCode.FILE_STORAGE_UNAVAILABLE, "创建文件存储会话失败", exception);
        }

        var session = new FileUploadSession();
        session.setOwnerUserId(userId);
        session.setOriginalName(request.getOriginalName().replace('\u0000', '_'));
        session.setDeclaredContentType(contentType);
        session.setSize(request.getSize());
        session.setContentSha256(sha256);
        session.setChunkSize(chunkSize);
        session.setTotalParts(totalParts);
        session.setStorageProvider(providerType);
        session.setTransportMode(transportMode);
        session.setStorageContainer(container);
        session.setStagingKey(key);
        session.setProviderUploadId(multipart.providerUploadId());
        session.setStatus(UploadSessionStatus.UPLOADING);
        session.setExpiresAt(now.plus(properties.getTaskTtl()));
        session.setLastActivityAt(now);
        session.setVerifyProcessedBytes(0L);
        session.setVerifyTotalBytes(request.getSize());
        session.setCleanupAttempts(0);
        try {
            if (sessionMapper.insert(session) != 1 || session.getId() == null) {
                throw new FileUploadException(FileErrorCode.FILE_UPLOAD_CONFLICT, "上传会话主键生成失败");
            }
            partService.createParts(session.getId(), request.getSize(), chunkSize, totalParts);
        } catch (RuntimeException exception) {
            abortQuietly(provider, multipart);
            throw exception;
        }
        return toView(session, "CREATED");
    }

    /** 查询当前用户有权访问的上传会话。 */
    @Transactional(readOnly = true)
    public UploadSessionVO status(UUID uploadId, UUID userId) {
        return toView(requireOwned(uploadId, false, userId), null);
    }

    /** 将已确认的分片收敛为待校验状态。 */
    @Transactional
    public UploadSessionVO complete(UUID uploadId, UUID userId) {
        return completeForApplication(uploadId, userId).response();
    }

    Completion completeForApplication(UUID uploadId, UUID userId) {
        FileUploadSession session = requireOwned(uploadId, true, userId);
        if (session.getStatus() == UploadSessionStatus.READY
                || session.getStatus() == UploadSessionStatus.VERIFYING) {
            return new Completion(toView(session, null), false);
        }
        ensureUploadable(session);
        if (partService.countConfirmed(uploadId) != session.getTotalParts()) {
            throw conflict("并非所有分片都已确认");
        }
        if (sessionMapper.claimForVerification(uploadId, Instant.now()) != 1) {
            FileUploadSession current = sessionMapper.selectForUpdate(uploadId);
            if (current == null) {
                throw notFound();
            }
            return new Completion(toView(current, null), false);
        }
        session.setStatus(UploadSessionStatus.VERIFYING);
        session.setVerifyProcessedBytes(0L);
        session.setVerifyTotalBytes(session.getSize());
        return new Completion(toView(session, null), true);
    }

    /** 取消上传会话并将存储清理由后台清理流程接管。 */
    @Transactional
    public void cancel(UUID uploadId, UUID userId) {
        FileUploadSession session = requireOwned(uploadId, true, userId);
        if (session.getStatus() == UploadSessionStatus.CANCELED
                || session.getStatus() == UploadSessionStatus.EXPIRED
                || session.getStatus() == UploadSessionStatus.FAILED
                || session.getStatus() == UploadSessionStatus.CLEANED) {
            return;
        }
        if (session.getStatus() != UploadSessionStatus.UPLOADING) {
            throw conflict("校验中的上传会话不能取消");
        }
        if (sessionMapper.markCanceled(uploadId, Instant.now().plus(properties.getRecordRetention())) != 1) {
            return;
        }
        abortQuietly(providerRegistry.require(session.getStorageProvider()), toMultipart(session));
    }

    /** 更新成功确认分片后的会话活跃时间。 */
    public void touchActivity(UUID uploadId) {
        sessionMapper.touchActivity(uploadId, Instant.now());
    }

    /** 校验会话是否仍处于接收分片状态。 */
    public void ensureUploadable(FileUploadSession session) {
        if (session.getStatus() == UploadSessionStatus.EXPIRED) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_EXPIRED, "上传会话已过期");
        }
        if (session.getStatus() != UploadSessionStatus.UPLOADING) {
            throw conflict("上传会话当前不接收分片");
        }
    }

    /** 按归属用户加载并可选锁定上传会话。 */
    public FileUploadSession requireOwned(UUID uploadId, boolean lock, UUID userId) {
        FileUploadSession session = lock ? sessionMapper.selectForUpdate(uploadId) : sessionMapper.selectById(uploadId);
        if (session == null) {
            throw notFound();
        }
        if (!userId.equals(session.getOwnerUserId())) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "上传会话不属于当前用户");
        }
        if (session.getExpiresAt() != null
                && session.getExpiresAt().isBefore(Instant.now())
                && session.getStatus() == UploadSessionStatus.UPLOADING) {
            sessionMapper.markExpired(uploadId, Instant.now().plus(properties.getRecordRetention()));
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_EXPIRED, "上传会话已过期");
        }
        return session;
    }

    StorageMultipart toMultipart(FileUploadSession session) {
        return new StorageMultipart(session.getStorageContainer(), session.getStagingKey(), session.getProviderUploadId());
    }

    /**
     * 处理就绪状态响应相关数据。
     */
    private UploadSessionVO readyResponse(FileAsset asset) {
        var response = fileUploadConverter.toDeduplicatedUploadVO(asset);
        response.setResult("DEDUPLICATED");
        response.setStatus(UploadSessionStatus.READY);
        response.setVerificationProgress(100);
        return response;
    }

    /**
     * 转换视图。
     */
    private UploadSessionVO toView(FileUploadSession session, String result) {
        var response = fileUploadConverter.toUploadSessionVO(session);
        response.setResult(result);
        var parts = partService.findBySessionId(session.getId());
        response.setCompletedParts(parts.stream()
                .filter(part -> part.getStatus() == UploadPartStatus.CONFIRMED)
                .map(FileUploadPart::getPartNumber)
                .toList());
        response.setUploadedBytes(parts.stream()
                .filter(part -> part.getStatus() == UploadPartStatus.CONFIRMED)
                .mapToLong(part -> part.getUploadedSize() == null ? 0 : part.getUploadedSize())
                .sum());
        response.setVerificationProgress(session.getVerifyTotalBytes() == null || session.getVerifyTotalBytes() == 0
                ? 0
                : (int) Math.min(100, session.getVerifyProcessedBytes() * 100 / session.getVerifyTotalBytes()));
        return response;
    }

    /**
     * 校验配置。
     */
    private void validateChunkConfiguration() {
        if (properties.getChunkSize() < properties.getMinChunkSize()
                || properties.getChunkSize() > properties.getMaxChunkSize()
                || properties.getMaxParts() < 1
                || properties.getParallelism() < 1) {
            throw invalid("上传分片配置无效");
        }
    }

    /**
     * 取消上传会话。
     */
    private void abortQuietly(FileStorageProvider provider, StorageMultipart multipart) {
        try {
            provider.abortMultipart(multipart);
        } catch (RuntimeException ignored) {
            // 数据库中的失败/取消状态和 next_cleanup_at 负责后续重试清理。
        }
    }

    /**
     * 处理不相关数据。
     */
    private static FileUploadException notFound() {
        return new FileUploadException(FileErrorCode.FILE_UPLOAD_NOT_FOUND, "上传会话不存在");
    }

    /**
     * 处理上传会话相关数据。
     */
    private static FileUploadException invalid(String message) {
        return new FileUploadException(FileErrorCode.FILE_PART_INVALID, message);
    }

    /**
     * 处理冲突相关数据。
     */
    private static FileUploadException conflict(String message) {
        return new FileUploadException(FileErrorCode.FILE_UPLOAD_CONFLICT, message);
    }

    /**
     * 定义相关数据相关的应用服务契约。
     *
     * @param response            上传完成后提供给调用方的响应信息
     * @param verificationClaimed 上传会话是否已声明完成校验
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    record Completion(UploadSessionVO response, boolean verificationClaimed) {
    }
}
