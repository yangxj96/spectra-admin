package com.yangxj96.spectra.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yangxj96.spectra.core.javabean.entity.Authority;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限mapper层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface AuthorityMapper extends BaseMapper<Authority> {

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<Authority> getByRoleIds(@Param("roleIds") List<Long> roleIds);
}
