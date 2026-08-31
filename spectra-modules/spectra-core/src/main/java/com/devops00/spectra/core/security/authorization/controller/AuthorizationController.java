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

package com.devops00.spectra.core.security.authorization.controller;

import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationCreateApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationChangeApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.OrganizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.RoleAuthorizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.RoleAuthorizationStateVO;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.security.authorization.service.OrganizationChangeService;
import com.devops00.spectra.core.security.authorization.service.RoleAuthorizationChangeService;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationAssignmentView;
import com.devops00.spectra.common.audit.Audit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 目标授权模型查询与 Grant Boundary Preview/Apply 入口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@RestController
@Slf4j
@RequestMapping("/security/authorization")
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationAssignmentQueryService queryService;

    private final AuthorizationAssignmentChangeService changeService;

    private final RoleAuthorizationChangeService roleChangeService;

    private final OrganizationChangeService organizationChangeService;

    /**
     * 查询或获取目标数据（{@code organizationVersion}）。
     */
    @Audit("'查询组织结构安全版本'")
    @GetMapping(value = "/departments/organization-version", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'department:read')")
    public long organizationVersion() {
        return organizationChangeService.currentOrganizationVersion();
    }

    /**
     * 处理内部业务逻辑（{@code departmentCreatePreview}）。
     */
    @Audit("'预览新增部门影响'")
    @PostMapping(value = "/departments/impact-preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'department:create')")
    public OrganizationChangePreviewVO departmentCreatePreview(
                                                               @Validated @RequestBody OrganizationChangeFrom from) {
        log.debug("预览新增部门影响: expectedOrganizationVersion={}", from.getExpectedOrganizationVersion());
        return organizationChangeService.previewCreate(from);
    }

    /**
     * 处理内部业务逻辑（{@code departmentCreateApply}）。
     */
    @Audit("'提交新增部门变更'")
    @PostMapping(value = "/departments/impact-apply", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'department:create')")
    public void departmentCreateApply(@Validated @RequestBody OrganizationCreateApplyFrom from) {
        log.debug("提交新增部门变更: expectedOrganizationVersion={}", from.getExpectedOrganizationVersion());
        organizationChangeService.applyCreate(from);
    }

    /**
     * 处理内部业务逻辑（{@code roleState}）。
     */
    @Audit("'查询 Role 授权能力'")
    @GetMapping(value = "/roles/{roleId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'role:read')")
    public RoleAuthorizationStateVO roleState(@PathVariable UUID roleId) {
        return roleChangeService.current(roleId);
    }

    /**
     * 处理内部业务逻辑（{@code departmentPreview}）。
     */
    @Audit("'预览部门编辑与移动影响'")
    @PostMapping(value = "/departments/{departmentId}/impact-preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'department:update')")
    public OrganizationChangePreviewVO departmentPreview(@PathVariable UUID departmentId,
                                                         @Validated @RequestBody OrganizationChangeFrom from) {
        log.debug("预览部门编辑与移动影响: departmentId={}", departmentId);
        return organizationChangeService.preview(departmentId, from);
    }

    /**
     * 处理内部业务逻辑（{@code departmentApply}）。
     */
    @Audit("'提交部门编辑与移动变更'")
    @PostMapping(value = "/departments/{departmentId}/impact-apply", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'department:update')")
    public void departmentApply(@PathVariable UUID departmentId,
                                @Validated @RequestBody OrganizationChangeApplyFrom from) {
        log.debug("提交部门编辑与移动变更: departmentId={}", departmentId);
        organizationChangeService.apply(departmentId, from);
    }

    /**
     * 处理内部业务逻辑（{@code rolePreview}）。
     */
    @Audit("'预览 Role 授权能力变更'")
    @PostMapping(value = "/roles/{roleId}/impact-preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'role:grant')")
    public RoleAuthorizationChangePreviewVO rolePreview(@PathVariable UUID roleId,
                                                        @Validated @RequestBody RoleAuthorizationChangeFrom from) {
        log.debug("预览 Role 授权能力变更: roleId={}", roleId);
        return roleChangeService.preview(roleId, from);
    }

    /**
     * 处理内部业务逻辑（{@code roleApply}）。
     */
    @Audit("'提交 Role 授权能力变更'")
    @PostMapping(value = "/roles/{roleId}/impact-apply", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'role:grant')")
    public void roleApply(@PathVariable UUID roleId,
                          @Validated @RequestBody RoleAuthorizationApplyFrom from) {
        log.debug("提交 Role 授权能力变更: roleId={}", roleId);
        roleChangeService.apply(roleId, from);
    }

    /**
     * 查询或获取目标数据（{@code assignments}）。
     */
    @Audit("'查询用户授权实例'")
    @GetMapping(value = "/users/{userId}/assignments", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'role:read')")
    public List<AuthorizationAssignmentView> assignments(@PathVariable UUID userId) {
        return queryService.findByUserId(userId);
    }

    /**
     * 处理内部业务逻辑（{@code preview}）。
     */
    @Audit("'预览用户授权实例变更'")
    @PostMapping(value = "/users/{userId}/assignments/preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'role:assign')")
    public AuthorizationChangePreviewVO preview(@PathVariable UUID userId,
                                                @Validated @RequestBody AuthorizationAssignmentChangeFrom from) {
        log.debug("预览用户授权实例变更: userId={}, assignmentId={}", userId, from.getAssignmentId());
        return changeService.preview(userId, from);
    }

    /**
     * 更新或推进目标状态（{@code apply}）。
     */
    @Audit("'提交用户授权实例变更'")
    @PostMapping(value = "/users/{userId}/assignments/apply", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'role:assign')")
    public void apply(@PathVariable UUID userId,
                      @Validated @RequestBody AuthorizationAssignmentApplyFrom from) {
        log.debug("提交用户授权实例变更: userId={}, assignmentId={}", userId, from.getAssignmentId());
        changeService.apply(userId, from);
    }
}
