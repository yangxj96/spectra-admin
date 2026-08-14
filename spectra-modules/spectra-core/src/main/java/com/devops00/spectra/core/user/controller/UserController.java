/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.core.authorization.LegacyAuthorizationWriteGuard;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.user.javabean.from.*;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.vo.UserPageVO;
import com.devops00.spectra.core.user.javabean.vo.UserProfileVO;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 用户控制器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService bindService;

    public UserController(UserService bindService) {
        this.bindService = bindService;
    }

    @ULog("'创建用户'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'USER:INSERT')")
    public void created(@Validated(Verify.Insert.class) @RequestBody UserSaveFrom params) {
        bindService.create(params);
    }

    @ULog("'根据ID删除用户'")
    @DeleteMapping(value = "/{uid}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'USER:DELETE')")
    public void deleteById(@PathVariable UUID uid) {
        bindService.deleteById(uid);
    }

    @ULog("'根据ID更新用户信息'")
    @PutMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'USER:UPDATE')")
    public void modify(@Validated(Verify.Update.class) @RequestBody UserSaveFrom params) {
        bindService.modify(params);
    }

    @ULog("'覆盖用户角色'")
    @PutMapping(value = "/{uid}/roles", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'USER:UPDATE')")
    public void replaceRoles(@PathVariable UUID uid, @Validated @RequestBody UserRelevanceRolesFrom from) {
        LegacyAuthorizationWriteGuard.reject("旧用户角色覆盖写入口");
    }

    @ULog("'重置用户密码'")
    @PutMapping(value = "/password/reset/{uid}", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void passwordResetById(@PathVariable UUID uid) {
        bindService.passwordResetById(uid);
    }

    @ULog("'锁定用户'")
    @PutMapping(value = "/lock/{uid}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'user:disable')")
    public void lock(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.LOCKED, reason);
    }

    @ULog("'解锁用户'")
    @PutMapping(value = "/unlock/{uid}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'user:unlock')")
    public void unlock(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.ACTIVE, reason);
    }

    @ULog("'禁用用户'")
    @PutMapping(value = "/disable/{uid}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'user:disable')")
    public void disable(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.DISABLED, reason);
    }

    @ULog("'启用用户'")
    @PutMapping(value = "/enable/{uid}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'user:unlock')")
    public void enable(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.ACTIVE, reason);
    }

    @ULog("'用户离职'")
    @PutMapping(value = "/depart/{uid}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'user:disable')")
    public void depart(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.DEPARTED, reason);
    }

    @ULog("'用户重新入职'")
    @PutMapping(value = "/reinstate/{uid}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'user:unlock')")
    public void reinstate(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.ACTIVE, reason);
    }

    @ULog("'分页查询用户列表'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'USER:QUERY')")
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) throws IllegalAccessException {
        return bindService.page(page, params);
    }

    @ULog("'获取在线用户'")
    @GetMapping(value = "/online", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'MONITOR:QUERY')")
    public List<UserOnlineVO> online(PageFrom page) {
        return bindService.online(page);
    }

    @ULog("'获取当前用户详情'")
    @GetMapping(value = "/profile", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public UserProfileVO getProfile() {
        UUID userId = SecUtil.getCurrentUserId();
        return bindService.getProfile(userId);
    }

    @ULog("'更新当前用户信息'")
    @PutMapping(value = "/profile", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void updateProfile(@Validated @RequestBody UserProfileFrom params) {
        UUID userId = SecUtil.getCurrentUserId();
        bindService.updateProfile(userId, params);
    }

    @ULog("'修改密码'")
    @PutMapping(value = "/password", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void changePassword(@Validated @RequestBody ChangePasswordFrom params) {
        UUID userId = SecUtil.getCurrentUserId();
        bindService.changePassword(userId, params);
    }
}
