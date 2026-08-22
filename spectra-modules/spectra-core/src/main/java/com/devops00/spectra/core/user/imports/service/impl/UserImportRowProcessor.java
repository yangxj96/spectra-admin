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

package com.devops00.spectra.core.user.imports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationBoundaryFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationScopeFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileBoundaryVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileScopeVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileVO;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.user.imports.entity.UserImportRow;
import com.devops00.spectra.core.user.imports.javabean.from.UserImportRowFrom;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.vo.UserCreatedVO;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.authorization.ScopeMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 单行导入事务处理器。
 * <p>
 * 每一行使用独立事务，单行授权失败时不会回滚已经成功的其他行；用户创建和该用户的授权方案应用仍处于同一行事务内。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Service
@RequiredArgsConstructor
public class UserImportRowProcessor {

    private final UserMapper userMapper;

    private final UserService userService;

    private final SecurityRoleMapper roleMapper;

    private final AuthorizationAssignmentChangeService assignmentChangeService;

    /**
     * 应用一行用户及其授权方案。
     *
     * @param row           暂存行
     * @param departmentIds 部门编码到 ID 的映射
     * @param profiles      授权方案编码到方案的映射
     * @return 行处理结果
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessResult process(UserImportRow row, boolean skipExisting, Map<String, UUID> departmentIds,
                                 Map<String, AuthorizationProfileVO> profiles) {
        var source = toSource(row.getNormalizedData());
        var existing = findExisting(source);
        if (existing != null) {
            if (skipExisting) {
                return new ProcessResult(existing.getId(), true);
            }
            throw new DataException("用户已存在: " + source.getEmployeeNo());
        }
        var departmentId = departmentIds.get(source.getDepartmentCode());
        if (departmentId == null) {
            throw new DataException("部门不存在: " + source.getDepartmentCode());
        }
        var profile = profiles.get(source.getAuthorizationProfileCode());
        if (profile == null || !"ACTIVE".equals(profile.getState())) {
            throw new DataException("授权方案不存在或已停用: " + source.getAuthorizationProfileCode());
        }
        var user = new UserSaveFrom();
        user.setEmployeeNo(source.getEmployeeNo());
        user.setRealName(source.getRealName());
        user.setPhone(source.getPhone());
        user.setEmail(source.getEmail());
        user.setLanguage(source.getLanguage());
        user.setTimezone(source.getTimezone());
        user.setDepartmentId(departmentId);
        user.setStatus(UserStatus.ACTIVE);
        UserCreatedVO created = userService.create(user);
        applyProfile(created.getId(), profile, departmentIds);
        return new ProcessResult(created.getId(), false);
    }

    private void applyProfile(UUID userId, AuthorizationProfileVO profile, Map<String, UUID> departmentIds) {
        for (var assignment : profile.getAssignments()) {
            var role = roleMapper.selectOne(new LambdaQueryWrapper<SecurityRole>()
                    .eq(SecurityRole::getCode, assignment.getRoleCode()));
            if (role == null || !"ACTIVE".equals(role.getState())) {
                throw new DataException("Role 不存在或已停用: " + assignment.getRoleCode());
            }
            if (!Long.valueOf(role.getVersion() == null ? 0L : role.getVersion()).equals(assignment.getRoleVersion())) {
                throw new DataException("Role version 已变化，请重新生成导入 Preview: " + assignment.getRoleCode());
            }
            var change = new AuthorizationAssignmentChangeFrom();
            change.setRoleId(role.getId());
            change.setExpectedVersion(0L);
            change.setBoundaries(assignment.getBoundaries()
                    .stream()
                    .map(boundary -> toBoundary(boundary, departmentIds))
                    .toList());
            var preview = assignmentChangeService.preview(userId, change);
            var apply = new AuthorizationAssignmentApplyFrom();
            apply.setAssignmentId(preview.getAssignmentId());
            apply.setRoleId(change.getRoleId());
            apply.setExpectedVersion(change.getExpectedVersion());
            apply.setBoundaries(change.getBoundaries());
            apply.setPreviewToken(preview.getPreviewToken());
            assignmentChangeService.apply(userId, apply);
        }
    }

    private AuthorizationBoundaryFrom toBoundary(AuthorizationProfileBoundaryVO source, Map<String, UUID> departmentIds) {
        var result = new AuthorizationBoundaryFrom();
        result.setPermission(source.getPermission());
        result.setAccess(toScope(source.getAccess(), departmentIds));
        result.setGrant(source.getGrant() == null ? null : toScope(source.getGrant(), departmentIds));
        return result;
    }

    private AuthorizationScopeFrom toScope(AuthorizationProfileScopeVO source, Map<String, UUID> departmentIds) {
        if (source == null || source.getMode() == null) {
            throw new DataException("授权方案 Scope 不能为空");
        }
        var result = new AuthorizationScopeFrom();
        result.setMode(source.getMode());
        result.setIncludeDescendants(source.isIncludeDescendants());
        if (source.getMode() == ScopeMode.RULES) {
            result.setDepartmentIds((source.getDepartmentCodes() == null ? List.<String>of() : source.getDepartmentCodes())
                    .stream()
                    .map(departmentIds::get)
                    .toList());
            if (result.getDepartmentIds().stream().anyMatch(id -> id == null)) {
                throw new DataException("授权方案引用了不存在的部门");
            }
        } else {
            result.setDepartmentIds(List.of());
        }
        return result;
    }

    private User findExisting(UserImportRowFrom source) {
        var byEmployeeNo = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmployeeNo, source.getEmployeeNo()));
        if (byEmployeeNo != null) {
            return byEmployeeNo;
        }
        var byEmail = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, source.getEmail()));
        if (byEmail != null) {
            return byEmail;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, source.getPhone()));
    }

    private UserImportRowFrom toSource(Map<String, Object> values) {
        var source = new UserImportRowFrom();
        source.setEmployeeNo(value(values, "employee_no"));
        source.setRealName(value(values, "real_name"));
        source.setPhone(value(values, "phone"));
        source.setEmail(value(values, "email"));
        source.setDepartmentCode(value(values, "department_code"));
        source.setLanguage(value(values, "language"));
        source.setTimezone(value(values, "timezone"));
        source.setAuthorizationProfileCode(value(values, "authorization_profile_code"));
        return source;
    }

    private String value(Map<String, Object> values, String key) {
        return values == null || values.get(key) == null ? null : String.valueOf(values.get(key));
    }

    /** 单行处理结果。 */
    public record ProcessResult(UUID userId, boolean skipped) {
    }
}
