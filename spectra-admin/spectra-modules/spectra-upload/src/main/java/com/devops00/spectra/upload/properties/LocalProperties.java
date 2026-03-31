package com.devops00.spectra.upload.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/// 本地上传的配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/31 13:57
@Data
@ConfigurationProperties(prefix = "spectra.file.upload.local")
public class LocalProperties {

    /// 上传的文件夹位置
    private String uploadDir = "uploads";

    /// 上传文件的时候临时文件路径
    private String uploadTempDir = "temp";

}
