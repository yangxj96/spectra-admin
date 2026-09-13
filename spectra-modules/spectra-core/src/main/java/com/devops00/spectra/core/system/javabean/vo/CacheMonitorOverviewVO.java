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

package com.devops00.spectra.core.system.javabean.vo;

import java.time.Instant;

/**
 * 封装缓存相关的响应数据。
 *
 * @param status              业务状态
 * @param generatedAt         监控概览的生成时间
 * @param regionCount         缓存区域数量
 * @param onlineSessionCount  在线会话数量
 * @param issueCount          当前发现的问题数量
 * @param securityRedisStatus 安全Redis状态
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record CacheMonitorOverviewVO(
                                     String status,
                                     Instant generatedAt,
                                     int regionCount,
                                     Long onlineSessionCount,
                                     int issueCount,
                                     String securityRedisStatus) {
}
