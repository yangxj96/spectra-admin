package com.devops00.spectra.security.base.javabean.entity;


import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.StrUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/// 用DTO传输类
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/2 17:55
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUser implements UserDetails {

    ///  用户ID
    private UUID id;

    /// 姓名
    private String name;

    /// 邮箱
    private String email;

    /// 头像
    private String avatar;

    /// 所属组织机构ID
    private String organizationId;

    /// 用户密码
    private String password;

    /// 用户状态
    private Short state;

    /// 时区
    private String timezone;

    private boolean enabled = true;

    private boolean accountNonExpired = true;

    private boolean accountNonLocked = true;

    private boolean credentialsNonExpired = true;

    @Nullable
    private transient Map<String, Object> extraData;

    /// 简易权限处理,主要是为了序列化的问题
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

    @SuppressWarnings("unused")
    public void setUsername(String username) {
        this.setEmail(username);
    }

}
