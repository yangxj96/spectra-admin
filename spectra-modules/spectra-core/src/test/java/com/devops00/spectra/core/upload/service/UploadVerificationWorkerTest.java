/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.service;

import com.devops00.spectra.core.upload.configure.FileStorageProviderRegistry;
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.javabean.constant.UploadPartStatus;
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
import com.devops00.spectra.core.upload.storage.StorageMultipart;
import com.devops00.spectra.core.upload.storage.StorageObject;
import com.devops00.spectra.core.upload.storage.StorageObjectMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 上传校验 Worker 的对象确认、哈希校验和失败清理测试。 */
@ExtendWith(MockitoExtension.class)
class UploadVerificationWorkerTest {

    private static final byte[] CONTENT = "hello".getBytes(StandardCharsets.UTF_8);
    private static final String CONTENT_SHA256 = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";

    @Mock
    private FileTypeMapper fileTypeMapper;

    @Mock
    private FileAssetMapper fileAssetMapper;

    @Mock
    private FileUploadSessionMapper sessionMapper;

    @Mock
    private FileUploadPartMapper partMapper;

    @Mock
    private FileStorageProviderRegistry providerRegistry;

    @Mock
    private FileStorageProvider provider;

    @Test
    void verifiesObjectAndCreatesAssetWithoutOverridingEntityId() {
        UUID uploadId = UUID.randomUUID();
        UUID fileTypeId = UUID.randomUUID();
        FileUploadSession session = session(uploadId, CONTENT_SHA256);
        FileUploadPart part = part(1, CONTENT_SHA256);
        FileType fileType = new FileType();
        fileType.setId(fileTypeId);
        StorageMultipart multipart = new StorageMultipart("local", "assets/object/content.bin", "provider-upload-id");
        when(sessionMapper.selectForUpdate(uploadId)).thenReturn(session);
        when(partMapper.findBySessionId(uploadId)).thenReturn(List.of(part));
        when(providerRegistry.require(StorageProviderType.LOCAL)).thenReturn(provider);
        when(provider.open("local", "assets/object/content.bin", null, null))
                .thenReturn(new StorageObject(new ByteArrayInputStream(CONTENT),
                        new StorageObjectMetadata(CONTENT.length, "text/plain", CONTENT_SHA256, "etag")));
        when(fileAssetMapper.findReady(CONTENT_SHA256, CONTENT.length)).thenReturn(null);
        when(fileTypeMapper.findEnabledByContentType("text/plain")).thenReturn(fileType);
        when(fileAssetMapper.insert(any(FileAsset.class))).thenAnswer(invocation -> {
            FileAsset asset = invocation.getArgument(0);
            asset.setId(UUID.randomUUID());
            return 1;
        });

        var worker = new UploadVerificationWorker(new FileUploadProperties(), fileTypeMapper, fileAssetMapper,
                sessionMapper, partMapper, providerRegistry);

        worker.verify(uploadId);

        verify(provider).completeMultipart(eq(multipart), any());
        verify(fileAssetMapper).insert(any(FileAsset.class));
        verify(sessionMapper).markReady(eq(uploadId), any(UUID.class), any(Instant.class));
    }

    @Test
    void marksSessionFailedAndDeletesObjectWhenHashDiffers() {
        UUID uploadId = UUID.randomUUID();
        FileUploadSession session = session(uploadId, "0000000000000000000000000000000000000000000000000000000000000000");
        when(sessionMapper.selectForUpdate(uploadId)).thenReturn(session);
        when(partMapper.findBySessionId(uploadId)).thenReturn(List.of(part(1, CONTENT_SHA256)));
        when(providerRegistry.require(StorageProviderType.LOCAL)).thenReturn(provider);
        when(provider.open("local", "assets/object/content.bin", null, null))
                .thenReturn(new StorageObject(new ByteArrayInputStream(CONTENT),
                        new StorageObjectMetadata(CONTENT.length, "text/plain", CONTENT_SHA256, "etag")));

        var worker = new UploadVerificationWorker(new FileUploadProperties(), fileTypeMapper, fileAssetMapper,
                sessionMapper, partMapper, providerRegistry);

        worker.verify(uploadId);

        verify(provider).delete("local", "assets/object/content.bin");
        verify(sessionMapper).markFailed(eq(uploadId), eq("FILE_UPLOAD_HASH_MISMATCH"), any(Instant.class));
    }

    private static FileUploadSession session(UUID uploadId, String sha256) {
        var session = new FileUploadSession();
        session.setId(uploadId);
        session.setStatus(UploadSessionStatus.VERIFYING);
        session.setSize((long) CONTENT.length);
        session.setContentSha256(sha256);
        session.setDeclaredContentType("text/plain");
        session.setOriginalName("hello.txt");
        session.setStorageProvider(StorageProviderType.LOCAL);
        session.setStorageContainer("local");
        session.setStagingKey("assets/object/content.bin");
        session.setProviderUploadId("provider-upload-id");
        return session;
    }

    private static FileUploadPart part(int partNumber, String sha256) {
        var part = new FileUploadPart();
        part.setPartNumber(partNumber);
        part.setExpectedSize((long) CONTENT.length);
        part.setActualSha256(sha256);
        part.setUploadedSize((long) CONTENT.length);
        part.setStatus(UploadPartStatus.CONFIRMED);
        return part;
    }
}
