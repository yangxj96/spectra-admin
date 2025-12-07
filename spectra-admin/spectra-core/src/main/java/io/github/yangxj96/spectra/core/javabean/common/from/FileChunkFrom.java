package io.github.yangxj96.spectra.core.javabean.common.from;


import org.springframework.web.multipart.MultipartFile;

/**
 * 文件分片上传参数
 *
 * @param file  分片文件
 * @param md5   整体文件的MD5
 * @param index 分片索引
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/7 23:18
 */
public record FileChunkFrom(
        MultipartFile file,
        String md5,
        Integer index
) {
}
