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

import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.properties.S3Properties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * 使用真实 AWS S3 SDK 连接本地 S3 协议夹具，验证 Range 字节和存储故障映射。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
class S3FileStorageProviderHttpTest {

    @Test
    void rangeBytesAndStorageFailuresAreMapped() throws Exception {
        var scenario = new AtomicReference<>("RANGE");
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            var current = scenario.get();
            if ("FAILURE".equals(current)) {
                respond(exchange, 500, "InternalError", null);
                return;
            }
            if ("NOT_FOUND".equals(current)) {
                respond(exchange, 404, "NoSuchKey", null);
                return;
            }
            var body = "2345".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
            exchange.getResponseHeaders().add("Content-Range", "bytes 2-5/10");
            exchange.getResponseHeaders().add("ETag", "\"etag-range\"");
            exchange.sendResponseHeaders(206, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try (var client = client(server)) {
            var properties = new S3Properties();
            properties.setBucket("test-bucket");
            var provider = new S3FileStorageProvider(client, mock(S3Presigner.class), properties);

            try (var object = provider.open("test-bucket", "key", 2L, 5L)) {
                assertEquals(4L, object.metadata().size());
                assertArrayEquals("2345".getBytes(StandardCharsets.UTF_8), object.stream().readAllBytes());
            }

            scenario.set("NOT_FOUND");
            var missing = assertThrows(FileUploadException.class,
                    () -> provider.open("test-bucket", "missing", null, null));
            assertEquals(FileErrorCode.FILE_ASSET_NOT_READY, missing.getErrorCode());

            scenario.set("FAILURE");
            var unavailable = assertThrows(FileUploadException.class,
                    () -> provider.open("test-bucket", "failed", null, null));
            assertEquals(FileErrorCode.FILE_STORAGE_UNAVAILABLE, unavailable.getErrorCode());
        } finally {
            server.stop(0);
        }
    }

    private static S3Client client(HttpServer server) {
        return S3Client.builder()
                .endpointOverride(URI.create("http://127.0.0.1:" + server.getAddress().getPort()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access", "test-secret")))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String code,
                                String message)
            throws java.io.IOException {
        var body = ("<Error><Code>" + code + "</Code><Message>" + (message == null ? code : message)
                + "</Message><RequestId>test</RequestId></Error>").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/xml");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
