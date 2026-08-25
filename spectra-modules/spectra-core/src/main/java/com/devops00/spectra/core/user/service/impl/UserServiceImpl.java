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

package com.devops00.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.security.authorization.domain.UserAuthorizationStatusCalculator;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationAssignmentView;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.converter.UserConverter;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.ChangePasswordFrom;
import com.devops00.spectra.core.user.javabean.from.UserPageFrom;
import com.devops00.spectra.core.user.javabean.from.UserProfileFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.vo.UserPageVO;
import com.devops00.spectra.core.user.javabean.vo.UserProfileVO;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;
import com.devops00.spectra.core.user.javabean.vo.UserCreatedVO;
import com.devops00.spectra.core.user.javabean.vo.UserPasswordResetVO;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.framework.assembler.NameFillExecutor;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.change.SecurityChangeExecutor;
import com.devops00.spectra.security.base.change.SecuritySessionQueryPort;
import com.devops00.spectra.security.base.change.SecuritySessionRevocationPort;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import com.devops00.spectra.security.base.policy.SecurityPasswordPolicyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户service层-实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Duration TEMPORARY_PASSWORD_VALIDITY = Duration.ofHours(24);

    private final UserConverter userConverter;

    private final AuthorizationAssignmentQueryService authorizationAssignmentQueryService;

    private final RoleAssignmentMapper roleAssignmentMapper;

    private final DepartmentService departmentService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationIdentityService authenticationIdentityService;

    private final UserContactService userContactService;

    private final PasswordCredentialService passwordCredentialService;

    private final NameFillExecutor fillExecutor;

    private final SecurityChangeExecutor securityChangeExecutor;

    private final SecurityAuditWriter securityAuditWriter;

    private final SecurityContextAccessor securityContextAccessor;

    private final SecuritySessionQueryPort securitySessionQueryPort;

    private final SecuritySessionRevocationPort securitySessionRevocationPort;

    private final SecurityPasswordPolicyProvider securityPasswordPolicyProvider;

    private final TimeMapper timeMapper;

    @Override
    public User getByUsername(String username) {
        if (StrUtils.isBlank(username)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<User>()
                .apply("lower(btrim(username)) = lower({0})", username.trim())
                .last("LIMIT 1"));
    }

    @Override
    @Transactional
    public UserCreatedVO create(UserSaveFrom params) {
        if (params.getStatus() != UserStatus.ACTIVE) {
            throw new DataException("新用户必须以 ACTIVE 状态创建");
        }
        params.setUsername(params.getUsername().trim());
        var entity = userConverter.toEntity(params);
        if (!this.save(entity)) {
            throw new DataSaveException("保存用户信息异常");
        }
        // 目标认证模型将身份标识和密码凭证拆分保存；临时密码只保存其哈希。
        authenticationIdentityService.createPasswordIdentity(entity.getId(), entity.getUsername());
        syncProvisionedContacts(entity.getId(), params);
        passwordCredentialService.createOrReplace(entity.getId(), passwordEncoder.encode(generateTemporaryPassword()), true);
        appendAudit("USER_CREATED", entity.getId(), Map.of(), Map.of("status", entity.getStatus().getCode()), null);
        return new UserCreatedVO(entity.getId(), entity.getRealName());
    }

    @Override
    @Transactional
    public void modify(UserSaveFrom params) {
        params.setUsername(params.getUsername().trim());
        var entity = this.getById(params.getId());
        if (null == entity) {
            throw new DataNotExistException("用户不存在");
        }
        if (params.getStatus() != entity.getStatus()) {
            throw new DataException("用户生命周期状态必须通过专用状态接口变更");
        }
        // 默认账号是否需要修改
        boolean defaultAccountUpdateFlag = params.getUsername().equals(entity.getUsername());
        userConverter.updateUser(params, entity);
        if (this.baseMapper.updateById(entity) == 0) {
            throw new EntityUpdateException("更新用户发生错误");
        }

        // 默认密码身份处理
        if (!defaultAccountUpdateFlag) {
            authenticationIdentityService.updatePasswordIdentifier(entity.getId(), entity.getUsername());
        }

    }

    @Override
    @Transactional
    public UserPasswordResetVO passwordResetById(UUID uid) {
        var user = this.getById(uid);
        if (user == null) {
            throw new DataNotExistException("用户不存在");
        }
        var credential = passwordCredentialService.getByUserId(user.getId());
        if (credential == null) {
            throw new DataNotExistException("密码凭证不存在");
        }
        String temporaryPassword = generateTemporaryPassword();
        Instant expiresAt = Instant.now().plus(TEMPORARY_PASSWORD_VALIDITY);
        passwordCredentialService.updatePassword(user.getId(), passwordEncoder.encode(temporaryPassword), true, expiresAt);
        appendAudit("PASSWORD_RESET", uid, Map.of(),
                Map.of("mustChange", true, "expiresAt", expiresAt.toString()), "管理员重置密码");
        // 密码凭证变化后，所有设备必须重新认证。
        securitySessionRevocationPort.revokeUserSessions(uid);
        return new UserPasswordResetVO(temporaryPassword, timeMapper.toLocalDateTime(expiresAt), true);
    }

    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) throws IllegalAccessException {
        // 条件构建
        var wrapper = new LambdaQueryWrapper<User>().like(StrUtils.isNotBlank(params.getRealName()), User::getRealName, params.getRealName())
                .like(StrUtils.isNotBlank(params.getEmployeeNo()), User::getEmployeeNo, params.getEmployeeNo())
                .like(StrUtils.isNotBlank(params.getUsername()), User::getUsername, params.getUsername())
                .in(params.getDepartmentId() != null, User::getDepartmentId, departmentService.getSelfAndDescendantIds(params.getDepartmentId()))
                .eq(params.getStatus() != null, User::getStatus, params.getStatus());

        var db = this.page(page.toPage(), wrapper);
        var result = userConverter.toVOPage(db);

        // 字段填充
        fillExecutor.fill(result.getRecords());

        // 扩展字段补充
        result.getRecords().forEach(this::fillAuthorization);
        // 响应
        return result;
    }

    @Override
    public UserPageVO detail(UUID userId) throws IllegalAccessException {
        var user = this.getById(userId);
        if (user == null) {
            throw new DataNotExistException("用户不存在");
        }
        var result = userConverter.toVO(user);
        fillExecutor.fill(List.of(result));
        fillAuthorization(result);
        return result;
    }

    @Override
    public List<UserOnlineVO> online(PageFrom page) {
        return securitySessionQueryPort.listOnlineUsers();
    }

    @Override
    public UserProfileVO getProfile(UUID userId) {
        var user = this.getById(userId);
        if (user == null) {
            throw new DataNotExistException("用户不存在");
        }
        var vo = userConverter.toProfileVO(user);
        // 填充部门名称
        if (user.getDepartmentId() != null) {
            var dept = departmentService.getById(user.getDepartmentId());
            if (dept != null) {
                vo.setDepartmentName(dept.getName());
            }
        }
        // 填充角色列表
        vo.setRoles(targetRoles(userId));
        return vo;
    }

    @Override
    @Transactional
    public void updateProfile(UUID userId, UserProfileFrom params) {
        var user = this.getById(userId);
        if (user == null) {
            throw new DataNotExistException("用户不存在");
        }
        userConverter.updateProfile(params, user);
        if (this.baseMapper.updateById(user) == 0) {
            throw new EntityUpdateException("更新用户信息失败");
        }
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordFrom params) {
        // 1. 获取用户
        var user = this.getById(userId);
        if (user == null) {
            throw new DataNotExistException("用户不存在");
        }

        // 2. 获取用户的默认账号
        var credential = passwordCredentialService.getByUserId(userId);
        if (credential == null) {
            throw new DataNotExistException("密码凭证不存在");
        }

        // 3. 验证旧密码
        if (!passwordEncoder.matches(params.getOldPassword(), credential.getPasswordHash())) {
            throw new SpectraException("旧密码错误");
        }

        // 4. 验证新密码和确认密码是否一致
        if (!params.getNewPassword().equals(params.getVerifyPassword())) {
            throw new SpectraException("两次输入的新密码不一致");
        }

        // 5. 验证新密码不能与旧密码相同
        if (passwordEncoder.matches(params.getNewPassword(), credential.getPasswordHash())) {
            throw new SpectraException("新密码不能与旧密码相同");
        }

        securityPasswordPolicyProvider.current().assertAccepts(params.getNewPassword());

        // 6. 加密新密码并更新
        passwordCredentialService.updatePassword(userId, passwordEncoder.encode(params.getNewPassword()), false, null);
        appendAudit("PASSWORD_CHANGED", userId, Map.of(), Map.of("mustChange", false), "用户修改密码");
        // 密码变化包括当前设备在内全部 Session 失效。
        securitySessionRevocationPort.revokeUserSessions(userId);
        log.info("用户 {} 修改密码成功", userId);
    }

    @Override
    @Transactional
    public void changeStatus(UUID userId, UserStatus target, String reason) {
        if (target == null) {
            throw new DataException("目标用户状态不能为空");
        }
        var current = this.getById(userId);
        if (current == null) {
            throw new DataNotExistException("用户不存在");
        }
        var previous = current.getStatus();
        if (previous == null) {
            throw new DataException("用户状态缺失，拒绝执行生命周期变更");
        }
        previous.assertTransitionTo(target);
        if (previous == target) {
            return;
        }

        var event = new SecurityAuditEvent(
                null,
                lifecycleEventType(previous, target),
                currentOperatorId(),
                userId,
                null,
                null,
                null,
                Map.of("status", previous.getCode()),
                Map.of("status", target.getCode()),
                reason,
                null,
                AuditResult.STARTED,
                null);

        securityChangeExecutor.execute(event, () -> {
            current.setStatus(target);
            current.setStatusReason(StrUtils.isBlank(reason) ? null : reason.trim());
            current.setDepartedAt(target == UserStatus.DEPARTED ? Instant.now() : null);
            current.setSecurityVersion((current.getSecurityVersion() == null ? 0L : current.getSecurityVersion()) + 1L);
            if (this.baseMapper.updateById(current) == 0) {
                throw new EntityUpdateException("更新用户生命周期状态失败");
            }
            // 离职后的旧 Assignment 不得在重新入职时自动恢复；REVOKED 历史记录保留在目标模型。
            if (target == UserStatus.DEPARTED) {
                revokeActiveAssignments(userId);
            }
            // Redis/Session 核心依赖不可用时撤销端口会 fail-closed，事务随之回滚。
            securitySessionRevocationPort.revokeUserSessions(userId);
            return Boolean.TRUE;
        });
    }

    /**
     * 处理内部业务逻辑（{@code lifecycleEventType}）。
     */
    private String lifecycleEventType(UserStatus previous, UserStatus target) {
        if (target == UserStatus.LOCKED) {
            return "ACCOUNT_LOCKED";
        }
        if (previous == UserStatus.LOCKED && target == UserStatus.ACTIVE) {
            return "ACCOUNT_UNLOCKED";
        }
        return switch (target) {
            case ACTIVE -> previous == UserStatus.DEPARTED ? "USER_REINSTATED" : "USER_ENABLED";
            case DISABLED -> "USER_DISABLED";
            case DEPARTED -> "USER_DEPARTED";
            case LOCKED -> "ACCOUNT_LOCKED";
        };
    }

    /**
     * 更新或推进目标状态（{@code appendAudit}）。
     */
    private void appendAudit(String eventType, UUID targetId, Map<String, Object> before,
                             Map<String, Object> after, String reason) {
        securityAuditWriter.append(new SecurityAuditEvent(null, eventType, currentOperatorId(), targetId,
                null, null, null, before, after, reason, null, AuditResult.SUCCEEDED, null));
    }

    /**
     * 更新或推进目标状态（{@code revokeActiveAssignments}）。
     */
    private void revokeActiveAssignments(UUID userId) {
        var assignments = roleAssignmentMapper.selectList(new LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getUserId, userId)
                .eq(RoleAssignment::getState, SecurityAuthorizationState.ACTIVE.name()));
        var revokedAt = Instant.now();
        for (var assignment : assignments) {
            assignment.setState(SecurityAuthorizationState.REVOKED.name());
            assignment.setValidUntil(revokedAt);
            assignment.setVersion((assignment.getVersion() == null ? 0L : assignment.getVersion()) + 1L);
            if (roleAssignmentMapper.updateById(assignment) != 1) {
                throw new EntityUpdateException("撤销用户 RoleAssignment 失败");
            }
        }
    }

    /**
     * 处理内部业务逻辑（{@code targetRoles}）。
     */
    private List<RoleVO> targetRoles(UUID userId) {
        return targetRoles(authorizationAssignmentQueryService.findByUserId(userId));
    }

    /** 可信用户创建/导入流程可同时登记已验证联系方式。 */
    private void syncProvisionedContacts(UUID userId, UserSaveFrom params) {
        if (StrUtils.isNotBlank(params.getPhone())) {
            userContactService.upsertVerified(userId, UserContactService.PHONE, params.getPhone());
            authenticationIdentityService.createIdentity(userId, "SMS", params.getPhone());
        }
        if (StrUtils.isNotBlank(params.getEmail())) {
            userContactService.upsertVerified(userId, UserContactService.EMAIL, params.getEmail());
            authenticationIdentityService.createIdentity(userId, "EMAIL", params.getEmail());
        }
    }

    /**
     * 处理内部业务逻辑（{@code fillAuthorization}）。
     */
    private void fillAuthorization(UserPageVO vo) {
        var assignments = authorizationAssignmentQueryService.findByUserId(vo.getId());
        vo.setRoles(targetRoles(assignments));
        vo.setAuthorizationStatus(UserAuthorizationStatusCalculator.calculate(
                assignments, timeMapper.toLocalDateTime(Instant.now())));
    }

    /**
     * 处理内部业务逻辑（{@code targetRoles}）。
     */
    private List<RoleVO> targetRoles(List<AuthorizationAssignmentView> assignments) {
        return assignments
                .stream()
                .filter(assignment -> SecurityAuthorizationState.ACTIVE.name().equals(assignment.state()))
                .collect(Collectors.toMap(
                        assignment -> assignment.roleId(),
                        assignment -> assignment,
                        (first, ignored) -> first,
                        LinkedHashMap::new))
                .values()
                .stream()
                .map(assignment -> {
                    var role = new RoleVO();
                    role.setId(assignment.roleId());
                    role.setName(assignment.roleName());
                    role.setCode(assignment.roleCode());
                    role.setState(true);
                    role.setBuiltin(Boolean.TRUE.equals(assignment.roleSystemManaged()));
                    role.setAuthorityLevel(null);
                    role.setVersion(assignment.roleVersion());
                    role.setRoleKind(assignment.roleKind());
                    return role;
                })
                .toList();
    }

    /**
     * 查询或获取目标数据（{@code currentOperatorId}）。
     */
    private UUID currentOperatorId() {
        var currentUser = securityContextAccessor.currentUser();
        return currentUser == null ? null : currentUser.getId();
    }

    /**
     * 生成仅用于占位的随机凭证，避免所有新账号共享一个可猜测的默认密码。
     *
     * @return 随机临时凭证
     */
    private String generateTemporaryPassword() {
        var bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
