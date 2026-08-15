/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.upload.configure;

import com.devops00.spectra.core.security.audit.SecurityAuditArchiveBackend;
import com.devops00.spectra.core.security.audit.SecurityAuditArchiveIntegrity;
import com.devops00.spectra.core.security.audit.SecurityAuditArchiveReceipt;
import com.devops00.spectra.upload.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectLockMode;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.time.Instant;

/** S3-compatible Object Lock 安全审计归档后端。 */
@Configuration
@ConditionalOnProperty(prefix = "spectra.security", name = "audit-archive-backend", havingValue = "S3_OBJECT_LOCK")
@RequiredArgsConstructor
public class SecurityAuditArchiveConfiguration {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

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
            client.putObject(PutObjectRequest.builder()
                    .bucket(properties.getArchiveBucket())
                    .key(key)
                    .contentType("application/octet-stream")
                    .metadata(java.util.Map.of("sha256", digest, "retention-until", retainUntil.toString()))
                    .objectLockMode(ObjectLockMode.COMPLIANCE)
                    .objectLockRetainUntilDate(retainUntil)
                    .build(), RequestBody.fromBytes(content));
            return new SecurityAuditArchiveReceipt(uri(key), digest, content.length, retainUntil);
        }

        @Override
        public byte[] read(String objectUri) {
            URI uri = URI.create(objectUri);
            if (!"s3".equalsIgnoreCase(uri.getScheme())
                    || !properties.getArchiveBucket().equals(uri.getHost())
                    || uri.getPath() == null
                    || uri.getPath().length() <= 1) {
                throw new IllegalArgumentException("归档对象 URI 不属于配置的 S3 archive bucket");
            }
            return client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(uri.getHost())
                    .key(uri.getPath().substring(1))
                    .build()).asByteArray();
        }

        private String prefix() {
            String prefix = properties.getArchivePrefix();
            if (prefix == null || prefix.isBlank()) {
                return "security-audit/";
            }
            return prefix.endsWith("/") ? prefix : prefix + "/";
        }

        private String uri(String key) {
            return "s3://" + properties.getArchiveBucket() + "/" + key;
        }
    }
}
