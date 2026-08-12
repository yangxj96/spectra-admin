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
import com.devops00.spectra.common.event.FileUploadFinishEvent;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.FileUploadException;
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
import com.devops00.spectra.upload.properties.LocalProperties;
import com.devops00.spectra.upload.service.FileInfoService;
import com.devops00.spectra.upload.service.FileUploadChunkService;
import com.devops00.spectra.upload.service.FileUploadService;
import com.devops00.spectra.upload.service.FileUploadTaskService;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件上传服务-本地上传
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/4/2 10:59
 */
@Slf4j
@NullMarked
@Service("fileUploadServiceLocalImpl")
public class FileUploadServiceLocalImpl implements FileUploadService {

    /**
     * 本地文件管理的根文件路径
     */
    private final Path root;

    /**
     * 本地文件管理的临时文件路径
     */
    private final Path temp;

    private final FileUploadProperties uploadProperties;

    private final FileInfoService infoService;

    private final FileUploadTaskService taskService;

    private final FileUploadChunkService chunkService;

    private final ApplicationEventPublisher publisher;

    public FileUploadServiceLocalImpl(LocalProperties properties, FileUploadProperties uploadProperties, FileInfoService infoService,
                                      FileUploadTaskService taskService, FileUploadChunkService chunkService, ApplicationEventPublisher publisher)
            throws IOException {
        this.uploadProperties = uploadProperties;
        this.infoService = infoService;
        this.taskService = taskService;
        this.chunkService = chunkService;
        this.publisher = publisher;

        log.debug("{}初始化本地存储位置,存储位置:{},临时文件位置:{}", LogPrefix.STORAGE.p(), properties.getUploadDir(), properties.getUploadTempDir());
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
    public UploadType getType() {
        return UploadType.LOCAL;
    }

    @Override
    public FileUploadPreVO pre(FileUploadPreFrom from) {
        var vo = new FileUploadPreVO();

        // 秒传判断
        var file = infoService.findByHash(from.getHash());

        if (file != null) {
            infoService.incrRefCount(file.getId());
            vo.setFileId(file.getId());
            vo.setExists(true);
            return vo;
        }

        // 是否需要分片
        long chunkSize = uploadProperties.getChunkSize();
        boolean multipart = from.getSize() > chunkSize;

        // 计算分片数量
        int totalChunks = multipart ? (int) Math.ceil((double) from.getSize() / chunkSize) : 1;

        // 创建上传任务
        String uploadId = UuidCreator.getTimeOrderedEpoch().toString();

        FileUploadTask task = new FileUploadTask();
        task.setUploadId(uploadId);
        task.setFilename(generatePathFilename(from.getFilename()));
        task.setHash(from.getHash());
        task.setSize(from.getSize());
        task.setChunkSize(chunkSize);
        task.setTotalChunks(totalChunks);
        task.setStorageType(UploadType.LOCAL);
        task.setStatus("INIT");

        taskService.save(task);

        // 4. 返回结果
        vo.setExists(false);
        vo.setMultipart(multipart);
        vo.setUploadId(uploadId);
        vo.setChunkSize(chunkSize);

        return vo;
    }

    @Override
    @Transactional
    public FileUploadVO upload(FileUploadFrom from) {
        MultipartFile file = from.getFile();
        // 拼装存储文件名
        String filename = generatePathFilename(file.getOriginalFilename());
        // 构建存储路径
        Path path = buildFilePath(filename);
        // 尝试保存
        try {
            file.transferTo(path);
        } catch (IOException e) {
            throw new FileUploadException("文件保存失败");
        }
        // 成功了存入数据库记录且构建响应vo
        String url = "/file/" + filename;
        // 保存 file_info
        var fileInfo = new FileInfo();
        fileInfo.setFilename(filename);
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setSize(file.getSize());
        fileInfo.setHash(from.getHash());
        fileInfo.setStorageType(UploadType.LOCAL);
        fileInfo.setStatus("ACTIVE");

        infoService.save(fileInfo);

        // 把task表对应的记录结果修改下
        FileUploadTask task = taskService.findByUploadId(from.getUploadId());
        task.setStatus("DON");
        taskService.updateById(task);

        FileUploadVO vo = new FileUploadVO();
        vo.setUrl(url);
        vo.setFileId(fileInfo.getId());

        publisher.publishEvent(new FileUploadFinishEvent(this, fileInfo.getId()));
        return vo;
    }

    @Override
    @Transactional
    public FileUploadChunkVO chunk(FileUploadChunkFrom from) {
        var task = taskService.findByUploadId(from.getUploadId());
        if (task == null) {
            throw new DataNotExistException("上传任务不存在");
        }

        int chunkNumber = from.getIndex();

        Path dir = buildTempDir(from.getUploadId());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new FileUploadException("创建临时目录失败");
        }

        Path chunkPath = dir.resolve(String.valueOf(chunkNumber));

        try {
            from.getFile().transferTo(chunkPath);
        } catch (IOException e) {
            throw new FileUploadException("分片文件保存失败");
        }

        FileUploadChunk chunk = new FileUploadChunk();
        chunk.setUploadId(from.getUploadId());
        chunk.setChunkNumber(chunkNumber);
        chunk.setSize(from.getFile().getSize());

        try {
            chunkService.save(chunk);
        } catch (Exception e) {
            log.debug("分片已存在: {}", chunkNumber);
        }
        return buildChunkVO(chunkNumber);
    }

