package io.github.yangxj96.spectra.core.javabean.common.from;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件保存参数
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/7 23:29
 */
public record FileUploadFrom(
        MultipartFile file,
        String hash
) {
}
