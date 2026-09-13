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

package com.devops00.spectra.core.security.authorization.service;

import com.devops00.spectra.core.user.javabean.vo.AuthorityTreeVO;

import java.util.List;

/**
 * 只读 Permission Catalog 展示服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface PermissionCatalogService {

    /**
     * 按资源分组返回活动 Permission，供管理端展示。
     *
     * @return 返回符合查询条件的权限目录树列表；无匹配时返回空列表，不返回 null。
     */
    List<AuthorityTreeVO> tree();
}
