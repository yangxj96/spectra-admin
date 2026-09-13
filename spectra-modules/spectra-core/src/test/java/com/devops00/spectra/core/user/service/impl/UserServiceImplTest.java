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

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.BusinessRuleViolationException;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecuritySessionQueryPort;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.security.policy.SecurityPasswordPolicyProvider;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authentication.javabean.entity.PasswordCredential;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.security.change.SecurityChangeExecutor;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.converter.UserConverter;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.ChangePasswordFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.user.provider.DefaultUserPasswordProvider;
import com.devops00.spectra.core.user.service.OnlineUserPageAssembler;
import com.devops00.spectra.framework.assembler.NameFillExecutor;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户初始凭据和管理员重置密码行为测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String DEFAULT_PASSWORD_HASH = "$2a$12$encoded-default-password-hash";

    private static final UUID USER_ID = UUID.fromString("018f3f2a-7c44-7d31-8c21-9a48de15f120");

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserConverter userConverter;

    @Mock
    private AuthorizationAssignmentQueryService authorizationAssignmentQueryService;

    @Mock
    private AuthorizationAssignmentChangeService authorizationAssignmentChangeService;

    @Mock
    private RoleAssignmentMapper roleAssignmentMapper;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationIdentityService authenticationIdentityService;

    @Mock
    private UserContactService userContactService;

    @Mock
    private PasswordCredentialService passwordCredentialService;

    @Mock
    private NameFillExecutor fillExecutor;

    @Mock
    private SecurityChangeExecutor securityChangeExecutor;

    @Mock
    private AuditService auditService;

    @Mock
    private AuditRecordFactory auditRecordFactory;

    @Mock
    private SecurityContextAccessor securityContextAccessor;

    @Mock
    private SecuritySessionQueryPort securitySessionQueryPort;

    @Mock
    private OnlineUserPageAssembler onlineUserPageAssembler;

    @Mock
    private SecuritySessionRevocationPort securitySessionRevocationPort;

    @Mock
    private SecurityPasswordPolicyProvider securityPasswordPolicyProvider;

    @Mock
    private TimeMapper timeMapper;

    @Mock
    private DefaultUserPasswordProvider defaultUserPasswordProvider;

    @InjectMocks
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);
    }

    @Test
    void shouldUseTheConfiguredEncodedPasswordForNewUsers() {
        var params = activeUserParams();
        var user = userEntity();
        when(defaultUserPasswordProvider.requireEncodedPassword()).thenReturn(DEFAULT_PASSWORD_HASH);
        when(userConverter.toEntity(params)).thenReturn(user);
        when(userMapper.insert(user)).thenReturn(1);

        service.create(params);

        verify(passwordCredentialService).createOrReplace(USER_ID, DEFAULT_PASSWORD_HASH, true);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldUseTheApplySnapshotForImportedUsers() {
        var params = activeUserParams();
        var user = userEntity();
        when(userConverter.toEntity(params)).thenReturn(user);
        when(userMapper.insert(user)).thenReturn(1);

        service.createWithDefaultPasswordHash(params, DEFAULT_PASSWORD_HASH);

        verify(passwordCredentialService).createOrReplace(USER_ID, DEFAULT_PASSWORD_HASH, true);
        verify(defaultUserPasswordProvider, never()).requireEncodedPassword();
    }

    @Test
    void shouldNotCreateAUserWhenTheDefaultPasswordIsMissing() {
        var params = activeUserParams();
        lenient().when(defaultUserPasswordProvider.requireEncodedPassword())
                .thenThrow(new DataException("系统默认密码未配置，请先在系统设置中配置"));

        assertThrows(DataException.class, () -> service.create(params));

        verify(defaultUserPasswordProvider).requireEncodedPassword();
        verify(userMapper, never()).insert(any(User.class));
        verify(passwordCredentialService, never()).createOrReplace(any(), anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void shouldResetToDefaultPasswordWithoutTimedExpiryAndReturnNoCredential() throws Exception {
        var user = userEntity();
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(passwordCredentialService.getByUserId(USER_ID)).thenReturn(new PasswordCredential());
        when(defaultUserPasswordProvider.requireEncodedPassword()).thenReturn(DEFAULT_PASSWORD_HASH);
        var resetMethod = UserServiceImpl.class.getMethod("passwordResetById", UUID.class);

        Object result = resetMethod.invoke(service, USER_ID);

        verify(passwordCredentialService).updatePassword(USER_ID, DEFAULT_PASSWORD_HASH, true, null);
        verify(securitySessionRevocationPort).revokeUserSessions(USER_ID);
        verify(passwordEncoder, never()).encode(anyString());
        assertNull(result);
    }

    @Test
    void shouldRejectReusingCurrentPasswordAsBusinessRuleViolation() {
        when(userMapper.selectById(USER_ID)).thenReturn(userEntity());
        var credential = new PasswordCredential();
        credential.setPasswordHash(DEFAULT_PASSWORD_HASH);
        when(passwordCredentialService.getByUserId(USER_ID)).thenReturn(credential);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        var params = new ChangePasswordFrom();
        params.setOldPassword("CurrentPassword1!");
        params.setNewPassword("CurrentPassword1!");
        params.setVerifyPassword("CurrentPassword1!");

        var exception = assertThrows(BusinessRuleViolationException.class,
                () -> service.changePassword(USER_ID, params));

        assertEquals("新密码不能与旧密码相同", exception.getMessage());
    }

    private static UserSaveFrom activeUserParams() {
        var params = new UserSaveFrom();
        params.setUsername("sample-user");
        params.setStatus(UserStatus.ACTIVE);
        return params;
    }

    private static User userEntity() {
        var user = new User();
        user.setId(USER_ID);
        user.setUsername("sample-user");
        user.setRealName("测试用户");
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
