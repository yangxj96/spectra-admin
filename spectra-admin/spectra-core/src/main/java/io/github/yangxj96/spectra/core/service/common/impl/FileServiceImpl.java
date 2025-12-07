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

import io.github.yangxj96.spectra.common.exception.FileTypeException;
import io.github.yangxj96.spectra.common.utils.ChunkUtils;
import io.github.yangxj96.spectra.core.configure.fileupload.properties.FileUploadProperties;
import io.github.yangxj96.spectra.core.configure.fileupload.strategy.FileTypeValidator;
import io.github.yangxj96.spectra.core.service.common.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private FileUploadProperties properties;

    @Resource
    private FileTypeValidator validator;

    private final Path root;

    public FileServiceImpl() throws IOException {
        this.root = Paths.get(properties.getUploadDir());
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
    }

    @Override
    public void upload(MultipartFile file) throws IOException {
        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }
        // 2. 使用策略模式进行文件类型验证
        if (!validator.validate(file)) {
            throw new FileTypeException("此类文件不允许上传");
        }
        // 创建目录（如果不存在）
        var uploadDirPath = Paths.get(properties.getUploadDir());
        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath);
        }
        // 构建目标文件路径
        var targetLocation = uploadDirPath.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(targetLocation); // Spring 提供的方法直接保存
        log.debug("文件已保存至: {}", targetLocation);
    }

    // 直接保存小文件
    public String saveDirect(MultipartFile file, String targetName) throws IOException {
        Path dest = root.resolve(targetName);
        Files.copy(file.getInputStream(), dest);
        return dest.toString();
    }


    // 保存单个 chunk
    public void saveChunk(MultipartFile chunk, String fileMd5, int chunkIndex) throws IOException {
        Path dir = ChunkUtils.getFileRoot(root, fileMd5);
        ChunkUtils.ensureDir(dir);
        Path chunkFile = ChunkUtils.getChunkFile(root, fileMd5, chunkIndex);
        Files.copy(chunk.getInputStream(), chunkFile);
    }


    // 查询已上传 chunk 索引
    public List<Integer> listUploadedChunks(String fileMd5) throws IOException {
        Path dir = ChunkUtils.getFileRoot(root, fileMd5);
        if (!Files.exists(dir)) {
            return Collections.emptyList();
        }

        try (Stream<Path> list = Files.list(dir)) {
            return list.map(p -> p.getFileName().toString())
                    .filter(n -> n.matches("\\d+"))
                    .map(Integer::valueOf)
                    .sorted()
                    .collect(Collectors.toList());
        }
    }


    // 合并
    public Path merge(String fileMd5, String filename, int totalChunks) throws IOException {
        Path dir = ChunkUtils.getFileRoot(root, fileMd5);
        Path dest = root.resolve(filename);
        ChunkUtils.mergeChunks(dest, dir, totalChunks);
        // 可选：上传到对象存储，或做哈希校验
        // 合并成功后可删除临时目录
        // Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        return dest;
    }
}
