package io.github.yangxj96.spectra.core.service.auth.impl;


import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.utils.CollUtils;
import io.github.yangxj96.spectra.core.configure.security.javabean.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.converter.AuthConverter;
import io.github.yangxj96.spectra.core.javabean.user.entity.Role;
import io.github.yangxj96.spectra.core.javabean.user.entity.User;
import io.github.yangxj96.spectra.core.javabean.user.vo.AuthorityVO;
import io.github.yangxj96.spectra.core.service.auth.AccountService;
import io.github.yangxj96.spectra.core.service.auth.AuthService;
import io.github.yangxj96.spectra.core.service.user.RelRoleAuthorityService;
import io.github.yangxj96.spectra.core.service.user.RelUserRoleService;
import io.github.yangxj96.spectra.core.service.user.UserService;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// 认证服务实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 11:28
@Service
@NullMarked
public class AuthServiceImpl implements AuthService {

    private final AuthConverter authConverter;

    private final RelRoleAuthorityService relRoleAuthorityService;

    private final RelUserRoleService relUserRoleService;

    private final UserService userService;

    private final AccountService accountService;

    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthConverter authConverter, RelRoleAuthorityService relRoleAuthorityService, RelUserRoleService relUserRoleService, UserService userService, AccountService accountService, PasswordEncoder passwordEncoder) {
        this.authConverter = authConverter;
        this.relRoleAuthorityService = relRoleAuthorityService;
        this.relUserRoleService = relUserRoleService;
        this.userService = userService;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Authentication login(String username, String password) {
        // 查询账户信息
        var account = accountService.getByLoginName(username);
        if (account == null || !passwordEncoder.matches(password, account.getPassword())) {
            throw new BadCredentialsException("账号或密码错误");
        }
        var user = userService.getById(account.getUserId());
        if (user == null) {
            throw new DataNotExistException("用户不存在");
        }
        // 验证通过,封装返回
        var su = toSecurityUser(user);

        return new UsernamePasswordAuthenticationToken(su, null, su.getAuthorities());
    }


    ///
    /// 数据库用户实体转SpringSecurity使用的用户对象
    ///
    /// @param user 数据库用户实体
    /// @return SpringSecuity的用户对象
    ///
    private SecurityUser toSecurityUser(User user) {
        var securityUser = authConverter.toSecurityUser(user);

        var authorities = new ArrayList<SimpleGrantedAuthority>();

        List<Role> roles = this.getUserRole(securityUser.getId());
        if (CollUtils.isNotEmpty(roles)) {
            authorities.addAll(roles.stream().map(i -> new SimpleGrantedAuthority(i.getCode())).toList());

            List<AuthorityVO> authority = this.getUserAuthority(roles.stream().map(Role::getId).toList());
            if (CollUtils.isNotEmpty(authority)) {
                authorities.addAll(authority.stream().map(i -> new SimpleGrantedAuthority(i.getCode())).toList());
            }
        }

        securityUser.setAuthorities(authorities);
        // 开始填充角色和权限
        return securityUser;
    }

    ///
    /// 获取用户角色信息
    ///
    /// @param userId 用户 ID
    /// @return 角色列表,无角色则返回空数组
    ///
    private List<Role> getUserRole(String userId) {
        return relUserRoleService.getRoles(userId);
    }

    ///
    /// 获取角色包含的权限信息
    ///
    /// @param roles 角色 ID 列表
    /// @return 权限列表
    ///
    private List<AuthorityVO> getUserAuthority(List<String> roles) {
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        return relRoleAuthorityService.get(roles);
    }
}
