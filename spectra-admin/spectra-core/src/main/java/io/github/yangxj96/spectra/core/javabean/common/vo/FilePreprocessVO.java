package io.github.yangxj96.spectra.core.javabean.common.vo;


import org.jspecify.annotations.NullMarked;

/**
 * 文件预处理响应vo
 *
 * @param hasChunked  是否需要分片上传
 * @param chunkedSize 分页大小
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/7 23:04
 */
@NullMarked
public record FilePreprocessVO(
        boolean hasChunked,
        Integer chunkedSize
) {

    /**
     * 不需要分片的情况构建
     *
     * @return {@link FilePreprocessVO} 视图对象
     */
    public static FilePreprocessVO ofFalse() {
        return new FilePreprocessVO(false, 0);
    }

}
