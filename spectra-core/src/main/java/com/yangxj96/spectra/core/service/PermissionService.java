package com.yangxj96.spectra.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.core.javabean.from.RoleFrom;
import com.yangxj96.spectra.core.javabean.from.RolePageFrom;
import com.yangxj96.spectra.core.javabean.vo.RoleVO;

import java.util.List;


/**
 * 权限service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
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

    /**
     * 获取角色列表
     * @return 角色列表
     */
    List<RoleVO> listRole();

}
