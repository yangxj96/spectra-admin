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

import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.core.security.authorization.entity.AuthorizationProfile;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationProfileSaveFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileVO;

import java.util.List;
import java.util.UUID;

/**
 * 授权方案管理服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
public interface AuthorizationProfileService extends BaseService<AuthorizationProfile> {

    /**
     * 创建授权方案。
     *
     * @param params 创建参数
     */
    void created(AuthorizationProfileSaveFrom params);

    /**
     * 修改授权方案。
     *
     * @param id     方案 ID
     * @param params 修改参数
     */
    void modify(UUID id, AuthorizationProfileSaveFrom params);

    /**
     * 启用授权方案。
     *
     * @param id 方案 ID
     */
    void enable(UUID id);

    /**
     * 停用授权方案。
     *
     * @param id 方案 ID
     */
    void disable(UUID id);

    /**
     * 删除授权方案。
     *
     * @param id 方案 ID
     */
    void deleteById(UUID id);

    /**
     * 查询可见授权方案。
     *
     * @return 授权方案列表
     */
    List<AuthorizationProfileVO> all();

    /**
     * 查询授权方案详情。
     *
     * @param id 方案 ID
     * @return 授权方案详情
     */
    AuthorizationProfileVO detail(UUID id);
}
