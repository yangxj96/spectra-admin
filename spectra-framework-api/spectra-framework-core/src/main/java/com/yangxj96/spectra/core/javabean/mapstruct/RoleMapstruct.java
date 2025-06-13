package com.yangxj96.spectra.core.javabean.mapstruct;


import com.yangxj96.spectra.core.javabean.entity.Role;
import com.yangxj96.spectra.core.javabean.vo.RoleVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 角色相关mapstruct
 *
 * @author Jack Young
 * @since 2025/6/13 15:16
 */
@Mapper(componentModel = "spring")
public interface RoleMapstruct {

    RoleVO toVO(Role role);

    List<RoleVO> toVOs(List<Role> roles);
}
