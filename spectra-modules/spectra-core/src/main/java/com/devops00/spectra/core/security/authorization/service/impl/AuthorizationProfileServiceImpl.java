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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.authorization.entity.AuthorizationProfile;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.entity.AuthorizationProfileAssignment;
import com.devops00.spectra.core.security.authorization.entity.AuthorizationProfileBoundary;
import com.devops00.spectra.core.security.authorization.entity.Permission;
import com.devops00.spectra.core.security.authorization.entity.RoleGrantablePermission;
import com.devops00.spectra.core.security.authorization.entity.RolePermission;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationProfileAssignmentFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationProfileBoundaryFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationProfileSaveFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationProfileScopeFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileAssignmentVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileBoundaryVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileScopeVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileVO;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.service.AuthorizationProfileService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.security.base.authorization.ScopeMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 授权方案管理实现。
 * <p>
 * 方案保存时立即校验当前 Role、Permission、Scope 和部门编码；应用到用户时仍必须重新通过
 * RoleAssignment Preview/Apply 和 Grant Boundary 校验。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationProfileServiceImpl extends BaseServiceImpl<AuthorizationProfileMapper, AuthorizationProfile>
        implements
            AuthorizationProfileService {

    private final AuthorizationProfileAssignmentMapper assignmentMapper;

    private final AuthorizationProfileBoundaryMapper boundaryMapper;

    private final SecurityRoleMapper roleMapper;

    private final RolePermissionMapper rolePermissionMapper;

    private final RoleGrantablePermissionMapper roleGrantablePermissionMapper;

    private final PermissionMapper permissionMapper;

    private final DepartmentService departmentService;

    @Override
    @Transactional
    public void created(AuthorizationProfileSaveFrom params) {
        validate(params);
        if (getOne(new LambdaQueryWrapper<AuthorizationProfile>().eq(AuthorizationProfile::getCode, params.getCode())) != null) {
            throw new DataException("授权方案编码已存在");
        }
        var profile = new AuthorizationProfile();
        profile.setCode(params.getCode().trim());
        profile.setName(params.getName().trim());
        profile.setDescription(trimToNull(params.getDescription()));
        profile.setState(SecurityAuthorizationState.ACTIVE.name());
        if (!save(profile)) {
            throw new DataException("创建授权方案失败");
        }
        replaceAssignments(profile.getId(), params.getAssignments());
        log.info("创建授权方案成功: id={}, code={}", profile.getId(), profile.getCode());
    }

    @Override
    @Transactional
    public void modify(UUID id, AuthorizationProfileSaveFrom params) {
        if (id == null || params == null || !id.equals(params.getId())) {
            throw new DataException("授权方案 ID 不匹配");
        }
        validate(params);
        var profile = getById(id);
        if (profile == null) {
            throw new DataNotExistException("授权方案不存在");
        }
        if (!profile.getCode().equals(params.getCode().trim())) {
            throw new DataException("授权方案编码不可修改");
        }
        var currentVersion = profile.getVersion() == null ? 0L : profile.getVersion();
        if (!Long.valueOf(currentVersion).equals(params.getExpectedVersion())) {
            throw new DataException("授权方案版本已变化，请刷新后重试");
        }
        var update = new LambdaUpdateWrapper<AuthorizationProfile>()
                .eq(AuthorizationProfile::getId, id)
                .eq(AuthorizationProfile::getVersion, currentVersion)
                .set(AuthorizationProfile::getName, params.getName().trim())
                .set(AuthorizationProfile::getDescription, trimToNull(params.getDescription()))
                .set(AuthorizationProfile::getVersion, currentVersion + 1L);
        if (!update(update)) {
            throw new DataException("授权方案版本并发变化，修改已拒绝");
        }
        replaceAssignments(id, params.getAssignments());
        log.info("修改授权方案成功: id={}, version={}", id, currentVersion + 1L);
    }

    @Override
    @Transactional
    public void enable(UUID id) {
        var profile = getById(id);
        if (profile == null) {
            throw new DataNotExistException("授权方案不存在");
        }
        if (SecurityAuthorizationState.ACTIVE.name().equals(profile.getState())) {
            return;
        }
        var currentVersion = profile.getVersion() == null ? 0L : profile.getVersion();
        var update = new LambdaUpdateWrapper<AuthorizationProfile>()
                .eq(AuthorizationProfile::getId, id)
                .eq(AuthorizationProfile::getVersion, currentVersion)
                .set(AuthorizationProfile::getState, SecurityAuthorizationState.ACTIVE.name())
                .set(AuthorizationProfile::getVersion, currentVersion + 1L);
        if (!update(update)) {
            throw new DataException("授权方案版本并发变化，启用已拒绝");
        }
        log.info("启用授权方案成功: id={}", id);
    }

    @Override
    @Transactional
    public void disable(UUID id) {
        var profile = getById(id);
        if (profile == null) {
            throw new DataNotExistException("授权方案不存在");
        }
        if (!SecurityAuthorizationState.ACTIVE.name().equals(profile.getState())) {
            return;
        }
        var currentVersion = profile.getVersion() == null ? 0L : profile.getVersion();
        var update = new LambdaUpdateWrapper<AuthorizationProfile>()
                .eq(AuthorizationProfile::getId, id)
                .eq(AuthorizationProfile::getVersion, currentVersion)
                .set(AuthorizationProfile::getState, SecurityAuthorizationState.DISABLED.name())
                .set(AuthorizationProfile::getVersion, currentVersion + 1L);
        if (!update(update)) {
            throw new DataException("授权方案版本并发变化，停用已拒绝");
        }
        log.info("停用授权方案成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        var profile = getById(id);
        if (profile == null) {
            throw new DataNotExistException("授权方案不存在");
        }
        var assignments = assignmentMapper.selectList(new LambdaQueryWrapper<AuthorizationProfileAssignment>()
                .eq(AuthorizationProfileAssignment::getProfileId, id));
        if (!assignments.isEmpty()) {
            var assignmentIds = assignments.stream().map(AuthorizationProfileAssignment::getId).toList();
            boundaryMapper.delete(new LambdaQueryWrapper<AuthorizationProfileBoundary>()
                    .in(AuthorizationProfileBoundary::getProfileAssignmentId, assignmentIds));
            assignmentMapper.delete(new LambdaQueryWrapper<AuthorizationProfileAssignment>()
                    .in(AuthorizationProfileAssignment::getId, assignmentIds));
        }
        if (!removeById(id)) {
            throw new DataException("删除授权方案失败");
        }
        log.info("删除授权方案成功: id={}, code={}", id, profile.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorizationProfileVO> all() {
        return list(new LambdaQueryWrapper<AuthorizationProfile>()
                .orderByAsc(AuthorizationProfile::getName)
                .orderByAsc(AuthorizationProfile::getCode))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorizationProfileVO detail(UUID id) {
        var profile = getById(id);
        if (profile == null) {
            throw new DataNotExistException("授权方案不存在");
        }
        return toVO(profile);
    }

    private void validate(AuthorizationProfileSaveFrom params) {
        if (params == null || params.getAssignments() == null || params.getAssignments().isEmpty()) {
            throw new DataException("授权方案至少需要一个 Role");
        }
        var roleCodes = new HashSet<String>();
        for (var assignment : params.getAssignments()) {
            var roleCode = assignment.getRoleCode() == null ? "" : assignment.getRoleCode().trim();
            if (!roleCodes.add(roleCode)) {
                throw new DataException("授权方案不能重复配置同一个 Role");
            }
            var role = roleMapper.selectOne(new LambdaQueryWrapper<SecurityRole>()
                    .eq(SecurityRole::getCode, roleCode));
            if (role == null || !SecurityAuthorizationState.ACTIVE.name().equals(role.getState())) {
                throw new DataNotExistException("Role 不存在或已停用: " + roleCode);
            }
            var currentRoleVersion = role.getVersion() == null ? 0L : role.getVersion();
            if (!Long.valueOf(currentRoleVersion).equals(assignment.getRoleVersion())) {
                throw new DataException("Role version 已变化，请刷新角色授权后重试: " + roleCode);
            }
            if ("DEV_OPS".equals(role.getRoleKind())) {
                throw new DataException("DEV_OPS Role 不能通过普通授权方案配置");
            }
            validateBoundaries(role, assignment.getBoundaries());
        }
    }

    private void validateBoundaries(SecurityRole role, List<AuthorizationProfileBoundaryFrom> boundaries) {
        if (boundaries == null || boundaries.isEmpty()) {
            throw new DataException("授权方案至少需要一个 Permission Boundary: " + role.getCode());
        }
        var rolePermissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, role.getId()))
                .stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toSet());
        var grantablePermissionIds = roleGrantablePermissionMapper.selectList(
                new LambdaQueryWrapper<RoleGrantablePermission>().eq(RoleGrantablePermission::getRoleId, role.getId()))
                .stream()
                .map(RoleGrantablePermission::getPermissionId)
                .collect(Collectors.toSet());
        var codes = boundaries.stream().map(item -> item.getPermission().trim()).toList();
        var permissions = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .in(Permission::getCode, codes))
                .stream()
                .collect(Collectors.toMap(Permission::getCode, item -> item));
        var seen = new HashSet<String>();
        for (var boundary : boundaries) {
            var permissionCode = boundary.getPermission().trim();
            if (!seen.add(permissionCode)) {
                throw new DataException("授权方案不能重复配置 Permission: " + permissionCode);
            }
            var permission = permissions.get(permissionCode);
            if (permission == null || !SecurityAuthorizationState.ACTIVE.name().equals(permission.getState())) {
                throw new DataNotExistException("Permission 不存在或已停用: " + permissionCode);
            }
            if (!rolePermissionIds.contains(permission.getId())) {
                throw new DataException("Role 未声明 Permission: " + permissionCode);
            }
            validateScope(boundary.getAccess(), permission, "Access Boundary");
            if (boundary.getGrant() != null) {
                if (!grantablePermissionIds.contains(permission.getId())) {
                    throw new DataException("Role 未声明 GrantablePermission: " + permissionCode);
                }
                validateScope(boundary.getGrant(), permission, "Grant Boundary");
            }
        }
    }

    private void validateScope(AuthorizationProfileScopeFrom scope, Permission permission, String label) {
        if (scope == null || scope.getMode() == null) {
            throw new DataException(label + "不能为空: " + permission.getCode());
        }
        var allowedModes = parseAllowedModes(permission.getAllowedScopeModes());
        if (!allowedModes.contains(scope.getMode())) {
            throw new DataException(label + "模式不符合 Permission 约束: " + permission.getCode());
        }
        var departmentCodes = normalizeDepartmentCodes(scope.getDepartmentCodes());
        if (scope.getMode() == ScopeMode.RULES && departmentCodes.isEmpty()) {
            throw new DataException("RULES Scope 必须配置部门编码: " + permission.getCode());
        }
        if (scope.getMode() != ScopeMode.RULES && !departmentCodes.isEmpty()) {
            throw new DataException("非 RULES Scope 不能配置部门编码: " + permission.getCode());
        }
        if (!departmentCodes.isEmpty()) {
            var count = departmentService.count(new LambdaQueryWrapper<Department>()
                    .in(Department::getCode, departmentCodes));
            if (count != departmentCodes.size()) {
                throw new DataNotExistException("授权方案引用了不存在的部门编码");
            }
        }
    }

    private Set<ScopeMode> parseAllowedModes(String value) {
        if (value == null || value.isBlank()) {
            return Set.of(ScopeMode.NONE);
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(item -> item.toUpperCase(Locale.ROOT))
                .map(ScopeMode::valueOf)
                .collect(Collectors.toSet());
    }

    private List<String> normalizeDepartmentCodes(List<String> departmentCodes) {
        if (departmentCodes == null) {
            return List.of();
        }
        return departmentCodes.stream()
                .map(code -> code == null ? "" : code.trim())
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();
    }

    private void replaceAssignments(UUID profileId, List<AuthorizationProfileAssignmentFrom> sources) {
        var oldAssignments = assignmentMapper.selectList(new LambdaQueryWrapper<AuthorizationProfileAssignment>()
                .eq(AuthorizationProfileAssignment::getProfileId, profileId));
        if (!oldAssignments.isEmpty()) {
            var oldIds = oldAssignments.stream().map(AuthorizationProfileAssignment::getId).toList();
            boundaryMapper.delete(new LambdaQueryWrapper<AuthorizationProfileBoundary>()
                    .in(AuthorizationProfileBoundary::getProfileAssignmentId, oldIds));
            assignmentMapper.delete(new LambdaQueryWrapper<AuthorizationProfileAssignment>()
                    .in(AuthorizationProfileAssignment::getId, oldIds));
        }
        for (var source : sources) {
            var assignment = new AuthorizationProfileAssignment();
            assignment.setProfileId(profileId);
            assignment.setRoleCode(source.getRoleCode().trim());
            assignment.setRoleVersion(source.getRoleVersion());
            if (assignmentMapper.insert(assignment) != 1) {
                throw new DataException("保存授权方案 Role 配置失败");
            }
            for (var sourceBoundary : source.getBoundaries()) {
                var boundary = new AuthorizationProfileBoundary();
                boundary.setProfileAssignmentId(assignment.getId());
                boundary.setPermissionCode(sourceBoundary.getPermission().trim());
                boundary.setAccessScope(toScopeMap(sourceBoundary.getAccess()));
                boundary.setGrantScope(sourceBoundary.getGrant() == null ? null : toScopeMap(sourceBoundary.getGrant()));
                if (boundaryMapper.insert(boundary) != 1) {
                    throw new DataException("保存授权方案 Permission Boundary 失败");
                }
            }
        }
    }

    private Map<String, Object> toScopeMap(AuthorizationProfileScopeFrom source) {
        var result = new LinkedHashMap<String, Object>();
        result.put("mode", source.getMode().name());
        if (source.getResourceCode() != null && !source.getResourceCode().isBlank()) {
            result.put("resource_code", source.getResourceCode().trim());
        }
        result.put("department_codes", normalizeDepartmentCodes(source.getDepartmentCodes()));
        result.put("include_descendants", source.isIncludeDescendants());
        return result;
    }

    private AuthorizationProfileVO toVO(AuthorizationProfile profile) {
        var result = new AuthorizationProfileVO();
        result.setId(profile.getId());
        result.setCode(profile.getCode());
        result.setName(profile.getName());
        result.setDescription(profile.getDescription());
        result.setState(profile.getState());
        result.setVersion(profile.getVersion() == null ? 0L : profile.getVersion());
        result.setAssignments(assignmentMapper.selectList(new LambdaQueryWrapper<AuthorizationProfileAssignment>()
                .eq(AuthorizationProfileAssignment::getProfileId, profile.getId()))
                .stream()
                .map(this::toAssignmentVO)
                .toList());
        return result;
    }

    private AuthorizationProfileAssignmentVO toAssignmentVO(AuthorizationProfileAssignment assignment) {
        var result = new AuthorizationProfileAssignmentVO();
        result.setRoleCode(assignment.getRoleCode());
        result.setRoleVersion(assignment.getRoleVersion());
        result.setBoundaries(boundaryMapper.selectList(new LambdaQueryWrapper<AuthorizationProfileBoundary>()
                .eq(AuthorizationProfileBoundary::getProfileAssignmentId, assignment.getId()))
                .stream()
                .map(this::toBoundaryVO)
                .toList());
        return result;
    }

    private AuthorizationProfileBoundaryVO toBoundaryVO(AuthorizationProfileBoundary boundary) {
        var result = new AuthorizationProfileBoundaryVO();
        result.setPermission(boundary.getPermissionCode());
        result.setAccess(toScopeVO(boundary.getAccessScope()));
        result.setGrant(boundary.getGrantScope() == null ? null : toScopeVO(boundary.getGrantScope()));
        return result;
    }

    private AuthorizationProfileScopeVO toScopeVO(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        var result = new AuthorizationProfileScopeVO();
        try {
            result.setMode(ScopeMode.valueOf(String.valueOf(source.get("mode"))));
        } catch (IllegalArgumentException exception) {
            throw new DataException("授权方案 Scope 模式无效");
        }
        result.setResourceCode(source.get("resource_code") == null ? null : String.valueOf(source.get("resource_code")));
        var departmentCodes = source.get("department_codes");
        if (departmentCodes instanceof List<?> values) {
            result.setDepartmentCodes(values.stream().map(String::valueOf).toList());
        }
        result.setIncludeDescendants(Boolean.TRUE.equals(source.get("include_descendants")));
        return result;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
