package com.yangxj96.spectra.security.controller;

import com.yangxj96.spectra.security.entity.from.RoleFrom;
import com.yangxj96.spectra.security.service.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限操作相关
 *
 * @author 杨新杰
 * @since 2025/6/9 23:45
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Resource
    private PermissionService bindService;

    /**
     * 创建角色
     */
    @PostMapping("/createdRole")
    public void createdRole(@Validated @RequestBody RoleFrom params){
        bindService.createdRole(params);
    }

}
