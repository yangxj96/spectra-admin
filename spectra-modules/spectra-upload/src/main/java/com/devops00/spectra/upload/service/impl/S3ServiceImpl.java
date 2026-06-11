package com.devops00.spectra.upload.service.impl;


import com.devops00.spectra.upload.properties.S3Properties;
import com.devops00.spectra.upload.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/// S3协议-服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/31 01:28
@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    private final S3Presigner presigner;

    @Override
    public List<String> listAllObjects(String bucket) {
        List<String> keys = new ArrayList<>();
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();
        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);

            response.contents().forEach(s3Object -> {
                keys.add(s3Object.key());
            });
            request = request.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();

        } while (response.isTruncated());
        return keys;
    }

    @Override
    public String createUploadUrl(String bucket, String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(r -> r
                        .signatureDuration(Duration.ofMinutes(10)) // 10分钟有效
                        .putObjectRequest(putObjectRequest)
                );

        return presignedRequest.url().toString();
    }
}
