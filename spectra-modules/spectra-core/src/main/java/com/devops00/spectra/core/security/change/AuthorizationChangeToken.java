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

package com.devops00.spectra.core.security.change;

import java.time.Instant;
import java.util.UUID;

/**
 * 承载授权令牌相关的不可变数据。
 *
 * @param tokenId         令牌标识
 * @param operatorId      操作人标识
 * @param targetUserId    目标用户标识
 * @param roleId          角色标识
 * @param assignmentId    分配标识
 * @param expectedVersion 执行变更时预期的版本号
 * @param requestHash     请求哈希值
 * @param expiresAt       该授权变更令牌的失效时间
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public record AuthorizationChangeToken(UUID tokenId,
                                       UUID operatorId,
                                       UUID targetUserId,
                                       UUID roleId,
                                       UUID assignmentId,
                                       long expectedVersion,
                                       String requestHash,
                                       Instant expiresAt) {
}
