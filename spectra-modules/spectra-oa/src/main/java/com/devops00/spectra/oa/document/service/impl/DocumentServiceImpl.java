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

package com.devops00.spectra.oa.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.notification.NotificationTemplateCode;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.oa.document.javabean.converter.DocumentConverter;
import com.devops00.spectra.oa.document.javabean.constant.DocumentStatus;
import com.devops00.spectra.oa.document.javabean.entity.Document;
import com.devops00.spectra.oa.document.javabean.entity.DocumentFolder;
import com.devops00.spectra.oa.document.javabean.entity.DocumentVersion;
import com.devops00.spectra.oa.document.javabean.from.DocumentFolderSaveFrom;
import com.devops00.spectra.oa.document.javabean.from.DocumentPageFrom;
import com.devops00.spectra.oa.document.javabean.from.DocumentSaveFrom;
import com.devops00.spectra.oa.document.javabean.from.DocumentVersionFrom;
import com.devops00.spectra.oa.document.javabean.vo.DocumentFolderVO;
import com.devops00.spectra.oa.document.javabean.vo.DocumentVO;
import com.devops00.spectra.oa.document.javabean.vo.DocumentVersionVO;
import com.devops00.spectra.oa.document.mapper.DocumentFolderMapper;
import com.devops00.spectra.oa.document.mapper.DocumentMapper;
import com.devops00.spectra.oa.document.mapper.DocumentVersionMapper;
import com.devops00.spectra.oa.document.service.DocumentService;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.common.port.file.FileAssetPort;
import com.devops00.spectra.common.port.file.FileReferenceService;
import com.devops00.spectra.oa.support.OaFileReferenceBinder;
import com.devops00.spectra.oa.support.OaFileReferenceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 文档表主表-服务默认实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/30 14:13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends BaseServiceImpl<DocumentMapper, Document> implements DocumentService {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";
    private static final String VISIBILITY_DEPARTMENT = "DEPARTMENT";
    private static final String VISIBILITY_PRIVATE = "PRIVATE";

    private final DocumentVersionMapper versionMapper;
    private final DocumentFolderMapper folderMapper;
    private final FileAssetPort fileAssetPort;
    private final FileReferenceService fileReferenceService;
    private final OaFileReferenceBinder fileReferenceBinder;
    private final NotificationService notificationService;
    private final UserService userService;
    private final DocumentConverter documentConverter;
    private final SecurityContextAccessor securityContextAccessor;

    @Override
    public IPage<DocumentVO> page(PageFrom page, DocumentPageFrom params) {
        var wrapper = new LambdaQueryWrapper<Document>();
        var user = securityContextAccessor.currentUser();
        var userId = securityContextAccessor.currentUserId();
        if (user == null || userId == null || user.getDepartmentId() == null) {
            return new Page<>(page.getPageNum(), page.getPageSize(), 0);
        }
        wrapper.and(query -> query.eq(Document::getOwnerId, userId)
                .or()
                .eq(Document::getVisibility, VISIBILITY_PUBLIC)
                .or(q -> q.eq(Document::getVisibility, VISIBILITY_DEPARTMENT).eq(Document::getDepartmentId, user.getDepartmentId())));
        if (params != null && StringUtils.hasText(params.getKeyword())) {
            wrapper.like(Document::getTitle, params.getKeyword());
        }
        if (params != null && StringUtils.hasText(params.getStatus())) {
            wrapper.eq(Document::getStatus, params.getStatus());
        }
        if (params != null && params.getFolderId() != null) {
            wrapper.eq(Document::getFolderId, params.getFolderId());
        }
        wrapper.orderByDesc(Document::getUpdatedAt);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<DocumentVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(document -> documentConverter.toVO(document, currentVersion(document.getId()))).toList());
        return voPage;
    }

    @Override
    public DocumentVO get(UUID id) {
        var document = requireAccessible(id);
        return documentConverter.toVO(document, currentVersion(document.getId()));
    }

    @Override
    @Transactional
    public UUID created(DocumentSaveFrom from) {
        var user = securityContextAccessor.currentUser();
        if (user == null || user.getId() == null || user.getDepartmentId() == null) {
            throw new DataSaveException("当前用户组织信息不可用");
        }
        var entity = documentConverter.toEntity(from);
        entity.setTitle(from.getTitle().trim());
        entity.setVisibility(normalizeVisibility(from.getVisibility()));
        entity.setStatus(DocumentStatus.DRAFT.getValue());
        entity.setOwnerId(user.getId());
        entity.setDepartmentId(user.getDepartmentId());
        if (!save(entity)) {
            throw new DataSaveException("保存文档失败");
        }
        return entity.getId();
    }

    @Override
    @Transactional
    public void modify(UUID id, DocumentSaveFrom from) {
        var entity = requireOwner(id);
        documentConverter.updateEntity(from, entity);
        entity.setTitle(from.getTitle().trim());
        entity.setVisibility(normalizeVisibility(from.getVisibility()));
        if (!updateById(entity)) {
            throw new DataSaveException("更新文档失败");
        }
    }

    @Override
    @Transactional
    public UUID addVersion(UUID id, DocumentVersionFrom from) {
        var document = requireOwner(id);
        var file = fileAssetPort.requireReadyForReference(from.getFileAssetId(), securityContextAccessor.currentUserId());
        var latest = versionMapper.selectOne(new LambdaQueryWrapper<DocumentVersion>().eq(DocumentVersion::getDocumentId, document.getId())
                .orderByDesc(DocumentVersion::getVersionNo)
                .last("limit 1"));
        var version = new DocumentVersion();
        version.setDocumentId(document.getId());
        version.setVersionNo(latest == null ? 1 : latest.getVersionNo() + 1);
        version.setFileAssetId(file.fileAssetId());
        version.setFileName(StringUtils.hasText(from.getFileName()) ? from.getFileName() : file.originalName());
        version.setFileSize(Objects.requireNonNullElse(from.getFileSize(), file.size()));
        version.setContentType(StringUtils.hasText(from.getContentType()) ? from.getContentType() : file.contentType());
        version.setVersionNote(from.getVersionNote());
        version.setCurrentVersion(true);
        versionMapper.update(null, new LambdaUpdateWrapper<DocumentVersion>().eq(DocumentVersion::getDocumentId, document.getId())
                .eq(DocumentVersion::getCurrentVersion, true)
                .set(DocumentVersion::getCurrentVersion, false));
        if (versionMapper.insert(version) != 1) {
            throw new DataSaveException("保存文档版本失败");
        }
        fileReferenceService.register(fileReferenceBinder.content(file.fileAssetId(), OaFileReferenceType.DOCUMENT_VERSION,
                version.getId(), version.getFileName()));
        return version.getId();
    }

    @Override
    public List<DocumentVersionVO> versions(UUID id) {
        var document = requireAccessible(id);
        return versionMapper.selectList(new LambdaQueryWrapper<DocumentVersion>().eq(DocumentVersion::getDocumentId, document.getId())
                .orderByDesc(DocumentVersion::getVersionNo)).stream().map(documentConverter::toVersionVO).toList();
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        var document = requireOwner(id);
        if (DocumentStatus.ARCHIVED.getValue().equals(document.getStatus())) {
            throw new DataSaveException("已归档文档不能再次发布");
        }
        var current = currentVersion(document.getId());
        if (current == null) {
            throw new DataSaveException("文档必须先上传一个版本");
        }
        document.setStatus(DocumentStatus.PUBLISHED.getValue());
        document.setPublishedAt(Instant.now());
        if (!updateById(document)) {
            throw new DataSaveException("发布文档失败");
        }
        sendPublishNotification(document);
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        var document = requireOwner(id);
        if (DocumentStatus.ARCHIVED.getValue().equals(document.getStatus())) {
            throw new DataSaveException("文档已经归档");
        }
        document.setStatus(DocumentStatus.ARCHIVED.getValue());
        if (!updateById(document)) {
            throw new DataSaveException("归档文档失败");
        }
    }

    @Override
    public List<DocumentFolderVO> folders() {
        var user = securityContextAccessor.currentUser();
        if (user == null || user.getDepartmentId() == null) {
            return List.of();
        }
        var wrapper = new LambdaQueryWrapper<DocumentFolder>()
                .and(query -> query.eq(DocumentFolder::getVisibility, VISIBILITY_PUBLIC)
                        .or(
                                q -> q.eq(DocumentFolder::getVisibility, VISIBILITY_DEPARTMENT)
                                        .eq(DocumentFolder::getDepartmentId, user.getDepartmentId())))
                .orderByAsc(DocumentFolder::getSort)
                .orderByAsc(DocumentFolder::getName);
        return folderMapper.selectList(wrapper).stream().map(documentConverter::toFolderVO).toList();
    }

    @Override
    @Transactional
    public UUID createFolder(DocumentFolderSaveFrom from) {
        var user = securityContextAccessor.currentUser();
        if (user == null || user.getDepartmentId() == null) {
            throw new DataSaveException("当前用户组织信息不可用");
        }
        var entity = new DocumentFolder();
        entity.setPid(from.getPid());
        entity.setName(from.getName().trim());
        entity.setVisibility(normalizeVisibility(from.getVisibility()));
        entity.setSort(from.getSort() == null ? Integer.valueOf(0) : from.getSort());
        entity.setDepartmentId(user.getDepartmentId());
        if (folderMapper.insert(entity) != 1) {
            throw new DataSaveException("保存文档目录失败");
        }
        return entity.getId();
    }

    @Override
    @Transactional
    public void restoreVersion(UUID id, UUID versionId) {
        var document = requireOwner(id);
        var version = versionMapper.selectOne(
                new LambdaQueryWrapper<DocumentVersion>().eq(DocumentVersion::getId, versionId).eq(DocumentVersion::getDocumentId, document.getId()));
        if (version == null) {
            throw new DataNotExistException("文档版本不存在");
        }
        versionMapper.update(null, new LambdaUpdateWrapper<DocumentVersion>().eq(DocumentVersion::getDocumentId, document.getId())
                .eq(DocumentVersion::getCurrentVersion, true)
                .set(DocumentVersion::getCurrentVersion, false));
        version.setCurrentVersion(true);
        if (versionMapper.updateById(version) != 1) {
            throw new DataSaveException("恢复文档版本失败");
        }
    }

    /**
     * 查询或获取目标数据（{@code currentVersion}）。
     */
    private DocumentVersion currentVersion(UUID id) {
        return versionMapper.selectOne(new LambdaQueryWrapper<DocumentVersion>().eq(DocumentVersion::getDocumentId, id)
                .eq(DocumentVersion::getCurrentVersion, true)
                .last("limit 1"));
    }

    /**
     * 校验并确保数据满足当前约束（{@code require}）。
     */
    private Document require(UUID id) {
        var entity = getById(id);
        if (entity == null) {
            throw new DataNotExistException("文档不存在: " + id);
        }
        return entity;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireAccessible}）。
     */
    private Document requireAccessible(UUID id) {
        var entity = require(id);
        var user = securityContextAccessor.currentUser();
        if (user == null
                || user.getDepartmentId() == null
                || (!VISIBILITY_PUBLIC.equals(entity.getVisibility())
                        && !(VISIBILITY_DEPARTMENT.equals(entity.getVisibility())
                                && Objects.equals(entity.getDepartmentId(), user.getDepartmentId()))
                        && !Objects.equals(entity.getOwnerId(), user.getId()))) {
            throw new DataNotExistException("文档不存在或无权访问");
        }
        return entity;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireOwner}）。
     */
    private Document requireOwner(UUID id) {
        var entity = require(id);
        var user = securityContextAccessor.currentUser();
        if (user == null || !Objects.equals(entity.getOwnerId(), user.getId())) {
            throw new DataNotExistException("文档不存在或无权操作");
        }
        return entity;
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeVisibility}）。
     */
    private String normalizeVisibility(String value) {
        var normalized = StringUtils.hasText(value) ? value.trim().toUpperCase() : VISIBILITY_DEPARTMENT;
        if (!List.of(VISIBILITY_PUBLIC, VISIBILITY_DEPARTMENT, VISIBILITY_PRIVATE).contains(normalized)) {
            throw new DataSaveException("文档可见范围不合法");
        }
        return normalized;
    }

    /**
     * 更新或推进目标状态（{@code sendPublishNotification}）。
     */
    private void sendPublishNotification(Document document) {
        try {
            var wrapper = new LambdaQueryWrapper<User>();
            if (VISIBILITY_DEPARTMENT.equals(document.getVisibility())) {
                wrapper.eq(User::getDepartmentId, document.getDepartmentId());
            } else if (VISIBILITY_PRIVATE.equals(document.getVisibility())) {
                wrapper.eq(User::getId, document.getOwnerId());
            }
            var receiverIds = userService.list(wrapper).stream().map(User::getId).toList();
            if (receiverIds.isEmpty()) {
                return;
            }
            notificationService.send(NotificationSendRequest.inApp("oa:document:" + document.getId() + ":publish",
                    NotificationPurpose.OA_NOTICE, receiverIds, NotificationTemplateCode.OA_DOCUMENT_PUBLISHED)
                    .parameter("document_title", Objects.toString(document.getTitle(), ""))
                    .parameter("summary", Objects.toString(document.getSummary(), ""))
                    .businessReference("OA_DOCUMENT", document.getId().toString())
                    .sourceModule("OA")
                    .link("/oa/document?id=" + document.getId())
                    .build());
        } catch (Exception exception) {
            log.warn("文档发布通知发送失败: documentId={}", document.getId(), exception);
        }
    }
}
