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

package com.devops00.spectra.core.auth.service.impl;

import com.devops00.spectra.common.mybatis.DataScopeProvider;
import com.devops00.spectra.core.auth.javabean.constant.AccountStatus;
import com.devops00.spectra.core.auth.javabean.converter.AuthConverter;
import com.devops00.spectra.core.auth.javabean.entity.Account;
import com.devops00.spectra.core.auth.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.auth.javabean.entity.PasswordCredential;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SecurityUser构建工具
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/28
 */
@Component
@NullMarked
public class SecurityUserHelper {

    private final AuthConverter authConverter;

    private final DataScopeProvider dataScopeProvider;

    private final AuthorizationSnapshotProvider authorizationSnapshotProvider;

    public SecurityUserHelper(AuthConverter authConverter, DataScopeProvider dataScopeProvider,
                              AuthorizationSnapshotProvider authorizationSnapshotProvider) {
        this.authConverter = authConverter;
        this.dataScopeProvider = dataScopeProvider;
        this.authorizationSnapshotProvider = authorizationSnapshotProvider;
    }

    /**
     * 数据库用户实体转SpringSecurity使用的用户对象
     *
     * @param loginType 本次登录方式
     * @param account   数据库账号实体
     * @param user      数据库用户实体
     * @return SpringSecurity的用户对象
     */
    public SecurityUser toSecurityUser(LoginType loginType, Account account, Object user) {
        if (loginType == null || account == null || !(user instanceof User u)) {
            throw new LoginException("账号当前不可用");
        }

        var now = Instant.now();
        boolean accountActive = AccountStatus.ACTIVE.getCode().equals(account.getStatus());
        boolean accountNotExpired = account.getExpiresAt() == null || account.getExpiresAt().isAfter(now);
        boolean userActive = UserStatus.ACTIVE.equals(u.getStatus());
        boolean accountTypeMatches = loginType.equals(account.getType());
        boolean verified = loginType == LoginType.PASSWORD
                || Short.valueOf((short) 1).equals(account.getVerified());

        if (account.getDeleted() != null
                || u.getDeleted() != null
                || !accountTypeMatches
                || !accountActive
                || !accountNotExpired
                || !verified
                || !userActive) {
            throw new LoginException("账号当前不可用");
        }

        var securityUser = authConverter.toSecurityUser(u);
        securityUser.setEnabled(accountActive && userActive);
        securityUser.setAccountNonExpired(accountNotExpired);
        securityUser.setAccountNonLocked(accountActive);
        securityUser.setCredentialsNonExpired(accountNotExpired && verified);
        securityUser.setAuthorities(buildAuthorities(securityUser.getId()));

        // 加载数据范围信息
        var scope = dataScopeProvider.resolve(securityUser.getId());
        securityUser.setDataScopeType(scope.getScopeType());
        securityUser.setDepartmentId(scope.getDepartmentId());
        securityUser.setDataScopeTargetIds(scope.getTargetIds());

        return securityUser;
    }

    /**
     * 使用目标 authentication_identity/password_credential 模型构建安全主体。
     */
    public SecurityUser toSecurityUser(LoginType loginType, AuthenticationIdentity identity, PasswordCredential credential, Object user) {
        if (loginType == null || identity == null || credential == null || !(user instanceof User u)) {
            throw new LoginException("账号当前不可用");
        }
        if (!loginType.name().equals(identity.getMethodCode())
                || !"ACTIVE".equals(identity.getState())
                || Boolean.TRUE.equals(credential.getMustChange())) {
            throw new LoginException("账号当前不可用");
        }
        if (identity.getUserId() == null || !identity.getUserId().equals(u.getId())) {
            throw new LoginException("账号当前不可用");
        }
        if (u.getDeleted() != null || !UserStatus.ACTIVE.equals(u.getStatus())) {
            throw new LoginException("账号当前不可用");
        }

        var securityUser = authConverter.toSecurityUser(u);
        securityUser.setEnabled(true);
        securityUser.setAccountNonExpired(true);
        securityUser.setAccountNonLocked(true);
        securityUser.setCredentialsNonExpired(true);
        securityUser.setAuthorities(buildAuthorities(securityUser.getId()));
        var scope = dataScopeProvider.resolve(securityUser.getId());
        securityUser.setDataScopeType(scope.getScopeType());
        securityUser.setDepartmentId(scope.getDepartmentId());
        securityUser.setDataScopeTargetIds(scope.getTargetIds());
        return securityUser;
    }

    /**
     * 构建用户权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<SimpleGrantedAuthority> buildAuthorities(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        var snapshot = authorizationSnapshotProvider.load(userId);
        var authorities = new ArrayList<SimpleGrantedAuthority>();
        snapshot.assignments().stream()
                .map(assignment -> assignment.roleCode())
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        snapshot.permissions().stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        return List.copyOf(authorities);
    }
}
