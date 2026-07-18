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

import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.exception.NotImplementedException;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.javabean.vo.AuthorityTreeVO;
import com.devops00.spectra.core.user.service.AuthorityService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/// 权限相关操作
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Slf4j
@RestController
@RequestMapping("/authority")
public class AuthorityController {

    private final AuthorityService bindService;

    public AuthorityController(AuthorityService bindService) {
        this.bindService = bindService;
    }

    @ULog("'创建权限'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void createdAuthority(@Validated(Verify.Insert.class) @RequestBody RoleFrom params) {
        throw new NotImplementedException("无需实现错误," + params);
    }

    @ULog("'删除权限'")
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void deleteAuthority(@PathVariable UUID id) {
        throw new NotImplementedException("无需实现错误," + id);
    }

    @ULog("'修改权限信息'")
    @PutMapping(version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void modifyAuthority(@Validated(Verify.Update.class) @RequestBody RoleFrom params) {
        throw new NotImplementedException("无需实现错误," + params);
    }

    @ULog("'获取权限树列表'")
    @GetMapping(value = "/tree", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public @Nullable List<AuthorityTreeVO> tree() {
        return bindService.tree();
    }

}
