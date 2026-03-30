package com.devops00.spectra.upload.service;

import java.util.List;

/// S3协议-服务
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/31 01:28
public interface S3Service {

    List<String> listAllObjects(String bucket);

    /// 生成上传预签名 URL
    ///
    /// @param bucket 桶
    /// @param key    key
    String createUploadUrl(String bucket, String key);

}
