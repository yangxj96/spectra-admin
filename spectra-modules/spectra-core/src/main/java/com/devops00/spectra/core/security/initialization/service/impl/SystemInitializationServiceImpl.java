/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.core.security.authentication.mfa.service.MfaService;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.initialization.javabean.entity.SystemState;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationCompleteFrom;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationMfaConfirmFrom;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationStartFrom;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationMfaConfirmVO;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStartVO;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStatusVO;
import com.devops00.spectra.core.security.initialization.mapper.SystemStateMapper;
import com.devops00.spectra.core.security.initialization.service.SystemInitializationService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort.MfaLoginChallenge;
import com.devops00.spectra.security.base.policy.SecurityPasswordPolicyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** PostgreSQL + Redis 的系统首次初始化实现。 */
@Service
@RequiredArgsConstructor
public class SystemInitializationServiceImpl implements SystemInitializationService {

    private static final String SYSTEM_KEY = "SYSTEM";
    private static final String UNINITIALIZED = "UNINITIALIZED";
    private static final String INITIALIZING = "INITIALIZING";
    private static final String INITIALIZED = "INITIALIZED";
    private static final String DEV_OPS_ROLE = "ROLE_DEV_OPS";
    private static final String ADVISORY_LOCK = "spectra:system-initialization";

    private final SystemStateMapper stateMapper;
    private final UserMapper userMapper;
    private final SecurityRoleMapper securityRoleMapper;
    private final RoleAssignmentMapper roleAssignmentMapper;
    private final AuthenticationIdentityService authenticationIdentityService;
    private final PasswordCredentialService passwordCredentialService;
    private final MfaService mfaService;
    private final SystemInitializationTokenManager initializationTokenManager;
    private final SecurityPasswordPolicyProvider passwordPolicyProvider;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectProvider<SecurityMfaChallengePort> challengeProvider;

    @Override
    public SystemInitializationStatusVO status() {
        SystemState state = loadState(false);
        return new SystemInitializationStatusVO(state.getState(), INITIALIZED.equals(state.getState()),
                !INITIALIZED.equals(state.getState()));
    }

