/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authorization.service;

import com.devops00.spectra.core.user.javabean.vo.AuthorityTreeVO;

import java.util.List;

/**
 * 只读 Permission Catalog 展示服务。
 */
public interface PermissionCatalogService {

    /**
     * 按资源分组返回活动 Permission，供管理端展示。
     */
    List<AuthorityTreeVO> tree();
}
