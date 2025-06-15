package com.yangxj96.spectra.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yangxj96.spectra.core.javabean.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * 用户mapper层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 用户关联角色列表
     *
     * @param id      id
     * @param userId  用户ID
     * @param roleId  角色ID
     * @param current 当前用户
     */
    int insertRelevanceRoles(@Param("id") Long id, @Param("userId") Long userId, @Param("roleId") Long roleId, @Param("current") Long current);

    /**
     * 根据ID移除之前关联的记录
     *
     * @param userId 用户ID
     * @return 收到影响的行数
     */
    int removeRelevanceRoles(@Param("userId") Long userId);
}
