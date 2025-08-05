package com.yangxj96.spectra.common.verify;

import com.yangxj96.spectra.common.strategy.FileTypeValidationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 文件类型校验策略管理器
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/19
 */
@Slf4j
public record FileTypeValidator(List<FileTypeValidationStrategy> strategies) {

    /**
     * 执行所有注册的验证策略
     *
     * @param file 待验证的文件
     * @return 如果所有策略均通过，则返回 true；否则返回 false
     */
    public boolean validate(MultipartFile file) {
        for (FileTypeValidationStrategy strategy : strategies) {
            try {
                if (!strategy.isValid(file)) {
                    return false;
                }
            } catch (IOException e) {
                // 可以根据需要记录日志或抛出异常
                log.atError().log("验证策略失败:" + e.getMessage(), e);
                return false;
            }
        }
        return true;
    }
}
