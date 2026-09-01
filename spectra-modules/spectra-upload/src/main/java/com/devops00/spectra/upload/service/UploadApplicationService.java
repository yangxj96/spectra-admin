/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.service;

import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.upload.api.FileErrorCode;
import com.devops00.spectra.upload.api.FileUploadException;
import com.devops00.spectra.upload.configure.FileStorageProviderRegistry;
import com.devops00.spectra.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.upload.javabean.constant.TransportMode;
import com.devops00.spectra.upload.javabean.constant.UploadPartStatus;
import com.devops00.spectra.upload.javabean.constant.UploadSessionStatus;
import com.devops00.spectra.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.upload.javabean.entity.FileAsset;
import com.devops00.spectra.upload.javabean.entity.FileType;
import com.devops00.spectra.upload.javabean.entity.FileUploadPart;
import com.devops00.spectra.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.upload.javabean.from.ConfirmPartRequest;
import com.devops00.spectra.upload.javabean.from.CreateUploadRequest;
import com.devops00.spectra.upload.javabean.from.PartTargetRequest;
import com.devops00.spectra.upload.javabean.vo.PartTargetVO;
import com.devops00.spectra.upload.javabean.vo.UploadSessionVO;
import com.devops00.spectra.upload.mapper.FileAssetMapper;
import com.devops00.spectra.upload.mapper.FileTypeMapper;
import com.devops00.spectra.upload.mapper.FileUploadPartMapper;
import com.devops00.spectra.upload.mapper.FileUploadSessionMapper;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.properties.S3Properties;
import com.devops00.spectra.upload.storage.FileStorageProvider;
import com.devops00.spectra.upload.storage.PartTarget;
import com.devops00.spectra.upload.storage.StorageMultipart;
import com.devops00.spectra.upload.storage.StorageObject;
import com.devops00.spectra.upload.storage.StoredPart;
import com.devops00.spectra.upload.validation.FileDeclarationValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class UploadApplicationService {

    private final FileUploadProperties properties;
    private final S3Properties s3Properties;
    private final FileTypeMapper fileTypeMapper;
    private final FileAssetMapper fileAssetMapper;
    private final FileUploadSessionMapper sessionMapper;
    private final FileUploadPartMapper partMapper;
    private final FileStorageProviderRegistry providerRegistry;
    private final FileDeclarationValidator declarationValidator;
    private final SecurityContextAccessor securityContextAccessor;
    private final TaskExecutor taskExecutor;
    private final ObjectProvider<UploadApplicationService> serviceProvider;
    private final FileUploadConverter fileUploadConverter;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public UploadApplicationService(FileUploadProperties properties,
                                    S3Properties s3Properties,
                                    FileTypeMapper fileTypeMapper,
                                    FileAssetMapper fileAssetMapper,
                                    FileUploadSessionMapper sessionMapper,
                                    FileUploadPartMapper partMapper,
                                    FileStorageProviderRegistry providerRegistry,
                                    FileDeclarationValidator declarationValidator,
                                    SecurityContextAccessor securityContextAccessor,
                                    @Qualifier("fileUploadTaskExecutor") TaskExecutor taskExecutor,
                                    ObjectProvider<UploadApplicationService> serviceProvider,
                                    FileUploadConverter fileUploadConverter) {
        this.properties = properties;
        this.s3Properties = s3Properties;
        this.fileTypeMapper = fileTypeMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.sessionMapper = sessionMapper;
        this.partMapper = partMapper;
        this.providerRegistry = providerRegistry;
        this.declarationValidator = declarationValidator;
        this.securityContextAccessor = securityContextAccessor;
        this.taskExecutor = taskExecutor;
        this.serviceProvider = serviceProvider;
        this.fileUploadConverter = fileUploadConverter;
    }

    @Transactional
    public UploadSessionVO create(CreateUploadRequest request) {
        UUID userId = requireUser();
        FileType type = fileTypeMapper.findEnabledByCode(request.getFileTypeCode());
        declarationValidator.validate(request, type);
        if (request.getSize() > properties.getMaxFileSize()) {
            throw invalid("file exceeds the global limit");
        }
        validateChunkConfiguration();
        Instant now = Instant.now();
        FileAsset ready = fileAssetMapper.findReady(request.getContentSha256().toLowerCase(), request.getSize());
        if (ready != null) {
            return readyResponse(ready);
        }
        FileUploadSession resumable = sessionMapper.findResumable(userId, request.getContentSha256().toLowerCase(), request.getSize(),
                now, now.minus(properties.getIdleTimeout()));
        if (resumable != null) {
            return toView(resumable, "RESUMABLE");
        }
        if (sessionMapper.countActiveByOwner(userId) >= properties.getMaxConcurrentTasksPerUser()) {
            throw conflict("too many active upload tasks for the current user");
        }

        UUID uploadId = UUID.randomUUID();
        long chunkSize = properties.getChunkSize();
        int totalParts = (int) Math.max(1, (request.getSize() + chunkSize - 1) / chunkSize);
        if (totalParts > properties.getMaxParts()) {
            throw invalid("file has too many parts");
        }
        StorageProviderType providerType = properties.getDefaultStorage();
        TransportMode transportMode = providerType == StorageProviderType.S3 ? TransportMode.PRESIGNED : TransportMode.LOCAL_PROXY;
        FileStorageProvider provider = providerRegistry.require(providerType);
        String container = providerType == StorageProviderType.S3 ? s3Properties.getBucket() : "local";
        String key = "assets/" + uploadId + "/content.bin";
        StorageMultipart multipart = provider.createMultipart(uploadId, container, key, totalParts);

        var session = new FileUploadSession();
        session.setId(uploadId);
        session.setOwnerUserId(userId);
        session.setOriginalName(request.getOriginalName().replace('\u0000', '_'));
        session.setDeclaredContentType(request.getContentType().toLowerCase());
        session.setSize(request.getSize());
        session.setContentSha256(request.getContentSha256().toLowerCase());
        session.setChunkSize(chunkSize);
        session.setTotalParts(totalParts);
        session.setStorageProvider(providerType);
        session.setTransportMode(transportMode);
        session.setStorageContainer(container);
        session.setStagingKey(key);
        session.setProviderUploadId(providerType == StorageProviderType.S3 ? multipart.providerUploadId() : uploadId.toString());
        session.setStatus(UploadSessionStatus.UPLOADING);
        session.setExpiresAt(now.plus(properties.getTaskTtl()));
        session.setLastActivityAt(now);
        session.setVerifyProcessedBytes(0L);
        session.setVerifyTotalBytes(request.getSize());
        session.setCleanupAttempts(0);
        sessionMapper.insert(session);

        for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
            var part = new FileUploadPart();
            part.setId(UUID.randomUUID());
            part.setUploadSessionId(uploadId);
            part.setPartNumber(partNumber);
            part.setExpectedSize(expectedPartSize(request.getSize(), chunkSize, partNumber));
            part.setStatus(UploadPartStatus.PENDING);
            part.setUploadAttempt(0);
            partMapper.insert(part);
        }
        return toView(session, "CREATED");
    }

    @Transactional(readOnly = true)
    public UploadSessionVO status(UUID uploadId) {
        FileUploadSession session = requireOwned(uploadId, false);
        return toView(session, null);
    }

    @Transactional
    public PartTargetVO target(UUID uploadId, int partNumber, PartTargetRequest request) {
        FileUploadSession session = requireOwned(uploadId, true);
        ensureUploadable(session);
        FileUploadPart part = requirePart(uploadId, partNumber);
        if (part.getStatus() == UploadPartStatus.CONFIRMED) {
            return new PartTargetVO();
        }
        if (!request.getPartSize().equals(part.getExpectedSize())) {
            throw invalid("part size does not match the session");
        }
        if (partMapper.prepareTarget(uploadId, partNumber, request.getPartSha256().toLowerCase(), part.getUploadAttempt() + 1) != 1) {
            throw conflict("part target changed concurrently");
        }
        FileStorageProvider provider = providerRegistry.require(session.getStorageProvider());
        PartTarget target = provider.createPartTarget(toMultipart(session), partNumber, request.getPartSize(), request.getPartSha256(),
                Instant.now().plus(properties.getPresignTtl()), part.getUploadAttempt() + 1);
        return fileUploadConverter.toPartTargetVO(target);
    }

    @Transactional
    public void putPart(UUID uploadId, int partNumber, InputStream body, long contentLength) {
        FileUploadSession session = requireOwned(uploadId, true);
        ensureUploadable(session);
        if (session.getTransportMode() != TransportMode.LOCAL_PROXY) {
            throw conflict("S3 sessions do not accept proxy uploads");
        }
        FileUploadPart part = requirePart(uploadId, partNumber);
        if (part.getExpectedSha256() == null)
            throw invalid("part target must be requested first");
        if (contentLength >= 0 && contentLength != part.getExpectedSize())
            throw invalid("content length is invalid");
        FileStorageProvider provider = providerRegistry.require(session.getStorageProvider());
        StoredPart stored = provider.putLocalPart(toMultipart(session), partNumber, body, part.getExpectedSize(), part.getExpectedSha256());
        if (partMapper.markUploaded(uploadId, partNumber, stored.size(), stored.sha256(), stored.etag()) != 1) {
            throw conflict("part upload changed concurrently");
        }
    }

    @Transactional
    public void confirm(UUID uploadId, int partNumber, ConfirmPartRequest request) {
        FileUploadSession session = requireOwned(uploadId, true);
        ensureUploadable(session);
        FileUploadPart part = requirePart(uploadId, partNumber);
        if (part.getExpectedSha256() == null
                || !request.getPartSize().equals(part.getExpectedSize())
                || !request.getPartSha256().equalsIgnoreCase(part.getExpectedSha256())) {
            throw new FileUploadException(FileErrorCode.FILE_PART_HASH_MISMATCH, "part declaration differs from target");
        }
        if (part.getStatus() == UploadPartStatus.CONFIRMED)
            return;
        FileStorageProvider provider = providerRegistry.require(session.getStorageProvider());
        StoredPart stored = session.getTransportMode() == TransportMode.PRESIGNED
                ? provider.confirmExternalPart(toMultipart(session), partNumber, request.getPartSize(), request.getPartSha256(),
                        request.getProviderEtag())
                : new StoredPart(partNumber, part.getUploadedSize(), part.getActualSha256(), part.getProviderEtag());
        if (stored.size() != part.getExpectedSize() || !stored.sha256().equalsIgnoreCase(part.getExpectedSha256())) {
            throw new FileUploadException(FileErrorCode.FILE_PART_HASH_MISMATCH, "stored part differs from declaration");
        }
        int updated = session.getTransportMode() == TransportMode.PRESIGNED
                ? partMapper.markExternalConfirmed(uploadId, partNumber, stored.size(), stored.sha256(), stored.etag())
                : partMapper.markConfirmed(uploadId, partNumber, stored.size(), stored.sha256(), stored.etag());
        if (updated != 1) {
            throw conflict("part confirmation changed concurrently");
        }
        // Only a successful confirmation advances the idle timeout.
        sessionMapper.touchActivity(uploadId, Instant.now());
    }

    @Transactional
    public UploadSessionVO complete(UUID uploadId) {
        FileUploadSession session = requireOwned(uploadId, true);
        if (session.getStatus() == UploadSessionStatus.READY || session.getStatus() == UploadSessionStatus.VERIFYING) {
            return toView(session, null);
        }
        ensureUploadable(session);
        if (partMapper.countConfirmed(uploadId) != session.getTotalParts()) {
            throw conflict("not all parts are confirmed");
        }
        if (sessionMapper.claimForVerification(uploadId, Instant.now()) != 1) {
            return toView(sessionMapper.selectForUpdate(uploadId), null);
        }
        scheduleVerification(uploadId);
        session.setStatus(UploadSessionStatus.VERIFYING);
        session.setVerifyProcessedBytes(0L);
        session.setVerifyTotalBytes(session.getSize());
        return toView(session, null);
    }

    @Transactional
    public void cancel(UUID uploadId) {
        FileUploadSession session = requireOwned(uploadId, true);
        if (session.getStatus() == UploadSessionStatus.CANCELED
                || session.getStatus() == UploadSessionStatus.EXPIRED
                || session.getStatus() == UploadSessionStatus.FAILED
                || session.getStatus() == UploadSessionStatus.CLEANED)
            return;
        if (session.getStatus() != UploadSessionStatus.UPLOADING)
            throw conflict("verifying upload cannot be canceled");
        if (sessionMapper.markCanceled(uploadId, Instant.now().plus(properties.getRecordRetention())) != 1)
            return;
        providerRegistry.require(session.getStorageProvider()).abortMultipart(toMultipart(session));
    }

    private void scheduleVerification(UUID uploadId) {
        Runnable verification = () -> taskExecutor.execute(() -> serviceProvider.getObject().verify(uploadId));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    verification.run();
                }
            });
        } else {
            verification.run();
        }
    }

    @Transactional
    public void verify(UUID uploadId) {
        FileUploadSession session = sessionMapper.selectForUpdate(uploadId);
        if (session == null || session.getStatus() != UploadSessionStatus.VERIFYING)
            return;
        FileStorageProvider provider = providerRegistry.require(session.getStorageProvider());
        StorageMultipart multipart = toMultipart(session);
        List<FileUploadPart> entities = partMapper.findBySessionId(uploadId);
        List<StoredPart> parts = entities.stream()
                .map(part -> new StoredPart(part.getPartNumber(), part.getUploadedSize(),
                        part.getActualSha256(), part.getProviderEtag()))
                .toList();
        try {
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
                var asset = new FileAsset();
                asset.setId(UUID.randomUUID());
                FileType fileType = fileTypeMapper.findEnabledByContentType(session.getDeclaredContentType());
                if (fileType == null) {
                    throw new FileUploadException(FileErrorCode.FILE_PART_INVALID, "file type policy disappeared during verification");
                }
                asset.setFileTypeId(fileType.getId());
                asset.setOriginalName(session.getOriginalName());
                asset.setContentSha256(actualHash);
                asset.setSize(actualSize);
                asset.setContentType(session.getDeclaredContentType());
                asset.setStorageProvider(session.getStorageProvider());
                asset.setStorageContainer(session.getStorageContainer());
                asset.setStorageKey(session.getStagingKey());
                asset.setStatus(com.devops00.spectra.upload.javabean.constant.FileAssetStatus.READY);
                asset.setCompletedAt(Instant.now());
                asset.setCleanupAttempts(0);
                fileAssetMapper.insert(asset);
                assetId = asset.getId();
            }
            sessionMapper.markReady(uploadId, assetId, Instant.now());
        } catch (IOException | NoSuchAlgorithmException | RuntimeException e) {
            try {
                provider.abortMultipart(multipart);
            } catch (RuntimeException ignored) {
            }
            sessionMapper.markFailed(uploadId, FileErrorCode.FILE_STORAGE_UNAVAILABLE.name(), Instant.now().plus(properties.getRecordRetention()));
        }
    }

    private FileUploadSession requireOwned(UUID uploadId, boolean lock) {
        UUID userId = requireUser();
        FileUploadSession session = lock ? sessionMapper.selectForUpdate(uploadId) : sessionMapper.selectById(uploadId);
        if (session == null)
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_NOT_FOUND, "upload session not found");
        if (!userId.equals(session.getOwnerUserId()))
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "upload session is not owned by user");
        if (session.getExpiresAt() != null
                && session.getExpiresAt().isBefore(Instant.now())
                && session.getStatus() == UploadSessionStatus.UPLOADING) {
            sessionMapper.markExpired(uploadId, Instant.now().plus(properties.getRecordRetention()));
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_EXPIRED, "upload session has expired");
        }
        return session;
    }

    private void ensureUploadable(FileUploadSession session) {
        if (session.getStatus() == UploadSessionStatus.EXPIRED)
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_EXPIRED, "upload session has expired");
        if (session.getStatus() != UploadSessionStatus.UPLOADING)
            throw conflict("upload session is not accepting parts");
    }

    private FileUploadPart requirePart(UUID uploadId, int partNumber) {
        if (partNumber < 1)
            throw invalid("part number must start at one");
        FileUploadPart part = partMapper.selectForUpdate(uploadId, partNumber);
        if (part == null)
            throw new FileUploadException(FileErrorCode.FILE_PART_INVALID, "part does not belong to the session");
        return part;
    }

    private StorageMultipart toMultipart(FileUploadSession session) {
        return new StorageMultipart(session.getStorageContainer(), session.getStagingKey(), session.getProviderUploadId());
    }

    private UploadSessionVO readyResponse(FileAsset asset) {
        var response = fileUploadConverter.toDeduplicatedUploadVO(asset);
        response.setResult("DEDUPLICATED");
        response.setStatus(UploadSessionStatus.READY);
        response.setVerificationProgress(100);
        return response;
    }

    private UploadSessionVO toView(FileUploadSession session, String result) {
        var response = fileUploadConverter.toUploadSessionVO(session);
        response.setResult(result);
        var parts = partMapper.findBySessionId(session.getId());
        response.setCompletedParts(
                parts.stream().filter(part -> part.getStatus() == UploadPartStatus.CONFIRMED).map(FileUploadPart::getPartNumber).toList());
        response.setUploadedBytes(parts.stream()
                .filter(part -> part.getStatus() == UploadPartStatus.CONFIRMED)
                .mapToLong(part -> part.getUploadedSize() == null ? 0 : part.getUploadedSize())
                .sum());
        response.setVerificationProgress(session.getVerifyTotalBytes() == null || session.getVerifyTotalBytes() == 0
                ? 0
                : (int) Math.min(100, session.getVerifyProcessedBytes() * 100 / session.getVerifyTotalBytes()));
        return response;
    }

    private UUID requireUser() {
        UUID userId = securityContextAccessor.currentUserId();
        if (userId == null)
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "authentication is required");
        return userId;
    }

    private void validateChunkConfiguration() {
        if (properties.getChunkSize() < properties.getMinChunkSize()
                || properties.getChunkSize() > properties.getMaxChunkSize()
                || properties.getMaxParts() < 1
                || properties.getParallelism() < 1)
            throw invalid("upload configuration is invalid");
    }

    private static long expectedPartSize(long totalSize, long chunkSize, int partNumber) {
        long offset = (partNumber - 1) * chunkSize;
        return Math.max(0, Math.min(chunkSize, totalSize - offset));
    }

    private static FileUploadException invalid(String message) {
        return new FileUploadException(FileErrorCode.FILE_PART_INVALID, message);
    }

    private static FileUploadException conflict(String message) {
        return new FileUploadException(FileErrorCode.FILE_UPLOAD_CONFLICT, message);
    }
}
