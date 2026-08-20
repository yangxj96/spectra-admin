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

import com.devops00.spectra.core.security.authorization.service.PermissionCatalogService;
import com.devops00.spectra.core.user.javabean.vo.AuthorityTreeVO;
import com.devops00.spectra.log.base.annotation.ULog;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限相关操作
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
@RestController
@RequestMapping("/authority")
public class AuthorityController {

    private final PermissionCatalogService permissionCatalogService;

    public AuthorityController(PermissionCatalogService permissionCatalogService) {
        this.permissionCatalogService = permissionCatalogService;
    }

    @ULog("'获取 Permission Catalog 树列表'")
    @GetMapping(value = "/tree", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'permission:read')")
    public List<AuthorityTreeVO> tree() {
        return permissionCatalogService.tree();
    }
}
