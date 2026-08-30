/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.service;

import java.util.Map;

/**
 * 单次文件上传清理扫描的脱敏统计。
 *
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
