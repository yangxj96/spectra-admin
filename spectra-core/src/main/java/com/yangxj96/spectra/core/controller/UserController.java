package com.yangxj96.spectra.core.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.common.base.Verify;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.core.javabean.from.UserPageFrom;
import com.yangxj96.spectra.core.javabean.from.UserRelevanceRolesFrom;
import com.yangxj96.spectra.core.javabean.from.UserSaveFrom;
import com.yangxj96.spectra.core.javabean.vo.UserPageVO;
import com.yangxj96.spectra.core.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService bindService;

    /**
     * 分页查询用户列表
     *
     * @param page   分页参数
     * @param params 查询条件参数
     * @return 分页结果
     */
    @ULog("分页查询用户列表")
    @SaCheckPermission(value = "ROLE:RELEVANCE_ROLES", orRole = "DEV_ADMIN")
    @GetMapping("/page")
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        return bindService.page(page, params);
    }

    /**
     * 关联用户到角色
     *
     * @param params 请求参数
     */
    @PutMapping("/relevanceRoles")
    public void relevanceRoles(@Validated @RequestBody UserRelevanceRolesFrom params) {
        bindService.relevanceRoles(params);
    }

    /**
     * 创建用户
     *
     * @param params 请求参数
     */
    @PostMapping
    public void created(@Validated(Verify.Insert.class) @RequestBody UserSaveFrom params) {
        bindService.create(params);
    }

    /**
     * 根据ID更新用户
     *
     * @param params 请求参数
     */
    @PutMapping
    public void updateById(@Validated(Verify.Update.class) @RequestBody UserSaveFrom params) {
        bindService.updateById(params);
    }

    /**
     * 根据用户ID删除用户信息
     * @param uid 用户ID
     */
    @DeleteMapping("/{uid}")
    public void deleteById(@PathVariable String uid){
        bindService.deleteById(uid);
    }
}
