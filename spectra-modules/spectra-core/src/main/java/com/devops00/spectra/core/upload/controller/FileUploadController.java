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

package com.devops00.spectra.core.upload.controller;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.upload.javabean.from.ConfirmPartRequest;
import com.devops00.spectra.core.upload.javabean.from.CreateUploadRequest;
import com.devops00.spectra.core.upload.javabean.from.PartTargetRequest;
import com.devops00.spectra.core.upload.javabean.vo.PartTargetVO;
import com.devops00.spectra.core.upload.javabean.vo.UploadSessionVO;
import com.devops00.spectra.core.upload.service.UploadApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

/**
 * 提供文件上传相关的 HTTP 接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@RestController
@RequestMapping("/file/uploads")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("isAuthenticated()")
public class FileUploadController {

    private final UploadApplicationService uploadService;

    /**
     * 构建文件上传。
     *
     * @param request 请求参数。
     * @return 上传会话数据。
     */
    @Audit("'创建或恢复文件上传任务'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public UploadSessionVO create(@Valid @RequestBody CreateUploadRequest request) {
        return uploadService.create(request);
    }

    /**
     * 处理状态相关数据。
     *
     * @param uploadId 上传标识。
     * @return 上传会话数据。
     */
    @Audit("'查询文件上传状态'")
    @GetMapping(value = "/{uploadId}", version = "1.0.0")
    public UploadSessionVO status(@PathVariable UUID uploadId) {
        return uploadService.status(uploadId);
    }

    /**
     * 处理目标相关数据。
     *
     * @param uploadId       上传标识。
     * @param partNumber     分片编号参数。
     * @param request        请求参数。
     * @param servletRequest 请求参数。
     * @return 分片目标数据。
     */
    @Audit("'获取文件分片上传地址'")
    @PostMapping(value = "/{uploadId}/parts/{partNumber}/target", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public PartTargetVO target(@PathVariable UUID uploadId, @PathVariable int partNumber,
                               @Valid @RequestBody PartTargetRequest request, HttpServletRequest servletRequest) {
        PartTargetVO target = uploadService.target(uploadId, partNumber, request);
        target.setUrl(withContextPath(servletRequest.getContextPath(), target.getUrl()));
        return target;
    }

    /**
     * 处理上下文路径相关数据。
     */
    private String withContextPath(String contextPath, String url) {
        if (url == null
                || url.isBlank()
                || isAbsoluteUrl(url)
                || contextPath == null
                || contextPath.isBlank()
                || "/".equals(contextPath)
                || url.equals(contextPath)
                || url.startsWith(contextPath + "/")) {
            return url;
        }
        String normalizedContextPath = contextPath.endsWith("/")
                ? contextPath.substring(0, contextPath.length() - 1)
                : contextPath;
        return normalizedContextPath + (url.startsWith("/") ? url : "/" + url);
    }

    /**
     * 判断URL。
     */
    private boolean isAbsoluteUrl(String url) {
        return url.matches("^[A-Za-z][A-Za-z0-9+.-]*:.*");
    }

    /**
     * 处理内容相关数据。
     *
     * @param uploadId   上传标识。
     * @param partNumber 分片编号参数。
     * @param request    请求参数。
     * @return 包含处理结果的 HTTP 响应。
     * @throws IOException 当操作无法完成或前置条件不满足时抛出。
     */
    @Audit("'上传文件分片'")
    @PutMapping(value = "/{uploadId}/parts/{partNumber}/content", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public ResponseEntity<Void> content(@PathVariable UUID uploadId, @PathVariable int partNumber,
                                        HttpServletRequest request)
            throws IOException {
        uploadService.putPart(uploadId, partNumber, request.getInputStream(), request.getContentLengthLong());
        return ResponseEntity.noContent().build();
    }

    /**
     * 处理文件上传相关数据。
     *
     * @param uploadId   上传标识。
     * @param partNumber 分片编号参数。
     * @param request    请求参数。
     * @return 包含处理结果的 HTTP 响应。
     */
    @Audit("'确认文件分片'")
    @PostMapping(value = "/{uploadId}/parts/{partNumber}/confirm", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public ResponseEntity<Void> confirm(@PathVariable UUID uploadId, @PathVariable int partNumber,
                                        @Valid @RequestBody ConfirmPartRequest request) {
        uploadService.confirm(uploadId, partNumber, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 完成文件上传。
     *
     * @param uploadId 上传标识。
     * @return 上传会话数据。
     */
    @Audit("'完成文件上传'")
    @PostMapping(value = "/{uploadId}/complete", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public UploadSessionVO complete(@PathVariable UUID uploadId) {
        return uploadService.complete(uploadId);
    }

    /**
     * 取消文件上传。
     *
     * @param uploadId 上传标识。
     * @return 包含处理结果的 HTTP 响应。
     */
    @Audit("'取消文件上传任务'")
    @DeleteMapping(value = "/{uploadId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'file:create')")
    public ResponseEntity<Void> cancel(@PathVariable UUID uploadId) {
        uploadService.cancel(uploadId);
        return ResponseEntity.noContent().build();
    }
}
