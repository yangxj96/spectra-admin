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

package io.github.yangxj96.spectra.core.listener;

import io.github.yangxj96.spectra.core.service.system.OperationLogService;
import io.github.yangxj96.spectra.core.configure.ulog.entity.ULogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 日志消息监听器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/3
 */
@Slf4j
@Component
public class ULogListener {

    private static final String PREFIX = "[日志消息监听器]: ";

    private final OperationLogService logService;

    public ULogListener(OperationLogService logService) {
        this.logService = logService;
    }

    @Async("uLogTaskExecutor")
    @EventListener
    public void handleLogEvent(ULogEntity entity) {
        // TODO 更换SpringSecurity需要进行修改
        log.debug(PREFIX + "开始记录,{}", entity.toString());
        //var datum = new OperationLog();
        //BeanUtils.copyProperties(entity, datum);
        //if (StrUtils.isNotBlank(entity.getToken())) {
        //    try {
        //        var loginId = StpUtil.getLoginIdByToken(entity.getToken());
        //        if (loginId != null) {
        //            datum.setCreatedBy(Long.parseLong(loginId.toString()));
        //            datum.setUpdatedBy(Long.parseLong(loginId.toString()));
        //        }
        //    } catch (Exception e) {
        //        log.error("获取登录用户ID失败", e);
        //    }
        //}
        //logService.save(datum);
    }

}
