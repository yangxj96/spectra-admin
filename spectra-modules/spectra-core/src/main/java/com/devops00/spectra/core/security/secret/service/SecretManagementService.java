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

import java.util.List;
import java.util.UUID;

/** 密钥定义、密文版本和发布状态管理端口。 */
public interface SecretManagementService {

    /** 当前启用密钥的受控导出内部记录；仅供传输服务使用，不得作为管理响应返回。 */
    record ActiveSecret(String code, String category, int version, String fingerprint, String value) {
    }

    /**
     * 查询全部已注册密钥定义。
     *
     * @return 按定义编码排序的密钥定义元数据列表，不包含任何密文载荷。
     */
    List<SecretDefinitionEntity> listDefinitions();

    /**
     * 查询指定密钥的版本元数据。
     *
     * @param code 已注册密钥编码。
     * @return 按版本号倒序排列的版本元数据列表，不包含任何密文载荷。
     */
    List<SecretVersionEntity> listVersions(String code);

    /**
     * 查询可导出的当前 ACTIVE 版本；category 为空表示全部分类。
     *
     * @param category 密钥分类；为空时查询全部可导出分类。
     * @return 当前 ACTIVE 版本的受控内部记录，仅供加密传输服务使用。
     */
    List<ActiveSecret> currentActiveSecrets(String category);

    /**
     * 创建待启用密钥版本；不会把明文放入返回对象。
     *
     * @param code      已注册密钥编码。
     * @param plaintext 待加密保存的密钥明文。
     * @param source    版本来源，例如 MANUAL、IMPORT、MIGRATION 或 GENERATED。
     * @return 新创建的待启用版本元数据，不包含明文。
     */
    SecretVersionEntity createPending(String code, String plaintext, String source);

    /**
     * 发布指定版本，并在同一事务中退役原 ACTIVE 版本。
     *
     * @param versionId 要发布的密钥版本 ID。
     */
    void publish(UUID versionId);

    /**
     * 校验版本属于路径中的密钥编码后发布。
     *
     * @param code      路径中的已注册密钥编码。
     * @param versionId 要发布的密钥版本 ID。
     */
    default void publish(String code, UUID versionId) {
        publish(versionId);
    }

    /**
     * 退役指定版本，不删除历史元数据。
     *
     * @param versionId 要退役的密钥版本 ID。
     */
    void retire(UUID versionId);

    /**
     * 校验版本属于路径中的密钥编码后退役。
     *
     * @param code      路径中的已注册密钥编码。
     * @param versionId 要退役的密钥版本 ID。
     */
    default void retire(String code, UUID versionId) {
        retire(versionId);
    }
}
