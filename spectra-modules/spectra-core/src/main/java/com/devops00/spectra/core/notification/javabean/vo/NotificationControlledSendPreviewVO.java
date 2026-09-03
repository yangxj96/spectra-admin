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

package com.devops00.spectra.core.notification.javabean.vo;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 受控发送 Preview 结果；不包含完整用户清单、地址或敏感参数。
 */
public record NotificationControlledSendPreviewVO(UUID previewId, String previewToken, String requestHash,
                                                  LocalDateTime expiresAt, int candidateUserCount, int eligibleTaskCount,
                                                  int skippedTaskCount, Map<String, Integer> skippedCounts,
                                                  List<NotificationControlledSendSkippedDetailVO> skippedDetails,
                                                  Map<NotificationChannel, NotificationChannelAvailability> channelAvailability,
                                                  Map<NotificationChannel, NotificationControlledSendTemplateVO> templates,
                                                  List<NotificationControlledSendSampleVO> samples) {

    public NotificationControlledSendPreviewVO {
        skippedCounts = immutableMap(skippedCounts);
        skippedDetails = immutableList(skippedDetails);
        channelAvailability = immutableMap(channelAvailability);
        templates = immutableMap(templates);
        samples = immutableList(samples);
    }

    @Override
    public Map<String, Integer> skippedCounts() {
        return immutableMap(skippedCounts);
    }

    @Override
    public List<NotificationControlledSendSkippedDetailVO> skippedDetails() {
        return immutableList(skippedDetails);
    }

    @Override
    public Map<NotificationChannel, NotificationChannelAvailability> channelAvailability() {
        return immutableMap(channelAvailability);
    }

    @Override
    public Map<NotificationChannel, NotificationControlledSendTemplateVO> templates() {
        return immutableMap(templates);
    }

    @Override
    public List<NotificationControlledSendSampleVO> samples() {
        return immutableList(samples);
    }

    /**
     * 转换、解析或规范化数据（{@code immutableMap}）。
     */
    private static <K, V> Map<K, V> immutableMap(Map<K, V> source) {
        return source == null || source.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    /**
     * 转换、解析或规范化数据（{@code immutableList}）。
     */
    private static <T> List<T> immutableList(List<T> source) {
        return source == null || source.isEmpty()
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(source));
    }
}
