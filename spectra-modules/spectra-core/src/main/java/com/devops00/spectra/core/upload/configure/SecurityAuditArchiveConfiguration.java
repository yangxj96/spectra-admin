/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.upload.configure;

import com.devops00.spectra.common.port.audit.SecurityAuditArchiveBackend;
import com.devops00.spectra.common.port.audit.SecurityAuditArchiveIntegrity;
import com.devops00.spectra.common.port.audit.SecurityAuditArchiveReceipt;
import com.devops00.spectra.core.upload.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectLockMode;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

/** S3-compatible Object Lock 安全审计归档后端。 */
@Configuration
@ConditionalOnProperty(prefix = "spectra.security", name = "audit-archive-backend", havingValue = "S3_OBJECT_LOCK")
@RequiredArgsConstructor
public class SecurityAuditArchiveConfiguration {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    /**
     * 处理内部业务逻辑（{@code securityAuditArchiveBackend}）。
     */
    @Bean
    public SecurityAuditArchiveBackend securityAuditArchiveBackend() {
        if (s3Properties.getArchiveBucket() == null || s3Properties.getArchiveBucket().isBlank()) {
            throw new IllegalStateException("S3_OBJECT_LOCK 归档后端必须配置独立 archive-bucket");
        }
        return new S3ObjectLockSecurityAuditArchiveBackend(s3Client, s3Properties);
    }

    private static final class S3ObjectLockSecurityAuditArchiveBackend implements SecurityAuditArchiveBackend {

        private final S3Client client;
        private final S3Properties properties;

        private S3ObjectLockSecurityAuditArchiveBackend(S3Client client, S3Properties properties) {
            this.client = client;
            this.properties = properties;
        }

        @Override
        public String id() {
            return "S3_OBJECT_LOCK";
        }

        @Override
        public SecurityAuditArchiveReceipt put(String objectKey, byte[] content, Instant retainUntil) {
            if (objectKey == null
                    || objectKey.isBlank()
                    || content == null
                    || retainUntil == null
                    || !retainUntil.isAfter(Instant.now())) {
                throw new IllegalArgumentException("归档对象 key、内容和未来保留截止时间不能为空");
            }
            String key = prefix() + objectKey;
            String digest = SecurityAuditArchiveIntegrity.sha256(content);
            try {
                client.putObject(PutObjectRequest.builder()
                        .bucket(properties.getArchiveBucket())
                        .key(key)
                        .ifNoneMatch("*")
                        .contentType("application/octet-stream")
                        .metadata(Map.of("sha256", digest, "retention-until", retainUntil.toString()))
                        .objectLockMode(ObjectLockMode.COMPLIANCE)
                        .objectLockRetainUntilDate(retainUntil)
                        .build(), RequestBody.fromBytes(content));
            } catch (S3Exception exception) {
                if (exception.statusCode() != 412) {
                    throw exception;
                }
                // 数据库确认失败后的重试可能命中已经写成功的 Object Lock 对象；
                // 只有完全相同的不可变内容才视为幂等成功，冲突内容必须失败。
                SecurityAuditArchiveIntegrity.verify(read(uri(key)), digest, content.length);
            }
            return new SecurityAuditArchiveReceipt(uri(key), digest, content.length, retainUntil);
        }

        @Override
        public byte[] read(String objectUri) {
            URI uri = requireArchiveUri(objectUri);
            return client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(uri.getHost())
                    .key(uri.getPath().substring(1))
                    .build()).asByteArray();
        }

        @Override
        public boolean exists(String objectUri) {
            URI uri = requireArchiveUri(objectUri);
            try {
                client.headObject(HeadObjectRequest.builder()
                        .bucket(uri.getHost())
                        .key(uri.getPath().substring(1))
                        .build());
                return true;
            } catch (RuntimeException exception) {
                return false;
            }
        }

        @Override
        public void verify(String objectUri, String expectedSha256, long expectedLength) {
            URI uri = requireArchiveUri(objectUri);
            var metadata = client.headObject(HeadObjectRequest.builder()
                    .bucket(uri.getHost())
                    .key(uri.getPath().substring(1))
                    .build());
            if (metadata.contentLength() != expectedLength) {
                throw new IllegalStateException("S3 归档对象长度校验失败");
            }
            SecurityAuditArchiveIntegrity.verify(read(objectUri), expectedSha256, expectedLength);
        }

        /** 校验并解析受配置 bucket 约束的对象 URI。 */
        private URI requireArchiveUri(String objectUri) {
            URI uri = URI.create(objectUri);
            if (!"s3".equalsIgnoreCase(uri.getScheme())
                    || !properties.getArchiveBucket().equals(uri.getHost())
                    || uri.getPath() == null
                    || uri.getPath().length() <= 1) {
                throw new IllegalArgumentException("归档对象 URI 不属于配置的 S3 archive bucket");
            }
            return uri;
        }

        /**
         * 处理内部业务逻辑（{@code prefix}）。
         */
        private String prefix() {
            String prefix = properties.getArchivePrefix();
            if (prefix == null || prefix.isBlank()) {
                return "security-audit/";
            }
            return prefix.endsWith("/") ? prefix : prefix + "/";
        }

        /**
         * 处理内部业务逻辑（{@code uri}）。
         */
        private String uri(String key) {
            return "s3://" + properties.getArchiveBucket() + "/" + key;
        }
    }
}
