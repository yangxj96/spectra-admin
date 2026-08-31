/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.storage.s3;

import com.devops00.spectra.upload.api.FileErrorCode;
import com.devops00.spectra.upload.api.FileUploadException;
import com.devops00.spectra.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.upload.properties.S3Properties;
import com.devops00.spectra.upload.storage.FileStorageProvider;
import com.devops00.spectra.upload.storage.PartTarget;
import com.devops00.spectra.upload.storage.StorageHealth;
import com.devops00.spectra.upload.storage.StorageMultipart;
import com.devops00.spectra.upload.storage.StorageObject;
import com.devops00.spectra.upload.storage.StorageObjectMetadata;
import com.devops00.spectra.upload.storage.StoredPart;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnBean({S3Client.class, S3Presigner.class})
public class S3FileStorageProvider implements FileStorageProvider {

    private final S3Client client;
    private final S3Presigner presigner;
    private final S3Properties properties;

    public S3FileStorageProvider(S3Client client, S3Presigner presigner, S3Properties properties) {
        this.client = client;
        this.presigner = presigner;
        this.properties = properties;
    }

    @Override
    public StorageProviderType type() {
        return StorageProviderType.S3;
    }

    @Override
    public StorageMultipart createMultipart(UUID uploadId, String container, String key, int totalParts) {
        try {
            var response = client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                    .bucket(container)
                    .key(key)
                    .contentType("application/octet-stream")
                    .build());
            return new StorageMultipart(container, key, response.uploadId());
        } catch (RuntimeException e) {
            throw unavailable("unable to create S3 multipart upload", e);
        }
    }

    @Override
    public PartTarget createPartTarget(StorageMultipart multipart, int partNumber, long partSize, String partSha256,
                                       Instant expiresAt, int attempt) {
        try {
            var uploadRequest = UploadPartRequest.builder()
                    .bucket(multipart.container())
                    .key(multipart.key())
                    .uploadId(multipart.providerUploadId())
                    .partNumber(partNumber)
                    .contentLength(partSize)
                    .build();
            Duration duration = Duration.between(Instant.now(), expiresAt);
            var request = presigner.presignUploadPart(UploadPartPresignRequest.builder()
                    .signatureDuration(duration.isNegative() || duration.isZero() ? Duration.ofSeconds(1) : duration)
                    .uploadPartRequest(uploadRequest)
                    .build());
            return new PartTarget("PUT", request.url().toString(), Map.of("Content-Length", Long.toString(partSize)), expiresAt, attempt);
        } catch (RuntimeException e) {
            throw unavailable("unable to create S3 presigned target", e);
        }
    }

    @Override
    public StoredPart putLocalPart(StorageMultipart multipart, int partNumber, InputStream content, long expectedSize,
                                   String expectedSha256) {
        throw new FileUploadException(FileErrorCode.FILE_UPLOAD_CONFLICT, "S3 provider requires direct presigned upload");
    }

    @Override
    public StoredPart confirmExternalPart(StorageMultipart multipart, int partNumber, long expectedSize,
                                          String expectedSha256, String providerEtag) {
        if (providerEtag == null || providerEtag.isBlank()) {
            throw new FileUploadException(FileErrorCode.FILE_PART_INVALID, "S3 ETag is required");
        }
        return new StoredPart(partNumber, expectedSize, expectedSha256, providerEtag);
    }

    @Override
    public void completeMultipart(StorageMultipart multipart, List<StoredPart> parts) {
        try {
            var completedParts = parts.stream()
                    .sorted(java.util.Comparator.comparingInt(StoredPart::partNumber))
                    .map(part -> CompletedPart.builder().partNumber(part.partNumber()).eTag(part.etag()).build())
                    .toList();
            client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                    .bucket(multipart.container())
                    .key(multipart.key())
                    .uploadId(multipart.providerUploadId())
                    .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
                    .build());
        } catch (RuntimeException e) {
            throw unavailable("unable to complete S3 multipart upload", e);
        }
    }

    @Override
    public void abortMultipart(StorageMultipart multipart) {
        try {
            client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(multipart.container())
                    .key(multipart.key())
                    .uploadId(multipart.providerUploadId())
                    .build());
        } catch (RuntimeException e) {
            throw unavailable("unable to abort S3 multipart upload", e);
        }
    }

    @Override
    public StorageObject open(String container, String key, Long rangeStart, Long rangeEnd) {
        try {
            var builder = GetObjectRequest.builder().bucket(container).key(key);
            if (rangeStart != null)
                builder.range("bytes=" + rangeStart + "-" + (rangeEnd == null ? "" : rangeEnd));
            ResponseInputStream<software.amazon.awssdk.services.s3.model.GetObjectResponse> stream = client.getObject(builder.build());
            var response = stream.response();
            return new StorageObject(stream, new StorageObjectMetadata(response.contentLength(), response.contentType(),
                    response.metadata().get("sha256"), response.eTag()));
        } catch (RuntimeException e) {
            throw new FileUploadException(FileErrorCode.FILE_ASSET_NOT_READY, "S3 object is unavailable", e);
        }
    }

    @Override
    public void delete(String container, String key) {
        try {
            client.deleteObject(DeleteObjectRequest.builder().bucket(container).key(key).build());
        } catch (RuntimeException e) {
            throw unavailable("unable to delete S3 object", e);
        }
    }

    @Override
    public boolean exists(String container, String key) {
        try {
            client.headObject(HeadObjectRequest.builder().bucket(container).key(key).build());
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public StorageHealth health() {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(properties.getBucket()).build());
            return StorageHealth.available("S3_BUCKET_REACHABLE");
        } catch (RuntimeException e) {
            return StorageHealth.unavailable("S3_BUCKET_UNAVAILABLE");
        }
    }

    private static FileUploadException unavailable(String message, RuntimeException cause) {
        return new FileUploadException(FileErrorCode.FILE_STORAGE_UNAVAILABLE, message, cause);
    }
}
