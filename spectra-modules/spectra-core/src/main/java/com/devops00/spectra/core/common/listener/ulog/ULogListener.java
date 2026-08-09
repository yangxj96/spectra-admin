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

package com.devops00.spectra.core.common.listener.ulog;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.core.system.javabean.entity.OperationLog;
import com.devops00.spectra.core.system.service.OperationLogService;
import com.devops00.spectra.log.base.entity.ULogEntity;
import com.devops00.spectra.log.base.utils.AuditLogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;

import java.util.List;
import java.util.Map;

/**
 * 日志消息监听器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/7/3 00:00
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ULogListener {

    private final OperationLogService logService;

    private final ObjectMapper om;

    @Async
    @EventListener
    public void handleLogEvent(ULogEntity entity) {
        log.debug("{}开始异步持久化日志...", LogPrefix.LOG.p());

        var datum = new OperationLog();
        BeanUtils.copyProperties(entity, datum);

        TypeFactory tf = om.getTypeFactory();

        // ==================== 1. 处理入参序列化 (安全守卫放宽至 10000) ====================
        if (entity.getArgs() != null) {
            try {
                String argsJson = om.writeValueAsString(entity.getArgs());
                List<Object> argList = om.readValue(argsJson, tf.constructCollectionType(List.class, Object.class));
                List<Object> sanitizedArgs = AuditLogSanitizer.sanitizeList(argList);
                String sanitizedArgsJson = om.writeValueAsString(sanitizedArgs);

                if (sanitizedArgsJson.length() > 10000) {
                    // 安全裁剪：动态计算长度，确保绝对不会越界崩溃
                    String safeSub = sanitizedArgsJson.substring(0, 10000);
                    datum.setArgs(Map.of("payload", "参数过长已自动截断", "raw_part", safeSub + "...(已截断)"));
                } else {
                    datum.setArgs(Map.of("payload", sanitizedArgs));
                }
            } catch (Exception ex) {
                log.warn("{}异步序列化入参失败: {}", LogPrefix.LOG.p(), ex.getMessage());
                datum.setArgs(Map.of("error", "入参序列化异常崩溃"));
            }
        }

        // ==================== 2. 处理出参序列化 (安全守卫放宽至 10000) ====================
        if (entity.getResult() != null) {
            try {
                String resultJson = om.writeValueAsString(entity.getResult());
                List<Object> resultList = null;
                Map<String, Object> resultMap = null;
                Object scalarResult = null;

                if (resultJson.startsWith("[")) {
                    List<Object> parsed = om.readValue(resultJson, tf.constructCollectionType(List.class, Object.class));
                    resultList = AuditLogSanitizer.sanitizeList(parsed);
                } else if (resultJson.startsWith("{")) {
                    Map<String, Object> parsed = om.readValue(resultJson, tf.constructMapType(Map.class, String.class, Object.class));
                    resultMap = AuditLogSanitizer.sanitizeMap(parsed);
                } else {
                    scalarResult = AuditLogSanitizer.sanitize(entity.getResult());
                }

                Object sanitizedResult = resultList != null ? resultList : resultMap != null ? resultMap : scalarResult;
                String sanitizedResultJson = om.writeValueAsString(sanitizedResult);

                if (sanitizedResultJson.length() > 10000) {
                    String safeSub = sanitizedResultJson.substring(0, 10000);
                    datum.setResult(Map.of("payload", "响应体过长已自动截断", "raw_part", safeSub + "...(已截断)"));
                } else if (resultList != null) {
                    datum.setResult(Map.of("data", resultList));
                } else if (resultMap != null) {
                    datum.setResult(resultMap);
                } else {
                    datum.setResult(Map.of("info", scalarResult));
                }
            } catch (Exception ex) {
                log.warn("{}异步序列化响应出参失败: {}", LogPrefix.LOG.p(), ex.getMessage());
                datum.setResult(Map.of("error", "响应体序列化异常崩溃", "raw_message", ex.getMessage()));
            }
        }

        datum.setCreatedBy(entity.getCurrentId());
        datum.setUpdatedBy(entity.getCurrentId());

        logService.save(datum);
    }
}
