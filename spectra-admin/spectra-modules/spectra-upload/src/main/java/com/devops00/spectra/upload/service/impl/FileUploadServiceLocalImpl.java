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
import com.devops00.spectra.upload.javabean.vo.FileUploadStatusVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadVO;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.properties.LocalProperties;
import com.devops00.spectra.upload.service.FileInfoService;
import com.devops00.spectra.upload.service.FileUploadChunkService;
import com.devops00.spectra.upload.service.FileUploadService;
import com.devops00.spectra.upload.service.FileUploadTaskService;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/// 文件上传服务-本地上传
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/2 10:59
@Slf4j
@RequiredArgsConstructor
@Service("fileUploadServiceLocalImpl")
public class FileUploadServiceLocalImpl implements FileUploadService {

    /// 本地文件管理的根文件路径
    private Path root;

    /// 本地文件管理的临时文件路径
    private Path temp;

    private final LocalProperties properties;

    private final FileUploadProperties uploadProperties;

    private final FileInfoService infoService;

    private final FileUploadTaskService taskService;

    private final FileUploadChunkService chunkService;

    @PostConstruct
    public void init() {
        try {
            log.debug(
                    "{}初始化本地存储位置,存储位置:{},临时文件位置:{}",
                    LogPrefix.STORAGE.p(),
                    properties.getUploadDir(),
                    properties.getUploadTempDir()
            );
            this.root = Paths.get(properties.getUploadDir());
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            this.temp = Paths.get(properties.getUploadTempDir());
            if (!Files.exists(temp)) {
                Files.createDirectories(temp);
            }
        } catch (IOException e) {
            log.error(LogPrefix.STORAGE.f("初始化本地存储位置失败"), e);
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

            vo.setExists(true);
            vo.setUrl(file.getUrl());
            return vo;
        }

        // 是否需要分片
        long chunkSize = uploadProperties.getChunkSize();
        boolean multipart = from.getSize() > chunkSize;

        // 计算分片数量
        int totalChunks = multipart
                ? (int) Math.ceil((double) from.getSize() / chunkSize)
                : 1;

        // 创建上传任务
        String uploadId = UuidCreator.getTimeOrderedEpoch().toString();

        FileUploadTask task = new FileUploadTask();
        task.setUploadId(uploadId);
        task.setFilename(from.getFilename());
        task.setHash(from.getHash());
        task.setSize(from.getSize());
        task.setChunkSize(chunkSize);
        task.setTotalChunks(totalChunks);
        task.setUploadedChunks(0);
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
    public FileUploadVO upload(FileUploadFrom from) {
        MultipartFile file = from.getFile();

        String filename = UuidCreator.getTimeOrderedEpoch().toString() + "_" + file.getOriginalFilename();

        Path path = buildFilePath(filename);

        try {
            file.transferTo(path);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }

        String url = "/file/" + filename;

        // 保存 file_info
        var fileInfo = new FileInfo();
        fileInfo.setFilename(filename);
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setSize(file.getSize());
        fileInfo.setHash(from.getHash());
        fileInfo.setUrl(url);
        fileInfo.setStorageType(UploadType.LOCAL);
        fileInfo.setStatus("ACTIVE");

        infoService.save(fileInfo);

        FileUploadVO vo = new FileUploadVO();
        vo.setUrl(url);

        return vo;
    }

    @Override
    public FileUploadChunkVO chunk(FileUploadChunkFrom from) {
        var task = taskService.findByUploadId(from.getUploadId());
        if (task == null) {
            throw new IllegalArgumentException("上传任务不存在");
        }

        int chunkNumber = from.getCount();

        Path dir = buildTempDir(from.getUploadId());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path chunkPath = dir.resolve(String.valueOf(chunkNumber));

        try {
            from.getFile().transferTo(chunkPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileUploadChunk chunk = new FileUploadChunk();
        chunk.setUploadId(from.getUploadId());
        chunk.setChunkNumber(chunkNumber);
        chunk.setSize(from.getFile().getSize());
        chunk.setStatus("UPLOADED");

        try {
            chunkService.save(chunk);
            taskService.incrUploadedChunks(task.getId());
        } catch (Exception e) {
            log.debug("分片已存在: {}", chunkNumber);
        }

        return buildChunkVO(chunkNumber);
    }

    @Override
    public FileUploadVO merge(String uploadId) {
        synchronized (uploadId.intern()) { // ⭐并发保护

            var task = taskService.findByUploadId(uploadId);
            if (task == null) {
                throw new IllegalArgumentException("上传任务不存在");
            }

            // 校验分片是否完整
            int uploaded = chunkService.countByUploadId(uploadId);
            if (uploaded != task.getTotalChunks()) {
                throw new IllegalStateException("分片未上传完成");
            }

            Path tempDir = buildTempDir(uploadId);
            String filename = UUID.randomUUID() + "_" + task.getFilename();
            Path dest = buildFilePath(filename);

            try (OutputStream out = Files.newOutputStream(dest)) {
                for (int i = 1; i <= task.getTotalChunks(); i++) {
                    Path chunkPath = tempDir.resolve(String.valueOf(i));
                    Files.copy(chunkPath, out);
                }
            } catch (IOException e) {
                throw new RuntimeException("合并失败", e);
            }

            String url = "/file/" + filename;

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(filename);
            fileInfo.setOriginalName(task.getFilename());
            fileInfo.setSize(task.getSize());
            fileInfo.setHash(task.getHash());
            fileInfo.setUrl(url);
            fileInfo.setStorageType(UploadType.LOCAL);
            fileInfo.setStatus("ACTIVE");

            infoService.save(fileInfo);

            task.setStatus("DONE");
            task.setFileId(fileInfo.getId());
            taskService.updateById(task);

            // 清理临时文件
            FileSystemUtils.deleteRecursively(tempDir);

            return buildUploadVO(url);
        }
    }

    @Override
    public FileUploadStatusVO getStatus(String uploadId) {
        var task = taskService.findByUploadId(uploadId);
        var chunks = chunkService.findByUploadId(uploadId);
        FileUploadStatusVO vo = new FileUploadStatusVO();
        vo.setStatus(task.getStatus());
        vo.setTotalChunks(task.getTotalChunks());
        vo.setChunkSize(task.getChunkSize());
        vo.setUploadedChunks(
                chunks.stream()
                        .map(FileUploadChunk::getChunkNumber)
                        .sorted()
                        .toList()
        );
        vo.setCompleted("DONE".equals(task.getStatus()));
        return vo;
    }

    private Path buildFilePath(String filename) {
        return root.resolve(filename);
    }

    private Path buildTempDir(String uploadId) {
        return temp.resolve(uploadId);
    }

    private FileUploadVO buildUploadVO(String url) {
        FileUploadVO vo = new FileUploadVO();
        vo.setUrl(url);
        return vo;
    }

    private FileUploadChunkVO buildChunkVO(int chunkNumber) {
        FileUploadChunkVO vo = new FileUploadChunkVO();
        vo.setChunkNumber(chunkNumber);
        return vo;
    }
}
