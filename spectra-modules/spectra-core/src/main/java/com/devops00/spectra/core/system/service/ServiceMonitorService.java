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

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorOverviewVO;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorHistoryFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorHistoryVO;

/**
 * 服务器信息监控
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
public interface ServiceMonitorService {

    /**
     * 获取服务监控总览。
     *
     * @return 返回当前实例的 CPU、内存、磁盘和运行时监控总览；采集失败时抛出异常，不返回 null。
     */
    ServiceMonitorOverviewVO getOverview();

    /**
     * 查询服务监控历史趋势。
     *
     * @param from 查询条件
     * @return 返回指定时间范围内的服务监控历史趋势数据；没有采样点时返回空列表，不返回 null，查询失败时抛出异常。
     */
    ServiceMonitorHistoryVO getHistory(ServiceMonitorHistoryFrom from);
}
