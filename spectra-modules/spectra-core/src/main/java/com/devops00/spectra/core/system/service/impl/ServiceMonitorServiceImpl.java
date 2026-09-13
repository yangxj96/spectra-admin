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

import com.devops00.spectra.core.system.service.ServiceMonitorService;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorHistoryFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorHistoryVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorOverviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 服务监控公开入口，只负责路由查询和快照采集两个用例组。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@Primary
public class ServiceMonitorServiceImpl implements ServiceMonitorService {

    private final ServiceMonitorEvaluationService evaluationService;

    private final ServiceMonitorQueryService queryService;

    @Autowired
    public ServiceMonitorServiceImpl(ServiceMonitorEvaluationService evaluationService,
                                     ServiceMonitorQueryService queryService) {
        this.evaluationService = evaluationService;
        this.queryService = queryService;
    }

    /** 调度器使用的快照采集入口。 */
    public void collectSnapshotForScheduler() {
        evaluationService.collectSnapshotForScheduler();
    }

    /** 手工或启动期采集快照入口。 */
    public void collectSnapshot() {
        evaluationService.collectSnapshot();
    }

    @Override
    public ServiceMonitorOverviewVO getOverview() {
        return queryService.getOverview();
    }

    @Override
    public ServiceMonitorHistoryVO getHistory(ServiceMonitorHistoryFrom from) {
        return queryService.getHistory(from);
    }
}
