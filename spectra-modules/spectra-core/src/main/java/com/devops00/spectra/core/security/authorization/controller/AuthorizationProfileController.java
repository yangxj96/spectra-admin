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

package com.devops00.spectra.core.security.authorization.controller;

import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationProfileSaveFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileVO;
import com.devops00.spectra.core.security.authorization.service.AuthorizationProfileService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 可复用授权方案管理入口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Slf4j
@RestController
@RequestMapping("/security/authorization/profiles")
@RequiredArgsConstructor
public class AuthorizationProfileController {

    private final AuthorizationProfileService profileService;

    /**
     * 查询或获取目标数据（{@code all}）。
     */
    @ULog("'查询授权方案列表'")
    @GetMapping(value = "", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public List<AuthorizationProfileVO> all() {
        return profileService.all();
    }

    /**
     * 查询或获取目标数据（{@code detail}）。
     */
    @ULog("'查询授权方案详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public AuthorizationProfileVO detail(@PathVariable UUID id) {
        return profileService.detail(id);
    }

    /**
     * 创建或构建目标数据（{@code created}）。
     */
    @ULog("'创建授权方案'")
    @PostMapping(value = "", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:grant')")
    public void created(@Validated(Verify.Insert.class) @RequestBody AuthorizationProfileSaveFrom params) {
        profileService.created(params);
    }

    /**
     * 更新或推进目标状态（{@code modify}）。
     */
    @ULog("'修改授权方案'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:grant')")
    public void modify(@PathVariable UUID id,
                       @Validated(Verify.Update.class) @RequestBody AuthorizationProfileSaveFrom params) {
        profileService.modify(id, params);
    }

    /**
     * 处理内部业务逻辑（{@code enable}）。
     */
    @ULog("'启用授权方案'")
    @PutMapping(value = "/{id}/enable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:grant')")
    public void enable(@PathVariable UUID id) {
        profileService.enable(id);
    }

    /**
     * 更新或推进目标状态（{@code disable}）。
     */
    @ULog("'停用授权方案'")
    @PutMapping(value = "/{id}/disable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:grant')")
    public void disable(@PathVariable UUID id) {
        profileService.disable(id);
    }

    /**
     * 更新或推进目标状态（{@code deleteById}）。
     */
    @ULog("'删除授权方案'")
    @DeleteMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:grant')")
    public void deleteById(@PathVariable UUID id) {
        profileService.deleteById(id);
    }
}
