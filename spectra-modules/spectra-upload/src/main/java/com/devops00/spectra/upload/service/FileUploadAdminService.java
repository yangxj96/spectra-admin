/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.upload.api.FileErrorCode;
import com.devops00.spectra.upload.api.FileUploadException;
import com.devops00.spectra.upload.javabean.constant.UploadPartStatus;
import com.devops00.spectra.upload.javabean.constant.UploadSessionStatus;
import com.devops00.spectra.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.upload.javabean.entity.FileUploadPart;
import com.devops00.spectra.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.upload.javabean.from.FileAdminOperationFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadAdminPageRequest;
import com.devops00.spectra.upload.javabean.vo.FileUploadAdminDetailVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadAdminVO;
import com.devops00.spectra.upload.mapper.FileUploadPartMapper;
import com.devops00.spectra.upload.mapper.FileUploadSessionMapper;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传任务管理应用服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026-08-31
 */
@Service
@RequiredArgsConstructor
public class FileUploadAdminService {

    private final FileUploadSessionMapper sessionMapper;

    private final FileUploadPartMapper partMapper;

    private final FileUploadProperties properties;

    private final FileUploadConverter fileUploadConverter;

    /** 分页查询上传任务，不返回存储容器、对象键或供应商上传 ID。 */
    @Transactional(readOnly = true)
    public IPage<FileUploadAdminVO> page(Page<FileUploadSession> page, FileUploadAdminPageRequest request) {
        var query = new LambdaQueryWrapper<FileUploadSession>()
                .like(request != null && StringUtils.hasText(request.getOriginalName()),
                        FileUploadSession::getOriginalName, request == null ? null : request.getOriginalName())
                .eq(request != null && request.getOwnerUserId() != null,
                        FileUploadSession::getOwnerUserId, request == null ? null : request.getOwnerUserId())
                .eq(request != null && request.getStatus() != null,
                        FileUploadSession::getStatus, request == null ? null : request.getStatus())
                .orderByDesc(FileUploadSession::getCreatedAt);
        return sessionMapper.selectPage(page, query).convert(session -> {
            var vo = fileUploadConverter.toUploadAdminVO(session);
            fillPartSummary(vo, partMapper.findBySessionId(session.getId()));
            return vo;
        });
    }

    /** 查询上传任务及其分片状态。 */
    @Transactional(readOnly = true)
    public FileUploadAdminDetailVO detail(UUID uploadId) {
        FileUploadSession session = requireSession(uploadId);
        var detail = fileUploadConverter.toUploadAdminDetailVO(session);
        detail.setParts(fileUploadConverter.toUploadPartAdminVOList(partMapper.findBySessionId(uploadId)));
        return detail;
    }

    /** 管理员取消任务；状态变更后由统一清理任务负责外部存储清理。 */
    @Transactional
    public void cancel(UUID uploadId, FileAdminOperationFrom operation) {
        FileUploadSession session = sessionMapper.selectForUpdate(uploadId);
        if (session == null) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_NOT_FOUND, "上传任务不存在");
        }
        if (isTerminal(session.getStatus())) {
            return;
        }
        if (session.getStatus() != UploadSessionStatus.UPLOADING) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_CONFLICT, "正在校验的上传任务不能取消");
        }
        if (sessionMapper.markCanceled(uploadId, Instant.now().plus(properties.getRecordRetention())) != 1) {
            return;
        }
    }

    private FileUploadSession requireSession(UUID uploadId) {
        FileUploadSession session = sessionMapper.selectById(uploadId);
        if (session == null || session.getDeleted() != null) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_NOT_FOUND, "上传任务不存在");
        }
        return session;
    }

    private boolean isTerminal(UploadSessionStatus status) {
        return status == UploadSessionStatus.READY
                || status == UploadSessionStatus.FAILED
                || status == UploadSessionStatus.CANCELED
                || status == UploadSessionStatus.EXPIRED
                || status == UploadSessionStatus.CLEANED;
    }

    private void fillPartSummary(FileUploadAdminVO vo, List<FileUploadPart> parts) {
        vo.setCompletedParts((int) parts.stream().filter(part -> part.getStatus() == UploadPartStatus.CONFIRMED).count());
        vo.setUploadedBytes(parts.stream()
                .map(FileUploadPart::getUploadedSize)
                .filter(size -> size != null)
                .reduce(0L, Long::sum));
    }

}
