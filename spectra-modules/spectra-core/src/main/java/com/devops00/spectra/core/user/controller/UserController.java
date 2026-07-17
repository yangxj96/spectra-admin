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
import com.devops00.spectra.core.user.javabean.from.UserPageFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.vo.UserPageVO;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/// 用户控制器
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
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
    @PreAuthorize("hasPermission(null ,'USER:UPDATE')")
    public void created(@Validated(Verify.Insert.class) @RequestBody UserSaveFrom params) {
        bindService.create(params);
    }

    @ULog("'根据ID删除用户'")
    @DeleteMapping(value = "/{uid}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'USER:DELETE')")
    public void deleteById(@PathVariable String uid) {
        bindService.deleteById(UUID.fromString(uid));
    }

    @ULog("'根据ID更新用户信息'")
    @PutMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'USER:UPDATE')")
    public void updateById(@Validated(Verify.Update.class) @RequestBody UserSaveFrom params) {
        bindService.updateById(params);
    }

    @ULog("'重置用户密码'")
    @PutMapping(value = "/password/reset/{uid}", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void passwordResetById(@PathVariable String uid) {
        bindService.passwordResetById(UUID.fromString(uid));
    }

    @ULog("'分页查询用户列表'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) throws IllegalAccessException {
        return bindService.page(page, params);
    }

    @ULog("'获取在线用户'")
    @GetMapping(value = "/online", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public List<UserOnlineVO> online(PageFrom page) {
        return bindService.online(page);
    }
}
