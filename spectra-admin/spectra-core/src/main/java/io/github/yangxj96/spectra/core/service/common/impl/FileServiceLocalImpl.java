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
import io.github.yangxj96.spectra.common.utils.CollUtils;
import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.configure.fileupload.properties.FileUploadProperties;
import io.github.yangxj96.spectra.core.configure.fileupload.strategy.FileTypeValidator;
import io.github.yangxj96.spectra.core.javabean.common.entity.FileChunk;
import io.github.yangxj96.spectra.core.javabean.common.entity.FileInfo;
import io.github.yangxj96.spectra.core.javabean.common.from.FileChunkFrom;
import io.github.yangxj96.spectra.core.javabean.common.from.FilePreprocessFrom;
import io.github.yangxj96.spectra.core.javabean.common.from.FileUploadFrom;
import io.github.yangxj96.spectra.core.javabean.common.vo.FilePreprocessVO;
import io.github.yangxj96.spectra.core.mapper.common.FileChunkMapper;
import io.github.yangxj96.spectra.core.mapper.common.FileInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

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

    @Resource
    private FileInfoMapper fileInfoMapper;

    @Resource
    private FileChunkMapper fileChunkMapper;

    public FileServiceLocalImpl(FileTypeValidator validator, FileUploadProperties properties) throws IOException {
        this.validator = validator;
        this.properties = properties;

        this.root = Paths.get(properties.getUploadDir());
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
        this.temp = Paths.get(properties.getUploadTempDir());
        if (!Files.exists(temp)) {
            Files.createDirectories(temp);
        }
    }

    @Override
    public FilePreprocessVO preprocess(FilePreprocessFrom from) {
        // 先查找之前是否上传过
        FileInfo fileInfo = fileInfoMapper.getByHash(from.hash());
        if (fileInfo != null) {
            return FilePreprocessVO.exist();
        }
        // 小文件不用计算分片了
        if (properties == null || from.size() <= properties.getChunkSize()) {
            return FilePreprocessVO.ofFalse();
        }
        // 计算分片信息后响应
        int count = Math.toIntExact(from.size() / properties.getChunkSize());
        if (from.size() % properties.getChunkSize() != 0) {
            count++;
        }
        return FilePreprocessVO.chunk(Math.toIntExact(properties.getChunkSize()), count);
    }

    @Override
    @Transactional
    public void upload(FileUploadFrom from) {
        // 文件如果存在就直接返回就好了
        FileInfo fileInfo = fileInfoMapper.getByHash(from.hash());
        if (fileInfo != null) {
            return;
        }
        // 先检查文件是否符合上传要求
        super.verify(from.file());
        // 保存文件
        try (var is = from.file().getInputStream()) {
            // 构建文件保存目录
            Path fileDir = root.resolve(from.hash()).normalize();
            Files.createDirectories(fileDir);

            // 构建文件后保存文件
            var filename = IdWorker.get32UUID();
            var originName = from.file().getOriginalFilename();
            if (StrUtils.isBlank(originName)) {
                throw new FileUploadException("文件名不存在");
            }
            var suffix = originName.substring(originName.lastIndexOf("."));
            var path = fileDir.resolve(filename).normalize();

            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            // 保存文件记录
            var datum = FileInfo.builder()
                    .fileName(filename)
                    .originName(originName)
                    .suffix(suffix)
                    .path(path.toAbsolutePath().toString())
                    .size(from.file().getSize())
                    .hash(from.hash())
                    .storageType((short) 0)
                    .build();
            fileInfoMapper.insert(datum);
        } catch (IOException e) {
            throw new FileUploadException(e);
        }
    }

    @Override
    @Transactional
    public void chunk(FileChunkFrom from) {
        var fileChunk = fileChunkMapper.getByFileId(from.hash());
        if (CollUtils.isNotEmpty(fileChunk)) {
            // 检查这一个分块是否存在,存在则跳过
            FileChunk chunk = fileChunk
                    .stream()
                    .filter(i -> i.getChunkIndex().equals(from.index()))
                    .findFirst()
                    .orElse(null);
            // 不等于null,则说明这个分片已经存在了
            if (chunk != null) {
                return;
            }
        }
        try (var is = from.file().getInputStream()) {
            // 如果是第一个分片则检查分片信息
            if (from.index() == 1) {
                this.verify(from.file());
            }
            // 构建临时文件上传目录
            Path fileDir = temp.resolve(from.hash()).normalize();
            Files.createDirectories(fileDir);
            // 保存文件
            var filename = fileDir.resolve("chunk_" + from.index()).normalize();
            Files.copy(is, filename, StandardCopyOption.REPLACE_EXISTING);
            // 存到临时文件信息表
            var datum = FileChunk.builder()
                    .fileId(from.hash())
                    .fileName(from.fileName())
                    .chunkIndex(from.index())
                    .totalChunks(from.count())
                    .chunkPath(filename.toAbsolutePath().toString())
                    .chunkSize(from.file().getSize())
                    .build();
            fileChunkMapper.insert(datum);
            // 检测合并
            this.merge(from.hash());
        } catch (IOException e) {
            throw new FileUploadException(e);
        }
    }

    @Override
    public void merge(String hash) {
        List<FileChunk> chunks = fileChunkMapper.getByFileId(hash);
        if (CollUtils.isEmpty(chunks) || chunks.size() != chunks.getFirst().getTotalChunks()) {
            return;
        }
        // 开始合并
        chunks.forEach(i -> log.debug("合并,文件ID:{},分片:{}", i.getFileId(), i.getChunkIndex()));

    }
}
