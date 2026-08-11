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
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.ObjUtils;
import com.devops00.spectra.core.auth.javabean.converter.AuthConverter;
import com.devops00.spectra.core.auth.javabean.constant.AccountStatus;
import com.devops00.spectra.core.auth.javabean.entity.Account;
import com.devops00.spectra.core.user.javabean.entity.Role;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.vo.AuthorityVO;
import com.devops00.spectra.core.user.service.RelRoleAuthorityService;
import com.devops00.spectra.core.user.service.RelUserRoleService;
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.constant.LoginType;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

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

    private final RelRoleAuthorityService relRoleAuthorityService;

    private final RelUserRoleService relUserRoleService;

    private final AuthConverter authConverter;

    private final DataScopeProvider dataScopeProvider;

    public SecurityUserHelper(RelRoleAuthorityService relRoleAuthorityService, RelUserRoleService relUserRoleService, AuthConverter authConverter,
            DataScopeProvider dataScopeProvider) {
        this.relRoleAuthorityService = relRoleAuthorityService;
        this.relUserRoleService = relUserRoleService;
        this.authConverter = authConverter;
        this.dataScopeProvider = dataScopeProvider;
    }

    /**
     * 数据库用户实体转SpringSecurity使用的用户对象
     *
     * @param loginType
     *            本次登录方式
     * @param account
     *            数据库账号实体
     * @param user
     *            数据库用户实体
     * @return SpringSecurity的用户对象
     */
    public SecurityUser toSecurityUser(LoginType loginType, Account account, Object user) {
        if (loginType == null || account == null || !(user instanceof User u)) {
            throw new LoginException("账号当前不可用");
        }

        var now = Instant.now();
        boolean accountActive = AccountStatus.ACTIVE.getCode().equals(account.getStatus());
        boolean accountNotExpired = account.getExpiresAt() == null || account.getExpiresAt().isAfter(now);
        boolean userActive = UserStatus.ACTIVE.getCode().equals(u.getStatus());
        boolean accountTypeMatches = loginType.equals(account.getType());
        boolean verified = loginType == LoginType.PASSWORD
                || Short.valueOf((short) 1).equals(account.getVerified());

        if (account.getDeleted() != null || u.getDeleted() != null || !accountTypeMatches || !accountActive || !accountNotExpired
                || !verified || !userActive) {
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
     * 构建用户权限列表
     *
     * @param userId
     *            用户ID
     * @return 权限列表
     */
    public List<SimpleGrantedAuthority> buildAuthorities(UUID userId) {
        var authorities = new ArrayList<SimpleGrantedAuthority>();
        List<Role> roles = getUserRole(userId);

        if (CollUtils.isNotEmpty(roles)) {
            authorities.addAll(roles.stream().map(i -> new SimpleGrantedAuthority(i.getCode())).toList());

            List<UUID> roleIds = roles.stream().map(Role::getId).toList();

            List<AuthorityVO> authorityVOs = getUserAuthority(roleIds);
            if (CollUtils.isNotEmpty(authorityVOs)) {
                authorities.addAll(authorityVOs.stream().map(i -> new SimpleGrantedAuthority(i.getCode())).toList());
            }
        }

        return authorities;
    }

    /**
     * 获取用户角色信息
     *
     * @param userId
     *            用户ID
     * @return 角色列表
     */
    public List<Role> getUserRole(UUID userId) {
        var roles = relUserRoleService.getRoles(userId);
        return roles == null ? Collections.emptyList() : new ArrayList<>(roles);
    }

    /**
     * 获取角色包含的权限信息
     *
     * @param roles
     *            角色ID列表
     * @return 权限列表
     */
    public List<AuthorityVO> getUserAuthority(List<UUID> roles) {
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        var authorities = relRoleAuthorityService.get(roles);
        return authorities == null ? Collections.emptyList() : ObjUtils.castList(new ArrayList<>(authorities), AuthorityVO.class);
    }
}
