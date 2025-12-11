package io.github.yangxj96.spectra.core.configure.security;


import io.github.yangxj96.spectra.common.utils.CollUtils;
import io.github.yangxj96.spectra.common.utils.StrUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * 用DTO传输类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 17:55
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUser implements UserDetails {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 所属组织机构ID
     */
    private Long organizationId;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户状态
     */
    private Short state;

    private boolean enabled = true;

    private boolean accountNonExpired = true;

    private boolean accountNonLocked = true;

    private boolean credentialsNonExpired = true;

    private transient Map<String,Object> extend;

    /**
     * 简易权限处理,主要是为了序列化的问题
     */
    private List<SimpleGrantedAuthority> authorities;

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return CollUtils.isNotEmpty(authorities) ? authorities : Collections.emptyList();
    }

    @Override
    @NullMarked
    public String getUsername() {
        return StrUtils.isNotBlank(this.getEmail()) ? this.getEmail() : "";
    }

    public void setUsername(String username) {
        this.setEmail(username);
    }

}
