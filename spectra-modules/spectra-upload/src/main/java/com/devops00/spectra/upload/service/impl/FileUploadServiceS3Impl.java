/*
 *  Copyright 2018-2026 yangxj96
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


import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.upload.javabean.constant.UploadType;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import com.devops00.spectra.upload.javabean.entity.FileUploadChunk;
import com.devops00.spectra.upload.javabean.entity.FileUploadTask;
import com.devops00.spectra.upload.javabean.from.FileUploadChunkFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadPreFrom;
import com.devops00.spectra.upload.javabean.vo.FileUploadChunkVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadPreVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadVO;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.properties.S3Properties;
import com.devops00.spectra.upload.service.FileInfoService;
import com.devops00.spectra.upload.service.FileUploadChunkService;
import com.devops00.spectra.upload.service.FileUploadService;
import com.devops00.spectra.upload.service.FileUploadTaskService;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/// 文件上传服务-S3协议
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/2 10:59
@Slf4j
@RequiredArgsConstructor
@Service("fileUploadServiceS3Impl")
public class FileUploadServiceS3Impl implements FileUploadService {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    private final S3Properties s3Properties;

    private final FileUploadProperties uploadProperties;

    private final FileInfoService infoService;

    private final FileUploadTaskService taskService;

    private final FileUploadChunkService chunkService;

    private final ApplicationEventPublisher publisher;

    @Override
    public UploadType getType() {
        return UploadType.S3;
    }

    @Override
    public FileUploadPreVO pre(FileUploadPreFrom from) {
        var vo = new FileUploadPreVO();

        // 1. 秒传判断
        var file = infoService.findByHash(from.getHash());
        if (file != null) {
            infoService.incrRefCount(file.getId());
            vo.setFileId(file.getId());
            vo.setExists(true);
            return vo;
        }

        // 2. 是否需要分片
        long chunkSize = uploadProperties.getChunkSize();
        boolean multipart = from.getSize() > chunkSize;

        // 3. 计算分片数量
        int totalChunks = multipart ? (int) Math.ceil((double) from.getSize() / chunkSize) : 1;

        // 生成系统唯一的存储文件名与上传任务 ID
        String filename = generatePathFilename(from.getFilename());
        String uploadId = UuidCreator.getTimeOrderedEpoch().toString();
        String s3UploadId = "";

        // 4. 如果是大文件分片上传，向 S3 申请一个 MultiPartUploadId 并在数据库备案
        if (multipart) {
            CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(filename)
                    .build();
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);
            s3UploadId = response.uploadId(); // 拿到 S3 核心标识符
        }

        // 5. 保存本地上传任务表
        FileUploadTask task = new FileUploadTask();
        task.setUploadId(uploadId);
        task.setFilename(filename); // 任务表 filename 字段直接存放生成的最终文件名
        task.setHash(from.getHash());
        task.setSize(from.getSize());
        task.setChunkSize(chunkSize);
        task.setTotalChunks(totalChunks);
        task.setStorageType(UploadType.S3);
        task.setStatus("INIT");
        if (multipart) {
            task.setEid(s3UploadId);
        }
        taskService.save(task);

        vo.setExists(false);
        vo.setMultipart(multipart);
        vo.setUploadId(uploadId); // 返回前端内部任务号
        vo.setChunkSize(chunkSize);
        return vo;
    }

    @Override
    public FileUploadVO upload(FileUploadFrom from) {
        MultipartFile file = from.getFile();
        String filename = generatePathFilename(file.getOriginalFilename());

        try {
            // 单文件直推 S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody
                            .fromInputStream(
                                    file.getInputStream(),
                                    file.getSize()
                            )
            );
        } catch (IOException e) {
            throw new RuntimeException("S3单文件上传失败", e);
        }

        // 保存文件主信息
        var fileInfo = new FileInfo();
        fileInfo.setFilename(filename);
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setSize(file.getSize());
        fileInfo.setHash(from.getHash());
        fileInfo.setStorageType(UploadType.S3);
        fileInfo.setContentType(file.getContentType());
        fileInfo.setStatus("ACTIVE");
        infoService.save(fileInfo);

        // 更新上传任务状态
        FileUploadTask task = taskService.findByUploadId(from.getUploadId());
        if (task != null) {
            task.setStatus("DONE");
            task.setFileId(fileInfo.getId());
            taskService.updateById(task);
        }

        FileUploadVO vo = new FileUploadVO();
        vo.setUrl("/api/file/preview/" + fileInfo.getId());

        publisher.publishEvent(new com.devops00.spectra.common.event.FileUploadFinishEvent(this, fileInfo.getId()));
        return vo;
    }

    @Override
    public FileUploadChunkVO chunk(FileUploadChunkFrom from) {
        FileUploadTask task = taskService.findByUploadId(from.getUploadId());
        if (task == null) {
            throw new IllegalArgumentException("上传任务不存在");
        }

        MultipartFile file = from.getFile();
        int chunkNumber = from.getIndex();

        // 调用 S3 的 uploadPart。
        // 注意：此处需要调用底层获取之前 pre 阶段向 S3 申请的真实 s3UploadId。
        // （如果之前未持久化，可从前端或通过缓存机制、或者在表里动态加上。此处假设利用 task 的额外标识机制拿到）
        String s3UploadId = getS3UploadIdFromTask(task);

        String eTag;
        try {
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(task.getFilename()) // 最终生成的系统存储文件名
                    .uploadId(s3UploadId)
                    .partNumber(chunkNumber)
                    .build();

            UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            eTag = uploadPartResponse.eTag(); // S3 返回的该分片唯一数字指纹
        } catch (IOException e) {
            throw new RuntimeException("S3分片传输失败", e);
        }

        // 录入分片明细表（保存关键的 Etag，合并时全靠它）
        FileUploadChunk chunk = new FileUploadChunk();
        chunk.setUploadId(from.getUploadId());
        chunk.setChunkNumber(chunkNumber);
        chunk.setSize(file.getSize());
        chunk.setEtag(eTag); // 存入你的这片 etag 字段

        try {
            chunkService.save(chunk);
        } catch (Exception e) {
            log.debug("分片明细记录已存在: {}", chunkNumber);
        }

        FileUploadChunkVO vo = new FileUploadChunkVO();
        vo.setChunkNumber(chunkNumber);
        return vo;
    }

    @Override
    public FileUploadVO merge(String uploadId) {
        synchronized (uploadId.intern()) {
            FileUploadTask task = taskService.findByUploadId(uploadId);
            if (task == null) {
                throw new IllegalArgumentException("上传任务不存在");
            }

            // 1. 校验分片数
            List<FileUploadChunk> dbChunks = chunkService.findByUploadId(uploadId);
            if (dbChunks.size() != task.getTotalChunks()) {
                throw new IllegalStateException("本地校验失败：云端分片未上传完整");
            }

            // 2. 将本地分片按 chunk_number 升序排序（S3合并协议强制要求）
            dbChunks.sort(Comparator.comparingInt(FileUploadChunk::getChunkNumber));

            // 3. 构建 S3 要求的 CompletedPart 集合
            List<CompletedPart> completedParts = new ArrayList<>();
            for (FileUploadChunk c : dbChunks) {
                completedParts.add(CompletedPart.builder()
                        .partNumber(c.getChunkNumber())
                        .eTag(c.getEtag()) // 必须与上传时 S3 返回的完全一致
                        .build());
            }

            String s3UploadId = getS3UploadIdFromTask(task);

            // 4. 发起远程 S3 端的合拢
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build();

            CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(task.getFilename())
                    .uploadId(s3UploadId)
                    .multipartUpload(completedMultipartUpload)
                    .build();

            s3Client.completeMultipartUpload(completeMultipartUploadRequest);

            // 5. 登记主表
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(task.getFilename());
            fileInfo.setOriginalName(task.getFilename());
            fileInfo.setSize(task.getSize());
            fileInfo.setHash(task.getHash());
            fileInfo.setStorageType(UploadType.S3);
            fileInfo.setStatus("ACTIVE");
            infoService.save(fileInfo);

            task.setStatus("DONE");
            task.setFileId(fileInfo.getId());
            taskService.updateById(task);

            FileUploadVO vo = new FileUploadVO();
            vo.setUrl("/api/file/preview/" + fileInfo.getId());

            publisher.publishEvent(new com.devops00.spectra.common.event.FileUploadFinishEvent(this, fileInfo.getId()));
            return vo;
        }
    }

    @Override
    public void preview(FileInfo file) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || attributes.getResponse() == null) {
            throw new IllegalStateException("不在 Web 请求上下文中");
        }
        HttpServletResponse response = attributes.getResponse();

        // 302 重定向架构核心：内存计算安全的短效授权链接
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(file.getFilename())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                // 10分钟后该链接自动失效
                .signatureDuration(Duration.ofMinutes(s3Properties.getPreviewMinutes()))
                .getObjectRequest(getObjectRequest)
                .build();

        // 生成带有时效加密串的完整对外 URL
        String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        // 优雅抛给前端浏览器，流量完美分流至 RustFS 容器
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY); // 302
        response.setHeader(HttpHeaders.LOCATION, presignedUrl);
        log.debug("{} 触发 S3 302 预览分流成功, 文件 ID: {}", LogPrefix.STORAGE.p(), file.getId());
    }

    @Override
    public InputStream openStream(FileInfo fileInfo) {
        log.debug("{} 开始获取 S3 文件流, 文件ID: {}, 存储Key: {}",
                LogPrefix.STORAGE.p(), fileInfo.getId(), fileInfo.getFilename());
        try {
            // 1. 构建 S3 获取对象的请求
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(fileInfo.getFilename()) // 数据库中存放的实际存储文件名/路径
                    .build();

            // 2. 调用 S3 客户端获取输入流
            // s3Client.getObject() 返回的是 ResponseInputStream，它继承了 InputStream
            return s3Client.getObject(getObjectRequest);

        } catch (NoSuchKeyException e) {
            log.error("{} S3 中未找到指定文件: {}", LogPrefix.STORAGE.p(), fileInfo.getFilename(), e);
            throw new RuntimeException("文件在云存储中不存在", e);
        } catch (S3Exception e) {
            log.error("{} 调用 S3 获取文件流失败", LogPrefix.STORAGE.p(), e);
            throw new RuntimeException("远程云存储服务异常", e);
        }
    }

    @Override
    public void download(FileInfo file) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || attributes.getResponse() == null) {
            throw new IllegalStateException("不在 Web 请求上下文中");
        }
        HttpServletResponse response = attributes.getResponse();

        String encodedFilename = java.net.URLEncoder
                .encode(file.getOriginalName(), java.nio.charset.StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(file.getFilename())
                .responseContentDisposition("attachment; filename=\"" + encodedFilename + "\"")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Properties.getPreviewMinutes()))
                .getObjectRequest(getObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader(HttpHeaders.LOCATION, presignedUrl);
        log.debug("{} 触发 S3 302 下载分流成功, 文件 ID: {}", LogPrefix.STORAGE.p(), file.getId());
    }

    /// 辅助小工具：由于跨方法需要用到 S3 的 uploadId，
    /// 你可以在本地设计中通过在 `file_upload_task` 表增加字段保存它，
    /// 或是临时拼装在某个非关键字段里（如存放在备注或扩展字段）。
    private String getS3UploadIdFromTask(FileUploadTask task) {
        if (task.getEid() == null || task.getEid().isBlank()) {
            throw new IllegalStateException("该任务缺少 S3 端的 MultipartUploadId (eid)");
        }
        return task.getEid();
    }

}
