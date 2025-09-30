package io.github.yangxj96.spectra.core.auth.mapper;

import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 安全mapper
 */
public interface SecurityMapper {

    /**
     * 根据用户ID获取用户角色
     *
     * @param uid 用户ID
     * @return 角色列表
     */
    List<Role> getRolesByUserId(@Param("uid") long uid);

    /**
     * 根据用户ID获取用户菜单
     *
     * @param uid 用户ID
     * @return 菜单列表
     */
    List<Menu> getMenusByUserId(@Param("uid") long uid);
}
