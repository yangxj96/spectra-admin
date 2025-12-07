/*
 *  Copyright 2018-2025 yangxj96
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
 */

package io.github.yangxj96.spectra.core.service.common.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.github.yangxj96.spectra.common.exception.FileUploadException;
import io.github.yangxj96.spectra.core.javabean.common.from.FileChunkFrom;
import io.github.yangxj96.spectra.core.javabean.common.from.FilePreprocessFrom;
import io.github.yangxj96.spectra.core.javabean.common.vo.FilePreprocessVO;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
@NullMarked
public class FileServiceLocalImpl extends AbstractFileService {

    /**
     * 本地文件管理的根文件路径
     */
    private final Path root;

    /**
     * 本地文件管理的临时文件路径
     */
    private final Path temp;

    public FileServiceLocalImpl() throws IOException {
        this.root = Paths.get(super.properties.getUploadDir());
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
        this.temp = Paths.get(super.properties.getUploadTempDir());
        if (!Files.exists(temp)) {
            Files.createDirectories(temp);
        }
    }

    @Override
    public FilePreprocessVO preprocess(FilePreprocessFrom from) {
        if (from.size() <= properties.getChunkSize()) {
            FilePreprocessVO.ofFalse();
        }
        return FilePreprocessVO.ofFalse();
    }

    @Override
    public void upload(FileChunkFrom from) {
        // TODO 先检查是否有预处理文件的信息
        // 先检查文件是否符合上传要求
        super.verify(from.file());
        // 保存文件
        try (var is = from.file().getInputStream()) {
            // 构建文件保存目录
            Path fileDir = root.resolve(from.md5()).normalize();
            Files.createDirectories(fileDir);
            // TODO 构建文件后保存文件
            var filename = fileDir.resolve(IdWorker.get32UUID()).normalize();
            Files.copy(is, filename, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileUploadException(e);
        }
    }

    @Override
    public void chunk(FileChunkFrom from) {
        // TODO 获取文件信息
        try (var is = from.file().getInputStream()) {
            // 构建临时文件上传目录
            Path fileDir = temp.resolve(from.md5()).normalize();
            Files.createDirectories(fileDir);
            // 保存文件
            var filename = fileDir.resolve("chunk_" + from.index()).normalize();
            Files.copy(is, filename, StandardCopyOption.REPLACE_EXISTING);
            // TODO 检测是否分片完成,如果完成后都在了的话需要进行合并
        } catch (IOException e) {
            throw new FileUploadException(e);
        }
    }

    @Override
    public void merge(String md5) {
        // TODO 获取临时文件信息
    }
}
