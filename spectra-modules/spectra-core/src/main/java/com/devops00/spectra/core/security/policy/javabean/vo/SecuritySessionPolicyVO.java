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

package com.devops00.spectra.core.security.policy.javabean.vo;

import java.util.UUID;

/**
 * 封装安全会话策略相关的响应数据。
 *
 * @param clientId           客户端标识
 * @param clientCode         客户端编码
 * @param clientName         客户端应用名称
 * @param concurrencyMode    会话并发控制模式
 * @param allowConcurrent    是否允许多个并发会话
 * @param maxSessions        该客户端允许的最大并发会话数
 * @param accessTtlSeconds   访问令牌有效时长（秒）
 * @param refreshTtlSeconds  刷新令牌有效时长（秒）
 * @param absoluteTtlSeconds 会话的绝对有效时长（秒）
 * @param idleTtlSeconds     会话空闲超时时长（秒）
 * @param version            当前对象或配置的版本号
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record SecuritySessionPolicyVO(UUID clientId,
                                      String clientCode,
                                      String clientName,
                                      String concurrencyMode,
                                      Boolean allowConcurrent,
                                      Integer maxSessions,
                                      Integer accessTtlSeconds,
                                      Integer refreshTtlSeconds,
                                      Integer absoluteTtlSeconds,
                                      Integer idleTtlSeconds,
                                      Long version) {
}
