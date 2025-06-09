package com.yangxj96.spectra.security.service.impl;

import com.yangxj96.spectra.security.entity.dto.Role;
import com.yangxj96.spectra.security.entity.from.RoleFrom;
import com.yangxj96.spectra.security.service.PermissionService;
import com.yangxj96.spectra.security.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限操作实现层
 *
 * @author 杨新杰
 * @since 2025/6/9 23:46
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private RoleService roleService;

    @Override
    @Transactional
    public void createdRole(RoleFrom params) {
        Role role = new Role();
        BeanUtils.copyProperties(params, role);
        roleService.save(role);
    }
}
