package io.github.yangxj96.spectra.core.mapper.user;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yangxj96.spectra.core.javabean.user.entity.RoleDataScope;
import org.apache.ibatis.annotations.Param;

/// 角色数据范围Mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/23 11:36
public interface RoleDataScopeMapper extends BaseMapper<RoleDataScope> {

    /**
     * 根据角色ID查询角色数据范围权限
     *
     * @param roleId 角色ID
     * @return 数据范围信息
     */
    RoleDataScope findByRoleId(@Param("roleID") Long roleId);
}
