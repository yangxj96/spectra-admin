/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.service;

import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.configure.FileStorageProviderRegistry;
import com.devops00.spectra.core.upload.javabean.constant.TransportMode;
import com.devops00.spectra.core.upload.javabean.constant.UploadPartStatus;
import com.devops00.spectra.core.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadPart;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.core.upload.javabean.from.ConfirmPartRequest;
import com.devops00.spectra.core.upload.javabean.from.PartTargetRequest;
import com.devops00.spectra.core.upload.javabean.vo.PartTargetVO;
import com.devops00.spectra.core.upload.mapper.FileUploadPartMapper;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.PartTarget;
import com.devops00.spectra.core.upload.storage.StorageMultipart;
import com.devops00.spectra.core.upload.storage.StoredPart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** 负责上传分片的元数据、上传目标和状态变更。 */
@Service
public class UploadPartService {

    private final FileUploadPartMapper partMapper;
    private final FileStorageProviderRegistry providerRegistry;
    private final FileUploadProperties properties;
    private final FileUploadConverter fileUploadConverter;

    public UploadPartService(FileUploadPartMapper partMapper,
                             FileStorageProviderRegistry providerRegistry,
                             FileUploadProperties properties,
                             FileUploadConverter fileUploadConverter) {
        this.partMapper = partMapper;
        this.providerRegistry = providerRegistry;
        this.properties = properties;
        this.fileUploadConverter = fileUploadConverter;
    }

    /** 按固定分片布局创建分片元数据，分片主键由 MetaObjectHandler 生成。 */
    public void createParts(UUID uploadSessionId, long totalSize, long chunkSize, int totalParts) {
        for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
            var part = new FileUploadPart();
            part.setUploadSessionId(uploadSessionId);
            part.setPartNumber(partNumber);
            part.setExpectedSize(expectedPartSize(totalSize, chunkSize, partNumber));
            part.setStatus(UploadPartStatus.PENDING);
            part.setUploadAttempt(0);
            if (partMapper.insert(part) != 1 || part.getId() == null) {
                throw new FileUploadException(FileErrorCode.FILE_UPLOAD_CONFLICT, "上传分片主键生成失败");
            }
        }
    }

    /** 为指定分片生成本地代理或预签名上传目标。 */
    @Transactional
    public PartTargetVO target(FileUploadSession session, int partNumber, PartTargetRequest request) {
        FileUploadPart part = requirePart(session.getId(), partNumber);
        if (part.getStatus() == UploadPartStatus.CONFIRMED) {
            return new PartTargetVO();
        }
        if (!request.getPartSize().equals(part.getExpectedSize())) {
            throw invalid("分片大小与上传会话不一致");
        }
        int attempt = nextAttempt(part);
        String sha256 = request.getPartSha256().toLowerCase(Locale.ROOT);
        if (partMapper.prepareTarget(session.getId(), partNumber, sha256, attempt) != 1) {
            throw conflict("分片上传目标已被并发修改");
        }
        FileStorageProvider provider = providerRegistry.require(session.getStorageProvider());
        PartTarget target = provider.createPartTarget(toMultipart(session), partNumber, request.getPartSize(), request.getPartSha256(),
                Instant.now().plus(properties.getPresignTtl()), attempt);
        return fileUploadConverter.toPartTargetVO(target);
    }

    /** 写入本地代理分片并保存对象存储返回的元数据。 */
    @Transactional
    public void putPart(FileUploadSession session, int partNumber, InputStream body, long contentLength) {
        if (session.getTransportMode() != TransportMode.LOCAL_PROXY) {
            throw conflict("S3 上传会话不接受代理分片写入");
        }
        FileUploadPart part = requirePart(session.getId(), partNumber);
        if (part.getExpectedSha256() == null) {
            throw invalid("请先申请分片上传目标");
        }
        if (contentLength >= 0 && contentLength != part.getExpectedSize()) {
            throw invalid("分片内容长度无效");
        }
        FileStorageProvider provider = providerRegistry.require(session.getStorageProvider());
        StoredPart stored = provider.putLocalPart(toMultipart(session), partNumber, body, part.getExpectedSize(), part.getExpectedSha256());
        if (partMapper.markUploaded(session.getId(), partNumber, stored.size(), stored.sha256(), stored.etag()) != 1) {
            throw conflict("分片上传状态已被并发修改");
        }
    }

    /** 校验并确认单个分片。 */
    @Transactional
    public void confirm(FileUploadSession session, int partNumber, ConfirmPartRequest request) {
        FileUploadPart part = requirePart(session.getId(), partNumber);
        if (part.getExpectedSha256() == null
                || !request.getPartSize().equals(part.getExpectedSize())
                || !request.getPartSha256().equalsIgnoreCase(part.getExpectedSha256())) {
            throw new FileUploadException(FileErrorCode.FILE_PART_HASH_MISMATCH, "分片声明与上传目标不一致");
        }
        if (part.getStatus() == UploadPartStatus.CONFIRMED) {
            return;
        }
        FileStorageProvider provider = providerRegistry.require(session.getStorageProvider());
        StoredPart stored = session.getTransportMode() == TransportMode.PRESIGNED
                ? provider.confirmExternalPart(toMultipart(session), partNumber, request.getPartSize(), request.getPartSha256(),
                        request.getProviderEtag())
                : new StoredPart(partNumber, part.getUploadedSize() == null ? 0 : part.getUploadedSize(),
                        part.getActualSha256(), part.getProviderEtag());
        if (stored.sha256() == null
                || stored.size() != part.getExpectedSize()
                || !stored.sha256().equalsIgnoreCase(part.getExpectedSha256())) {
            throw new FileUploadException(FileErrorCode.FILE_PART_HASH_MISMATCH, "存储分片与声明不一致");
        }
        int updated = session.getTransportMode() == TransportMode.PRESIGNED
                ? partMapper.markExternalConfirmed(session.getId(), partNumber, stored.size(), stored.sha256(), stored.etag())
                : partMapper.markConfirmed(session.getId(), partNumber, stored.size(), stored.sha256(), stored.etag());
        if (updated != 1) {
            throw conflict("分片确认状态已被并发修改");
        }
    }

    /** 统计已确认分片数量。 */
    public int countConfirmed(UUID uploadSessionId) {
        return partMapper.countConfirmed(uploadSessionId);
    }

    /** 查询会话下的全部分片元数据。 */
    public List<FileUploadPart> findBySessionId(UUID uploadSessionId) {
        return partMapper.findBySessionId(uploadSessionId);
    }

    private FileUploadPart requirePart(UUID uploadSessionId, int partNumber) {
        if (partNumber < 1) {
            throw invalid("分片编号必须从 1 开始");
        }
        FileUploadPart part = partMapper.selectForUpdate(uploadSessionId, partNumber);
        if (part == null) {
            throw new FileUploadException(FileErrorCode.FILE_PART_INVALID, "分片不属于当前上传会话");
        }
        return part;
    }

    private StorageMultipart toMultipart(FileUploadSession session) {
        return new StorageMultipart(session.getStorageContainer(), session.getStagingKey(), session.getProviderUploadId());
    }

    private static int nextAttempt(FileUploadPart part) {
        return (part.getUploadAttempt() == null ? 0 : part.getUploadAttempt()) + 1;
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
