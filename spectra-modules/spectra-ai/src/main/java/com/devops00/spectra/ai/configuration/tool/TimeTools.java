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

package com.devops00.spectra.ai.configuration.tool;

import com.devops00.spectra.ai.base.AiToolMarker;
import com.devops00.spectra.ai.base.ToolExecutor;
import com.devops00.spectra.ai.base.AiMemoryId;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.security.base.holder.SecUtil;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 用户工具类
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/9 16:36
 */
@Slf4j
@Component
public class TimeTools implements AiToolMarker {

    /**
     * 获取当前日期和时间，返回标准的 ISO 8601 格式字符串
     *
     * @param memoryId
     *            复合记忆标识
     */
    @Tool("获取当前日期和时间，返回标准的ISO 8601格式字符串")
    public String getCurrentDateTimeISO(@ToolMemoryId AiMemoryId memoryId) {
        return ToolExecutor.execute(memoryId.token(), _ -> {
            String zoneId = SecUtil.getCurrentUserZoneId();
            log.debug("{}当前用户时区:{}", LogPrefix.AI.p(), zoneId);
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));
            return now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        });
    }
}
