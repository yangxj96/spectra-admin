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
     * 根据账号ID获取所拥有的角色
     *
     * @param accountId 账号 ID
     * @return 角色列表
     */
    List<Role> getAccountId(@Param("accountId") Long accountId);

}
