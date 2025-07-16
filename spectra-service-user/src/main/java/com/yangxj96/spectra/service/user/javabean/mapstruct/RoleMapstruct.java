package com.yangxj96.spectra.service.user.javabean.mapstruct;

import com.yangxj96.spectra.service.user.javabean.entity.Role;
import com.yangxj96.spectra.share.javabean.ShareRole;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 角色转换用的
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Mapper(componentModel = "spring")
public interface RoleMapstruct {

    /**
     * 转换成共享对象
     *
     * @param role 实体对象
     * @return 共享对象
     */
    ShareRole toShare(Role role);

    /**
     * 转换成共享对象列表
     *
     * @param roles 实体对象列表
     * @return 共享对象
     */
    List<ShareRole> toShares(List<Role> roles);

}
