package io.github.yangxj96.spectra.core.configure.security.strategy;


import io.github.yangxj96.spectra.common.utils.CollUtils;
import io.github.yangxj96.spectra.core.javabean.auth.converter.AuthConverter;
import io.github.yangxj96.spectra.core.configure.security.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.user.entity.Role;
import io.github.yangxj96.spectra.core.javabean.user.entity.User;
import io.github.yangxj96.spectra.core.javabean.user.vo.AuthorityVO;
import io.github.yangxj96.spectra.core.service.user.RelRoleAuthorityService;
import io.github.yangxj96.spectra.core.service.user.RelUserRoleService;
import jakarta.annotation.Resource;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 抽象实现一些通用的复用逻辑
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/3 00:04
 */
@NullMarked
public abstract class AbstractLoginStrategy implements LoginStrategy {

    @Resource
    private AuthConverter authConverter;

    @Resource
    private RelRoleAuthorityService relRoleAuthorityService;

    @Resource
    private RelUserRoleService relUserRoleService;

    protected SecurityUser toSecurityUser(User user) {
        SecurityUser securityUser = authConverter.toUserDTO(user);

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


    /**
     * 获取用户角色信息
     *
     * @param userId 用户ID
     * @return 角色列表,无角色则返回空数组
     */
    private List<Role> getUserRole(Long userId) {
        return relUserRoleService.getRoles(userId);
    }

    /**
     * 获取角色包含的权限信息
     *
     * @param roles 角色ID列表
     * @return 权限列表
     */
    private List<AuthorityVO> getUserAuthority(List<Long> roles) {
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        return relRoleAuthorityService.get(roles);
    }

}
