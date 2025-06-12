package com.yangxj96.spectra.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yangxj96.spectra.security.entity.dto.Authority;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthorityMapper extends BaseMapper<Authority> {

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<Authority> getByRoleIds(@Param("roleIds") List<Long> roleIds);
}
