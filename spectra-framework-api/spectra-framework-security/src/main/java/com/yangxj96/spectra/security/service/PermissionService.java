package com.yangxj96.spectra.security.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.core.javabean.from.PageFrom;
import com.yangxj96.spectra.security.entity.from.RoleFrom;
import com.yangxj96.spectra.security.entity.from.RolePageFrom;
import com.yangxj96.spectra.security.entity.vo.RoleVO;

/**
 * 权限操作业务层
 */
public interface PermissionService {

    /**
     * 新增角色
     *
     * @param params 角色入参实体
     */
    void createdRole(RoleFrom params);

    /**
     * 修改角色
     *
     * @param params 角色入参实体
     */
    void modifyRole(RoleFrom params);

    /**
     * 分页查询角色信息
     *
     * @param page   分页信息
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<RoleVO> pageRole(PageFrom page, RolePageFrom params);
}
