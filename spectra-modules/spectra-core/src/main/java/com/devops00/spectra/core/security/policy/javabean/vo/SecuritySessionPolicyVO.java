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

/** 登录端会话策略视图。 */
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