    @Override
    @Transactional
    public FileUploadVO merge(String uploadId) {
        synchronized (uploadId.intern()) {

            var task = taskService.findByUploadId(uploadId);
            if (task == null) {
                throw new DataNotExistException("上传任务不存在");
            }

            // 校验分片是否完整
            int uploaded = chunkService.countByUploadId(uploadId);
            if (uploaded != task.getTotalChunks()) {
                throw new DataException("分片未上传完成");
            }

            Path tempDir = buildTempDir(uploadId);
            // 拼装存储文件名
            String filename = UuidCreator.getTimeOrderedEpoch().toString() + getSuffix(task.getFilename());

            Path dest = buildFilePath(filename);

            try (OutputStream out = Files.newOutputStream(dest)) {
                for (int i = 1; i <= task.getTotalChunks(); i++) {
                    Path chunkPath = tempDir.resolve(String.valueOf(i));
                    Files.copy(chunkPath, out);
                }
            } catch (IOException e) {
                throw new FileUploadException("合并失败");
            }

            String url = "/file/" + filename;

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(filename);
            fileInfo.setOriginalName(task.getFilename());
            fileInfo.setSize(task.getSize());
            fileInfo.setHash(task.getHash());
            fileInfo.setStorageType(UploadType.LOCAL);
            fileInfo.setStatus("ACTIVE");

            infoService.save(fileInfo);

            task.setStatus("DONE");
            task.setFileId(fileInfo.getId());
            taskService.updateById(task);

            // 清理临时文件
            try {
                FileSystemUtils.deleteRecursively(tempDir);
            } catch (Exception e) {
                log.warn("清理临时目录失败: {}", tempDir, e);
            }

            publisher.publishEvent(new FileUploadFinishEvent(this, fileInfo.getId()));
            return buildUploadVO(url, fileInfo.getId());
        }
    }

