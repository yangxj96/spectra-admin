package com.yangxj96.spectra.security.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.core.base.Verify;
import com.yangxj96.spectra.core.base.javabean.from.PageFrom;
import com.yangxj96.spectra.security.entity.from.RoleFrom;
import com.yangxj96.spectra.security.entity.from.RolePageFrom;
import com.yangxj96.spectra.security.entity.vo.RoleVO;
import com.yangxj96.spectra.security.service.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * 分页查询角色信息
     *
     * @param page   分页信息,页码和每页数量
     * @param params 角色分页入参
     * @return 分页结果
     */
    @ULog("分页查询角色列表")
    @GetMapping("/pageRole")
    public IPage<RoleVO> pageRole(PageFrom page, RolePageFrom params) {
        return bindService.pageRole(page, params);
    }


    /**
     * 创建角色
     *
     * @param params 角色实体
     */
    @ULog("创建角色")
    @PostMapping("/createdRole")
    public void createdRole(@Validated(Verify.Insert.class) @RequestBody RoleFrom params) {
        bindService.createdRole(params);
    }

    /**
     * 修改角色信息
     *
     * @param params 角色实体
     */
    @ULog("修改角色信息")
    @PutMapping("/modifyRole")
    public void modifyRole(@Validated(Verify.Update.class) @RequestBody RoleFrom params) {
        bindService.modifyRole(params);
    }

}
