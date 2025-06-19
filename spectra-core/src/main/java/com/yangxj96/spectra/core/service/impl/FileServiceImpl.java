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

package com.yangxj96.spectra.core.service.impl;

import com.yangxj96.spectra.common.fileupload.FileTypeValidator;
import com.yangxj96.spectra.common.properties.FileProperties;
import com.yangxj96.spectra.core.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * 文件业务层实现
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/19
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Resource
    private FileProperties properties;

    @Resource
    private FileTypeValidator validator;

    @Override
    public void upload(MultipartFile file) throws IOException {
        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }
        // 2. 使用策略模式进行文件类型验证
        if (!validator.validate(file)) {
            throw new RuntimeException("此类文件不允许上传");
        }
        // 获取上传位置前缀
        String prefix = properties.getBaseDir() + properties.getUploadDir();

        // 3. 打印文件基本信息（实际应用中可替换为保存逻辑）
        log.info("文件名: " + file.getOriginalFilename());
        log.info("文件大小: " + file.getSize() + " bytes");
        log.info("MIME 类型: " + file.getContentType());

        // 4. 实际保存文件（示例：保存到临时目录）
        String targetPath = prefix + File.separator + file.getOriginalFilename();
        File dest = new File(targetPath);
        // 防止重名覆盖，可加 UUID 前缀
        // FileUtils.writeBytesToFile(file.getBytes(), dest); // 可用 Apache Commons IO
        file.transferTo(dest); // Spring 提供的方法直接保存

        log.info("文件已保存至: " + targetPath);
    }
}
