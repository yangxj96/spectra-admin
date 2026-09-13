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

package com.devops00.spectra.core.upload.service;

import java.util.Map;

/**
 * 定义文件上传清理相关的应用服务契约。
 *
 * @param expiredSessions       过期状态会话
 * @param sessionRetryScheduled 会话重试
 * @param cleanedSessions       本次清理的上传会话数量
 * @param orphanedAssets        孤立状态资产
 * @param deletedAssets         删除状态资产
 * @param assetRetryScheduled   资产重试
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public record FileUploadCleanupSummary(long expiredSessions,
                                       long sessionRetryScheduled,
                                       long cleanedSessions,
                                       long orphanedAssets,
                                       long deletedAssets,
                                       long assetRetryScheduled) {

    public FileUploadCleanupSummary {
        if (expiredSessions < 0
                || sessionRetryScheduled < 0
                || cleanedSessions < 0
                || orphanedAssets < 0
                || deletedAssets < 0
                || assetRetryScheduled < 0) {
            throw new IllegalArgumentException("文件清理统计不能为负数");
        }
    }

    /** 返回可以写入调度执行记录的纯数字摘要。 */
    public Map<String, Object> resultSummary() {
        return Map.of(
                "expiredSessions", expiredSessions,
                "sessionRetryScheduled", sessionRetryScheduled,
                "cleanedSessions", cleanedSessions,
                "orphanedAssets", orphanedAssets,
                "deletedAssets", deletedAssets,
                "assetRetryScheduled", assetRetryScheduled);
    }
}
