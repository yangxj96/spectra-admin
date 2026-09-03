/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.core.upload.javabean.entity.FileType;
import com.devops00.spectra.core.upload.javabean.from.FileTypePolicySaveFrom;
import com.devops00.spectra.core.upload.javabean.vo.FileTypePolicyVO;
import com.devops00.spectra.core.upload.mapper.FileTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 文件类型策略管理应用服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026-08-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileTypeManagementService extends BaseServiceImpl<FileTypeMapper, FileType> {

    private final FileUploadConverter fileUploadConverter;

    /** 查询全部未删除策略，包含停用策略。 */
    @Transactional(readOnly = true)
    public IPage<FileTypePolicyVO> page(Page<FileType> page) {
        var query = new LambdaQueryWrapper<FileType>()
                .orderByAsc(FileType::getCode);
        return fileUploadConverter.toTypePolicyVOPage(baseMapper.selectPage(page, query));
    }

    /** 查询单个策略。 */
    @Transactional(readOnly = true)
    public FileTypePolicyVO get(UUID id) {
        return fileUploadConverter.toTypePolicyVO(requirePolicy(id));
    }

    /** 新建策略。 */
    @Transactional
    public FileTypePolicyVO create(FileTypePolicySaveFrom from) {
        validate(from);
        String code = normalizeCode(from.getCode());
        if (baseMapper.selectCount(new LambdaQueryWrapper<FileType>()
                .eq(FileType::getCode, code)) > 0) {
            throw conflict("文件类型编码已存在");
        }
        var entity = new FileType();
        apply(entity, from, code);
        if (baseMapper.insert(entity) != 1) {
            throw conflict("保存文件类型策略失败");
        }
        return fileUploadConverter.toTypePolicyVO(entity);
    }

    /** 修改策略。 */
    @Transactional
    public FileTypePolicyVO modify(UUID id, FileTypePolicySaveFrom from) {
        validate(from);
        FileType entity = requirePolicy(id);
        if (from.getVersion() != null && !from.getVersion().equals(entity.getVersion())) {
            throw conflict("文件类型策略已被其他管理员修改，请刷新后重试");
        }
        String code = normalizeCode(from.getCode());
        if (baseMapper.selectCount(new LambdaQueryWrapper<FileType>()
                .eq(FileType::getCode, code)
                .ne(FileType::getId, id)) > 0) {
            throw conflict("文件类型编码已存在");
        }
        apply(entity, from, code);
        if (baseMapper.updateById(entity) != 1) {
            throw conflict("文件类型策略已被其他管理员修改，请刷新后重试");
        }
        return fileUploadConverter.toTypePolicyVO(entity);
    }

    /** 启用策略。 */
    @Transactional
    public FileTypePolicyVO enable(UUID id) {
        return changeEnabled(id, true);
    }

    /** 停用策略。 */
    @Transactional
    public FileTypePolicyVO disable(UUID id) {
        return changeEnabled(id, false);
    }

    private FileTypePolicyVO changeEnabled(UUID id, boolean enabled) {
        FileType entity = requirePolicy(id);
        entity.setEnabled(enabled);
        if (baseMapper.updateById(entity) != 1) {
            throw conflict("文件类型策略已被其他管理员修改，请刷新后重试");
        }
        return fileUploadConverter.toTypePolicyVO(entity);
    }

    private FileType requirePolicy(UUID id) {
        FileType entity = baseMapper.selectById(id);
        if (entity == null || entity.getDeleted() != null) {
            throw new FileUploadException(FileErrorCode.FILE_TYPE_NOT_FOUND, "文件类型策略不存在");
        }
        return entity;
    }

    private void validate(FileTypePolicySaveFrom from) {
        if (from == null) {
            throw invalid("文件类型策略不能为空");
        }
        validateTextArray(from.getAllowedExtensions(), "允许扩展名", false);
        validateTextArray(from.getAllowedContentTypes(), "允许媒体类型", true);
        validateMagicRules(from.getMagicRules());
    }

    private void validateTextArray(JsonNode node, String field, boolean contentType) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            throw invalid(field + "必须是非空数组");
        }
        var values = new HashSet<String>();
        for (JsonNode value : node) {
            if (value == null || !value.isTextual() || !StringUtils.hasText(value.asText())) {
                throw invalid(field + "只能包含非空文本");
            }
            String normalized = value.asText().trim().toLowerCase(Locale.ROOT);
            if (!contentType && normalized.startsWith(".")) {
                normalized = normalized.substring(1);
            }
            if (!values.add(normalized)) {
                throw invalid(field + "不能包含重复值");
            }
        }
    }

    private void validateMagicRules(JsonNode node) {
        if (node == null || !node.isArray()) {
            throw invalid("魔数规则必须是数组");
        }
        for (JsonNode rule : node) {
            if (rule == null || !rule.isObject()) {
                throw invalid("魔数规则只能是对象数组");
            }
            for (Map.Entry<String, JsonNode> field : rule.properties()) {
                String name = field.getKey();
                if (!name.equals("bytes") && !name.equals("offset") && !name.equals("description")) {
                    throw invalid("魔数规则包含不支持的字段");
                }
            }
            JsonNode bytes = rule.get("bytes");
            if (bytes == null
                    || !bytes.isTextual()
                    || !bytes.asText().matches("(?i)^[0-9a-f]+$")
                    || (bytes.asText().length() % 2 != 0)) {
                throw invalid("魔数规则 bytes 必须是偶数位十六进制文本");
            }
            JsonNode offset = rule.get("offset");
            if (offset != null && (!offset.isIntegralNumber() || offset.asInt() < 0)) {
                throw invalid("魔数规则 offset 必须是非负整数");
            }
            JsonNode description = rule.get("description");
            if (description != null && !description.isTextual()) {
                throw invalid("魔数规则 description 必须是文本");
            }
        }
    }

    private void apply(FileType entity, FileTypePolicySaveFrom from, String code) {
        entity.setCode(code);
        entity.setDisplayName(from.getDisplayName().trim());
        entity.setAllowedExtensions(from.getAllowedExtensions());
        entity.setAllowedContentTypes(from.getAllowedContentTypes());
        entity.setMagicRules(from.getMagicRules());
        entity.setMaxSize(from.getMaxSize());
        entity.setPreviewEnabled(from.getPreviewEnabled());
        entity.setDownloadEnabled(from.getDownloadEnabled());
        entity.setUploadEnabled(from.getUploadEnabled());
        entity.setDangerous(from.getDangerous());
        entity.setEnabled(from.getEnabled());
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private FileUploadException invalid(String message) {
        return new FileUploadException(FileErrorCode.FILE_TYPE_INVALID, message);
    }

    private FileUploadException conflict(String message) {
        return new FileUploadException(FileErrorCode.FILE_UPLOAD_CONFLICT, message);
    }
}
