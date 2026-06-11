package com.devops00.spectra.upload.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/// S3协议配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/31 01:46
@Data
@ConfigurationProperties(prefix = "spectra.file.upload.s3")
public class S3Properties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String region;

    private String bucket;

}