    @Override
    @Transactional
    public SystemInitializationStartVO start(SystemInitializationStartFrom from, String initializationToken) {
        initializationTokenManager.assertToken(initializationToken);
        lockInitialization();
        SystemState state = loadState(true);
        if (!UNINITIALIZED.equals(state.getState())) {
            throw new IllegalStateException("系统已经初始化或已有初始化流程进行中");
        }

        String username = normalize(from.getUsername());
        passwordPolicyProvider.current().assertAccepts(from.getPassword());
        if (findExistingUser(username) != null) {
            throw new DuplicateKeyException("初始化账号已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(username);
        user.setStatus(UserStatus.LOCKED);
        user.setRealName(from.getRealName() == null || from.getRealName().isBlank()
                ? username
                : from.getRealName().trim());
        user.setLanguage("zh-CN");
        user.setTimezone("Asia/Shanghai");
        user.setSecurityVersion(0L);
        if (userMapper.insert(user) != 1) {
            throw new IllegalStateException("创建初始化用户失败");
        }

        authenticationIdentityService.createPasswordIdentity(user.getId(), username);
        passwordCredentialService.createOrReplace(user.getId(), passwordEncoder.encode(from.getPassword()), false);
        var enrollment = mfaService.beginTotpEnrollment(user.getId());
        MfaLoginChallenge challenge = requireChallengePort().create(
                user.getId(), username, ClientType.WEB, true);
        UUID initializationId = UUID.fromString(challenge.id());

        state.setState(INITIALIZING);
        state.setInitializationId(initializationId);
        if (stateMapper.updateById(state) != 1) {
            throw new IllegalStateException("保存初始化状态失败");
        }
        return new SystemInitializationStartVO(initializationId, enrollment.enrollmentId(),
                enrollment.provisioningUri(), enrollment.secret(), challenge.expiresAt());
    }

    @Override
    @Transactional
    public SystemInitializationMfaConfirmVO confirmMfa(SystemInitializationMfaConfirmFrom from) {
        MfaLoginChallenge challenge = requireInitializationChallenge(from.getInitializationId(), false);
        if (!challenge.enrollmentRequired() || challenge.enrollmentCompleted()) {
            throw new IllegalStateException("初始化 MFA 挑战状态无效");
        }
        var recoveryCodes = mfaService.confirmTotpEnrollment(challenge.userId(), from.getEnrollmentId(), from.getCode());
        if (!requireChallengePort().markEnrollmentCompleted(challenge.id())) {
            throw new IllegalStateException("初始化 MFA 挑战状态更新失败");
        }
        return new SystemInitializationMfaConfirmVO(recoveryCodes);
    }

    @Override
    @Transactional
    public void complete(SystemInitializationCompleteFrom from) {
        MfaLoginChallenge challenge = requireInitializationChallenge(from.getInitializationId(), true);
        lockInitialization();
        SystemState state = loadState(true);
        if (!INITIALIZING.equals(state.getState())
                || !challenge.id().equals(String.valueOf(state.getInitializationId()))) {
            throw new IllegalStateException("初始化状态已变化，请重新开始初始化");
        }

        User user = userMapper.selectById(challenge.userId());
        if (user == null || !UserStatus.LOCKED.equals(user.getStatus())) {
            throw new IllegalStateException("初始化用户状态无效");
        }
        SecurityRole role = securityRoleMapper.selectOne(new LambdaQueryWrapper<SecurityRole>()
                .eq(SecurityRole::getCode, DEV_OPS_ROLE)
                .eq(SecurityRole::getState, "ACTIVE")
                .last("LIMIT 1"));
        if (role == null) {
            throw new IllegalStateException("ROLE_DEV_OPS 种子不存在");
        }

        user.setStatus(UserStatus.ACTIVE);
        if (userMapper.updateById(user) != 1) {
            throw new IllegalStateException("激活初始化用户失败");
        }
        RoleAssignment assignment = roleAssignmentMapper.selectOne(new LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getUserId, user.getId())
                .eq(RoleAssignment::getRoleId, role.getId())
                .eq(RoleAssignment::getState, "ACTIVE")
                .last("LIMIT 1"));
        if (assignment == null) {
            assignment = new RoleAssignment();
            assignment.setUserId(user.getId());
            assignment.setRoleId(role.getId());
            assignment.setState("ACTIVE");
            assignment.setVersion(0L);
            if (roleAssignmentMapper.insert(assignment) != 1) {
                throw new IllegalStateException("创建 DEV_OPS 角色分配失败");
            }
        }

        state.setState(INITIALIZED);
        state.setInitializedAt(Instant.now());
        state.setInitializedBy(user.getId());
        if (stateMapper.updateById(state) != 1) {
            throw new IllegalStateException("完成系统初始化失败");
        }

        if (!requireChallengePort().consume(challenge.id())) {
            throw new IllegalStateException("初始化 MFA 挑战已失效");
        }
        initializationTokenManager.clear();
    }

    private User findExistingUser(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, username)
                .or(wrapper -> wrapper.eq(User::getUsername, username))
                .last("LIMIT 1"));
    }

    private SystemState loadState(boolean lock) {
        SystemState state = lock
                ? stateMapper.selectForUpdateByStateKey(SYSTEM_KEY)
                : stateMapper.selectOne(new LambdaQueryWrapper<SystemState>()
                        .eq(SystemState::getStateKey, SYSTEM_KEY));
        if (state == null) {
            throw new IllegalStateException("系统初始化状态不存在");
        }
        return state;
    }

    private void lockInitialization() {
        jdbcTemplate.query("SELECT pg_advisory_xact_lock(hashtext('" + ADVISORY_LOCK + "'))",
                resultSet -> null);
    }

    private MfaLoginChallenge requireInitializationChallenge(String initializationId, boolean completed) {
        UUID challengeUuid;
        try {
            challengeUuid = UUID.fromString(initializationId);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("初始化挑战无效", exception);
        }
        SystemState state = loadState(false);
        if (!INITIALIZING.equals(state.getState()) || !challengeUuid.equals(state.getInitializationId())) {
            throw new IllegalStateException("初始化状态不存在或已完成");
        }
        MfaLoginChallenge challenge = requireChallengePort().find(initializationId);
        if (challenge == null
                || challenge.clientType() != ClientType.WEB
                || challenge.enrollmentCompleted() != completed) {
            throw new IllegalStateException("初始化 MFA 挑战不存在或已过期");
        }
        return challenge;
    }

    private SecurityMfaChallengePort requireChallengePort() {
        SecurityMfaChallengePort port = challengeProvider.getIfAvailable();
        if (port == null) {
            throw new IllegalStateException("Redis MFA 挑战存储未配置");
        }
        return port;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
