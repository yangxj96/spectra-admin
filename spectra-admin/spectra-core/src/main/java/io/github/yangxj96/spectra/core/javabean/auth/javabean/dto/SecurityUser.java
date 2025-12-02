package io.github.yangxj96.spectra.core.javabean.auth.javabean.dto;


import io.github.yangxj96.spectra.common.utils.CollUtils;
import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.javabean.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
@EqualsAndHashCode(callSuper = true)
public class SecurityUser extends User implements UserDetails {

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

}
