package com.yangxj96.spectra.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yangxj96.spectra.core.javabean.entity.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色mapper层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getByUserId(@Param("userId") Long userId);

    /**
     * 删除关联的角色列表
     *
     * @param uid 用户ID
     * @return 删除的条目数
     */
    int removeRelevanceRoles(@Param("userId") Long uid);

    /**
     * 新增关联的角色列表
     *
     * @param id     主键ID
     * @param uid    用户ID
     * @param roleId 角色ID
     * @return 收影响的行数
     */
    int insertRelevanceRole(@Param("id") Long id, @Param("uid") Long uid, @Param("rid") Long roleId);
}
