package com.yangxj96.spectra.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yangxj96.spectra.security.entity.dto.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据账号ID获取所拥有的角色
     *
     * @param accountId 账号 ID
     * @return 角色列表
     */
    List<Role> getAccountId(@Param("accountId") Long accountId);

}
