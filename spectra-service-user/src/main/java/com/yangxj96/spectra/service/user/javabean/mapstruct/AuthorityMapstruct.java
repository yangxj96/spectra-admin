package com.yangxj96.spectra.service.user.javabean.mapstruct;

import com.yangxj96.spectra.service.user.javabean.entity.Authority;
import com.yangxj96.spectra.share.javabean.ShareAuthority;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 权限mapstruct
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Mapper(componentModel = "spring")
public interface AuthorityMapstruct {

    /**
     * 转换成共享对象
     *
     * @param role 实体对象
     * @return 共享对象
     */
    ShareAuthority toShare(Authority role);

    /**
     * 转换成共享对象列表
     *
     * @param authorities 实体对象列表
     * @return 共享对象
     */
    List<ShareAuthority> toShares(List<Authority> authorities);


}
