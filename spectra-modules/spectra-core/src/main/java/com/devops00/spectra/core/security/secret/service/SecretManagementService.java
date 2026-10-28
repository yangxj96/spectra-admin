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

package com.devops00.spectra.core.security.secret.service;

import com.devops00.spectra.core.security.secret.javabean.entity.SecretDefinitionEntity;
import com.devops00.spectra.core.security.secret.javabean.entity.SecretVersionEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

/**
 * 密钥定义、密文版本和发布状态管理端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface SecretManagementService {

    /**
     * 定义活动状态密钥相关的应用服务契约。
     */
    record ActiveSecret(String code, String category, int version, String fingerprint, String value) {
    }

    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    List<SecretDefinitionEntity> listDefinitions();

    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    List<SecretVersionEntity> listVersions(String code);

    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    SecretVersionEntity createPendingAsAdmin(String code, String plaintext, String source);

    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    void publishAsAdmin(String code, UUID versionId);

    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    void retire(String code, UUID versionId);
}
