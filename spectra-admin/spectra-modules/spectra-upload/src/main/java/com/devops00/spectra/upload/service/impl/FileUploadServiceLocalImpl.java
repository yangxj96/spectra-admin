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

package com.devops00.spectra.upload.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.FileTypeException;
import com.devops00.spectra.common.exception.FileUploadException;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.upload.javabean.entity.FileChunk;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import com.devops00.spectra.upload.javabean.from.FileChunkFrom;
import com.devops00.spectra.upload.javabean.from.FilePreprocessFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadFrom;
import com.devops00.spectra.upload.javabean.vo.FilePreprocessVO;
import com.devops00.spectra.upload.mapper.FileChunkMapper;
import com.devops00.spectra.upload.mapper.FileInfoMapper;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.properties.LocalProperties;
import com.devops00.spectra.upload.service.FileUploadService;
import com.devops00.spectra.upload.strategy.FileTypeValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/// 文件业务层实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/19
@Slf4j
@NullMarked
@RequiredArgsConstructor
@Service("fileUploadServiceLocalImpl")
public class FileUploadServiceLocalImpl implements FileUploadService {

    /// 本地文件管理的根文件路径
    @Nullable
    private Path root;

    /// 本地文件管理的临时文件路径
    @Nullable
    private Path temp;

    private final FileInfoMapper fileInfoMapper;

    private final FileChunkMapper fileChunkMapper;

    private final FileTypeValidator validator;

    private final LocalProperties localProperties;

    private final FileUploadProperties fileUploadProperties;

    @PostConstruct
    public void init() {
        try {
            log.debug(
                    "{}初始化本地存储位置,存储位置:{},临时文件位置:{}",
                    LogPrefix.STORAGE.p(),
                    localProperties.getUploadDir(),
                    localProperties.getUploadTempDir()
            );
            this.root = Paths.get(localProperties.getUploadDir());
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            this.temp = Paths.get(localProperties.getUploadTempDir());
            if (!Files.exists(temp)) {
                Files.createDirectories(temp);
            }
        } catch (IOException e) {
            log.error(LogPrefix.STORAGE.f("初始化本地存储位置失败"), e);
        }
    }

    @Override
    public void verify(@Nullable MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new FileTypeException("上传的文件不能为空");
        }
        // 使用策略模式进行文件类型验证
        if (!validator.validate(file)) {
            throw new FileTypeException("此类文件不允许上传");
        }
        // 文件大小
        if (file.getSize() > fileUploadProperties.getChunkSize()) {
            throw new FileTypeException("文件大小超过阈值");
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
        if (from.size() <= fileUploadProperties.getChunkSize()) {
            return FilePreprocessVO.ofFalse();
        }
        // 计算分片信息后响应
        int count = Math.toIntExact(from.size() / fileUploadProperties.getChunkSize());
        if (from.size() % fileUploadProperties.getChunkSize() != 0) {
            count++;
        }
        return FilePreprocessVO.chunk(Math.toIntExact(fileUploadProperties.getChunkSize()), count);
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
        this.verify(from.file());
        if (root == null) {
            throw new FileUploadException("存储目录配置错误");
        }
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
            if (temp == null) {
                throw new FileUploadException("存储目录配置错误");
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
        chunks.forEach(i -> log.debug("{}合并,文件ID:{},分片:{}", LogPrefix.STORAGE.p(), i.getFileId(), i.getChunkIndex()));

    }
}
