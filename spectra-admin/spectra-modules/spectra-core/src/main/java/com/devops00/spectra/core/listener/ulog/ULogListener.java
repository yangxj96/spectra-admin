/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.core.listener.ulog;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.core.configure.ulog.entity.ULogEntity;
import com.devops00.spectra.core.javabean.system.entity.OperationLog;
import com.devops00.spectra.core.service.system.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/// 日志消息监听器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/7/3
@Slf4j
@Component
public class ULogListener {

    private final OperationLogService logService;

    public ULogListener(OperationLogService logService) {
        this.logService = logService;
    }

    @Async
    @EventListener
    public void handleLogEvent(ULogEntity entity) {
        log.debug("{}开始记录,{}", LogPrefix.LOG.p(), entity);
        var datum = new OperationLog();
        BeanUtils.copyProperties(entity, datum);
        datum.setCreatedBy(entity.getCurrentId());
        datum.setUpdatedBy(entity.getCurrentId());
        logService.save(datum);
    }

}