    @Override
    public void preview(FileInfo file) {
        // 从当前线程的上下文中获取请求属性
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("{}无法获取当前请求上下文，可能未在 Web 线程中调用", LogPrefix.STORAGE.p());
            throw new DataException("当前不在有效的 Web 请求上下文中");
        }
        // 直接拿到真正的 HttpServletResponse
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            log.error("{}获取的 HttpServletResponse 为空", LogPrefix.STORAGE.p());
            return;
        }
        // 构建本地文件的绝对路径
        Path filePath = buildFilePath(file.getFilename());
        // 检查文件在物理磁盘上是否存在
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
            log.warn("{}预览文件不存在, filename: {}", LogPrefix.STORAGE.p(), file.getFilename());
            return;
        }
        try {
            // 动态探测并设置文件的媒体类型（MIME Type），例如 image/jpeg, application/pdf
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                // 如果探测不到，默认采用二进制流
                contentType = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            response.setContentType(contentType);
            // 设置为 inline（内联），告诉浏览器“能预览就预览，不能预览再下载”
            // 对文件名进行 URL 编码，防止中文或特殊字符在 Header 中乱码
            String encodedFilename = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFilename + "\"");

            // 设置文件大小，方便浏览器展示进度条
            response.setContentLengthLong(file.getSize());

            // 使用 NIO 将文件高效传输到 Response 的输出流
            try (OutputStream out = response.getOutputStream()) {
                Files.copy(filePath, out);
                out.flush();
            }
        } catch (IOException e) {
            log.error("{}文件预览流传输失败, filename: {}", LogPrefix.STORAGE.p(), file.getFilename(), e);
            // 注意：此时如果已经输出了部分流，setStatus 可能失效，但仍建议设置
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    public InputStream openStream(FileInfo fileInfo) {
        log.debug("{} 开始获取本地文件流, 文件ID: {}, 存储Key: {}", LogPrefix.STORAGE.p(), fileInfo.getId(), fileInfo.getFilename());

        // 1. 获取文件在磁盘上的绝对路径
        Path filePath = buildFilePath(fileInfo.getFilename());

        // 2. 严妙校验：检查文件在物理磁盘上是否存在，防止因意外删改导致抛出底层原生异常
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            log.error("{} 本地物理文件不存在: {}", LogPrefix.STORAGE.p(), filePath);
            throw new DataNotExistException("本地存储中未找到该文件");
        }

        try {
            // 3. 基于 NIO 高效打开本地文件输入流
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("{} 打开本地文件流失败, filename: {}", LogPrefix.STORAGE.p(), fileInfo.getFilename(), e);
            throw new FileUploadException("读取物理文件流异常");
        }
    }

    @Override
    public void download(FileInfo file) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("{}无法获取当前请求上下文，可能未在 Web 线程中调用", LogPrefix.STORAGE.p());
            throw new DataException("当前不在有效的 Web 请求上下文中");
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            log.error("{}获取的 HttpServletResponse 为空", LogPrefix.STORAGE.p());
            return;
        }
        Path filePath = buildFilePath(file.getFilename());
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.warn("{}下载文件不存在, filename: {}", LogPrefix.STORAGE.p(), file.getFilename());
            return;
        }
        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            response.setContentType(contentType);
            String encodedFilename = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"");
            response.setContentLengthLong(file.getSize());
            try (OutputStream out = response.getOutputStream()) {
                Files.copy(filePath, out);
                out.flush();
            }
        } catch (IOException e) {
            log.error("{}文件下载流传输失败, filename: {}", LogPrefix.STORAGE.p(), file.getFilename(), e);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * 构建文件保存路径
     * <p>
     * 文件名本身包含年月目录前缀（例如 202608/uuid.png），因此这里直接相对根目录解析，
     * 避免再次拼接年月目录导致实际保存路径变成 202608/202608/uuid.png。
     *
     * @param filename 文件名称（可包含相对目录）
     */
    private Path buildFilePath(String filename) {
        Path filePath = root.resolve(filename).normalize();
        if (!filePath.startsWith(root)) {
            throw new FileUploadException("文件路径非法");
        }

        Path parent = filePath.getParent();
        try {
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new FileUploadException("创建文件目录失败");
        }
        return filePath;
    }

    /**
     * 构建临时文件路径
     *
     * @param uploadId 文件ID
     */
    private Path buildTempDir(String uploadId) {
        return temp.resolve(uploadId);
    }

    /**
     * 构建上传响应VO
     *
     * @param url 地址
     */
    private FileUploadVO buildUploadVO(String url, java.util.UUID fileId) {
        FileUploadVO vo = new FileUploadVO();
        vo.setUrl(url);
        vo.setFileId(fileId);
        return vo;
    }

    /**
     * 构建分片上传响应VO
     *
     * @param chunkNumber 分片序号
     */
    private FileUploadChunkVO buildChunkVO(int chunkNumber) {
        FileUploadChunkVO vo = new FileUploadChunkVO();
        vo.setChunkNumber(chunkNumber);
        return vo;
    }
}
