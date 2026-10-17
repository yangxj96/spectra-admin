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

package com.devops00.spectra.core.upload.storage.s3;

import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.configure.S3Configuration;
import com.devops00.spectra.core.upload.properties.S3Properties;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 使用真实 S3 兼容存储验证预签名分片上传与存储侧确认校验。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class S3FileStorageProviderRealTest {

    @Test
    void presignedPartIsVerifiedAgainstStorage() throws Exception {
        S3Properties properties = new S3Properties();
        properties.setEndpoint(requireEnv("S3_ENDPOINT"));
        properties.setAccessKey(requireEnv("S3_ACCESS_KEY"));
        properties.setSecretKey(requireEnv("S3_SECRET_KEY"));
        properties.setRegion(requireEnv("S3_REGION"));
        properties.setBucket(requireEnv("S3_BUCKET"));

        var configuration = new S3Configuration(properties);
        try (var client = configuration.s3Client(); var presigner = configuration.s3Presigner()) {
            var provider = new S3FileStorageProvider(client, presigner, properties);
            byte[] payload = "s3-part-verification".getBytes(StandardCharsets.UTF_8);
            String key = "tests/" + UUID.randomUUID() + ".bin";
            var multipart = provider.createMultipart(UUID.randomUUID(), properties.getBucket(), key, 1);
            try {
                String sha256 = sha256(payload);
                var target = provider.createPartTarget(new FileStorageProvider.PartTargetInput(
                        multipart, 1, payload.length, sha256, Instant.now().plusSeconds(300), 1));
                try (var http = HttpClient.newHttpClient()) {
                    var put = http.send(HttpRequest.newBuilder(URI.create(target.url()))
                            .PUT(HttpRequest.BodyPublishers.ofByteArray(payload))
                            .build(), HttpResponse.BodyHandlers.discarding());
                    assertEquals(200, put.statusCode(), "预签名分片上传应成功");
                    String etag = put.headers().firstValue("ETag").orElseThrow();

                    var stored = provider.confirmExternalPart(multipart, 1, payload.length, sha256, etag);
                    assertEquals(payload.length, stored.size());
                    assertEquals(etag.replace("\"", ""), stored.etag().replace("\"", ""));

                    assertThrows(FileUploadException.class,
                            () -> provider.confirmExternalPart(multipart, 1, payload.length, sha256, "\"wrong\""));
                }
            } finally {
                provider.abortMultipart(multipart);
            }
        }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("真实依赖测试缺少环境变量 " + name);
        }
        return value;
    }

    private static String sha256(byte[] payload) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload));
    }
}
