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

import com.devops00.spectra.core.system.javabean.from.ServiceMonitorHistoryFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorHistoryVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 服务监控总览和历史趋势查询用例。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@RequiredArgsConstructor
public class ServiceMonitorQueryService {

    private final ServiceMonitorServiceSupport support;

    public ServiceMonitorOverviewVO getOverview() {
        return support.getOverview();
    }

    public ServiceMonitorHistoryVO getHistory(ServiceMonitorHistoryFrom from) {
        return support.getHistory(from);
    }
}
