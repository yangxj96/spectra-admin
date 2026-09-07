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

package com.devops00.spectra.core.security.audit.javabean.vo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 安全审计归档 manifest 的对外状态视图。
 *
 * <p>归档内部使用 UTC {@code Instant}，API 按当前用户时区返回 {@code LocalDateTime}，并且不暴露
 * 租约所有者、租约截止时间等内部调度字段。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
public record SecurityAuditArchiveManifestVO(UUID manifestId,
                                             String partitionName,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             String objectUri,
                                             String contentSha256,
                                             Long contentLength,
                                             Long rowCount,
                                             String state,
                                             LocalDateTime archivedAt,
                                             LocalDateTime verifiedAt,
                                             String lastError,
                                             int attempts,
                                             LocalDateTime availableAt) {
}
