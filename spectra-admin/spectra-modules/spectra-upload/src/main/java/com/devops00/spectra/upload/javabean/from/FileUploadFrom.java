package com.devops00.spectra.upload.javabean.from;

import org.springframework.web.multipart.MultipartFile;

/// 文件保存参数
///
/// @param file 文件
/// @param hash 文件hash值
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/7 23:29
public record FileUploadFrom(
        MultipartFile file,
        String hash
) {
}
