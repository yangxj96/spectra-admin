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
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.user.javabean.from.ChangePasswordFrom;
import com.devops00.spectra.core.user.javabean.from.UserOnboardingFrom;
import com.devops00.spectra.core.user.javabean.from.UserPageFrom;
import com.devops00.spectra.core.user.javabean.from.UserProfileFrom;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.vo.UserPageVO;
import com.devops00.spectra.core.user.javabean.vo.UserProfileVO;
import com.devops00.spectra.core.user.javabean.vo.UserPasswordResetVO;
import com.devops00.spectra.core.user.javabean.vo.UserOnboardingVO;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.core.user.service.UserOnboardingService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private final UserOnboardingService onboardingService;

    private final SecurityContextAccessor securityContextAccessor;

    public UserController(UserService bindService, UserOnboardingService onboardingService,
                          SecurityContextAccessor securityContextAccessor) {
        this.bindService = bindService;
        this.onboardingService = onboardingService;
        this.securityContextAccessor = securityContextAccessor;
    }

    /**
     * 处理内部业务逻辑（{@code onboarding}）。
     */
    @ULog("'提交新增用户及角色授权'")
    @PostMapping(value = "/onboarding", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'user:create') and hasPermission(null, 'role:assign')")
    public UserOnboardingVO onboarding(@Validated(Verify.Insert.class) @RequestBody UserOnboardingFrom params) {
        return onboardingService.submit(params);
    }

    /**
     * 更新或推进目标状态（{@code modifyOnboarding}）。
     */
    @ULog("'提交用户信息及角色授权变更'")
    @PutMapping(value = "/onboarding", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'user:update') and hasPermission(null, 'role:assign')")
    public UserOnboardingVO modifyOnboarding(@Validated(Verify.Update.class) @RequestBody UserOnboardingFrom params) {
        return onboardingService.submit(params);
    }

    /**
     * 处理内部业务逻辑（{@code passwordResetById}）。
     */
    @ULog("'重置用户密码'")
    @PutMapping(value = "/password/reset/{uid}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'user:reset-password')")
    public UserPasswordResetVO passwordResetById(@PathVariable UUID uid, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        return bindService.passwordResetById(uid);
    }

    @ULog("'锁定用户'")
    @PutMapping(value = "/lock/{uid}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'user:disable')")
    public void lock(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.LOCKED, reason);
    }

    @ULog("'解锁用户'")
    @PutMapping(value = "/unlock/{uid}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'user:unlock')")
    public void unlock(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.ACTIVE, reason);
    }

    @ULog("'禁用用户'")
    @PutMapping(value = "/disable/{uid}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'user:disable')")
    public void disable(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.DISABLED, reason);
    }

    @ULog("'启用用户'")
    @PutMapping(value = "/enable/{uid}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'user:unlock')")
    public void enable(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.ACTIVE, reason);
    }

    @ULog("'用户离职'")
    @PutMapping(value = "/depart/{uid}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'user:disable')")
    public void depart(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.DEPARTED, reason);
    }

    @ULog("'用户重新入职'")
    @PutMapping(value = "/reinstate/{uid}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'user:unlock')")
    public void reinstate(@PathVariable UUID uid, @RequestParam(required = false) String reason) {
        bindService.changeStatus(uid, UserStatus.ACTIVE, reason);
    }

    /**
     * 查询或获取目标数据（{@code page}）。
     */
    @ULog("'分页查询用户列表'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'user:read')")
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) throws IllegalAccessException {
        return bindService.page(page, params);
    }

    /**
     * 查询或获取目标数据（{@code detail}）。
     */
    @ULog("'根据ID获取用户详情'")
    @GetMapping(value = "/{uid}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'user:read')")
    public UserPageVO detail(@PathVariable UUID uid) throws IllegalAccessException {
        return bindService.detail(uid);
    }

    /**
     * 处理内部业务逻辑（{@code online}）。
     */
    @ULog("'获取在线用户'")
    @GetMapping(value = "/online", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'session:read')")
    public List<UserOnlineVO> online(PageFrom page) {
        return bindService.online(page);
    }

    /**
     * 查询或获取目标数据（{@code getProfile}）。
     */
    @ULog("'获取当前用户详情'")
    @GetMapping(value = "/profile", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public UserProfileVO getProfile() {
        UUID userId = securityContextAccessor.currentUserId();
        return bindService.getProfile(userId);
    }

    /**
     * 更新或推进目标状态（{@code updateProfile}）。
     */
    @ULog("'更新当前用户信息'")
    @PutMapping(value = "/profile", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public void updateProfile(@Validated @RequestBody UserProfileFrom params) {
        UUID userId = securityContextAccessor.currentUserId();
        bindService.updateProfile(userId, params);
    }

    /**
     * 处理内部业务逻辑（{@code changePassword}）。
     */
    @ULog("'修改密码'")
    @PutMapping(value = "/password", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public void changePassword(@Validated @RequestBody ChangePasswordFrom params) {
        UUID userId = securityContextAccessor.currentUserId();
        bindService.changePassword(userId, params);
    }
}
