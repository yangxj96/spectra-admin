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

package com.devops00.spectra.core.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 服务监控快照采集和告警评估用例。 */
@Service
@RequiredArgsConstructor
public class ServiceMonitorEvaluationService {

    private final ServiceMonitorServiceSupport support;

    /** 手工采集失败时保留上一份快照。 */
    public void collectSnapshot() {
        support.collectSnapshot();
    }

    /** 调度采集失败时向调度器抛出异常。 */
    public void collectSnapshotForScheduler() {
        support.collectSnapshotForScheduler();
    }
}
