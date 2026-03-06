package com.devops00.spectra.upload.javabean.from;


/// 文件上传预处理
///
/// @param filename 文件名称
/// @param size     文件大小
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/7 22:59
public record FilePreprocessFrom(
        String filename,
        Long size,
        String hash
) {
}
