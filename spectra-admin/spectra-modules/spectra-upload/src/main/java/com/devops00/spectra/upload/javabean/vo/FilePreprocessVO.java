package com.devops00.spectra.upload.javabean.vo;


import org.jspecify.annotations.NullMarked;

/// 文件预处理响应vo
///
/// @param hasExist   文件是否存在
/// @param hasChunked 是否需要分片上传
/// @param size       分页大小
/// @param count      分页数量
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/7 23:04
@NullMarked
public record FilePreprocessVO(
        boolean hasExist,
        boolean hasChunked,
        Integer size,
        Integer count
) {

    /// 不需要分片的情况构建
    ///
    /// @return 视图对象
    public static FilePreprocessVO ofFalse() {
        return new FilePreprocessVO(false, false, 0, 0);
    }

    /// 文件已经存在
    ///
    /// @return 视图对象
    public static FilePreprocessVO exist() {
        return new FilePreprocessVO(true, false, 0, 0);
    }

    /// 需要进行分片
    ///
    /// @param size  分片大小
    /// @param count 分片数量
    /// @return {@link FilePreprocessVO} 视图对象
    public static FilePreprocessVO chunk(Integer size, Integer count) {
        return new FilePreprocessVO(false, true, size, count);
    }
}
