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

package com.yangxj96.spectra.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;

/**
 * <p>
 * 文件相关配置
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/19
 */
@Data
@Component
@ConfigurationProperties(prefix = "spectra.file")
public class FileProperties {

    /**
     * 基础文件位置,所有文件都会在这个目录下面进行存放
     */
    private String baseDir = "spectra-files";

    /**
     * 上传的文件夹位置
     */
    private String uploadDir = "uploads";

    /**
     * 获取当前工作目录
     *
     * @return 当前工作目录
     */
    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     * 获取基础目录的绝对路径（基于当前工作目录）
     *
     * @return 获取基础目录的绝对目录
     */
    public String getAbsoluteBaseDir() {
        // 如果 baseDir 是绝对路径，直接返回
        if (StringUtils.hasText(baseDir) && Paths.get(baseDir).isAbsolute()) {
            return baseDir;
        }

        // 否则以当前工作目录为基准构建路径
        return Paths.get(getWorkingDirectory(), baseDir).normalize().toString();
    }

    /**
     * 获取上传目录的绝对路径
     *
     * @return 文件上传目录
     */
    public String getUploadDir() {
        return Paths.get(getAbsoluteBaseDir(), uploadDir).normalize().toString();
    }
}
