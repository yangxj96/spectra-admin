package com.yangxj96.spectra.core.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yangxj96.spectra.common.annotation.ULog;
import com.yangxj96.spectra.common.base.Verify;
import com.yangxj96.spectra.core.system.javabean.from.OrganizationFrom;
import com.yangxj96.spectra.core.system.javabean.vo.OrganizationTreeVo;
import com.yangxj96.spectra.core.system.service.OrganizationService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织机构控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/14
 */
@SaCheckLogin
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    @Resource
    private OrganizationService bindService;

    /**
     * 组织机构树形结构
     *
     * @return 组织机构树形结构数组
     */
    @ULog("获取组织机构树形列表")
    @GetMapping("/tree")
    public List<OrganizationTreeVo> tree() {
        return bindService.tree();
    }

    /**
     * 新增组织机构
     *
     * @param from 请求入参
     */
    @ULog("新增组织机构")
    @PostMapping
    public void created(@RequestBody @Validated(Verify.Insert.class) OrganizationFrom from) {
        bindService.created(from);
    }

    /**
     * 编辑组织机构
     *
     * @param from 请求入参
     */
    @ULog("编辑组织机构")
    @PutMapping
    public void modify(@RequestBody @Validated(Verify.Update.class) OrganizationFrom from) {
        bindService.modify(from);
    }

}
