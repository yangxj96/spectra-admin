/*
 *  Copyright 2025 yangxj96.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.yangxj96.spectra.common.fileupload;

import com.yangxj96.spectra.common.fileupload.strategy.FileTypeValidationStrategy;
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
public class FileTypeValidator {

    private final List<FileTypeValidationStrategy> strategies;

    public FileTypeValidator(List<FileTypeValidationStrategy> strategies) {
        this.strategies = strategies;
    }

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
