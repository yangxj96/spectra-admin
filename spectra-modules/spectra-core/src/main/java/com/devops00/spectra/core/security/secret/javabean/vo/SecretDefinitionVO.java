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

package com.devops00.spectra.core.security.secret.javabean.vo;

import java.time.Instant;

/**
 * 封装密钥相关的响应数据。
 *
 * @param code                业务对象的唯一编码
 * @param name                密钥定义的名称
 * @param category            业务类别
 * @param valueType           密钥值的数据类型
 * @param ownerModule         定义该密钥的模块名称
 * @param description         业务对象的文字说明
 * @param mutable             密钥定义是否允许修改
 * @param hotReload           密钥定义是否支持热加载
 * @param exportable          密钥定义是否允许导出
 * @param activeVersion       活动状态版本
 * @param pendingVersionCount 待生效的密钥版本数量
 * @param activeFingerprint   活动状态
 * @param updatedAt           更新时间
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record SecretDefinitionVO(String code, String name, String category, String valueType, String ownerModule,
                                 String description, boolean mutable, boolean hotReload, boolean exportable,
                                 Integer activeVersion, long pendingVersionCount, String activeFingerprint,
                                 Instant updatedAt) {
}
