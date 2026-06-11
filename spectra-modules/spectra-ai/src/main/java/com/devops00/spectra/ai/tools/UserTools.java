package com.devops00.spectra.ai.tools;


import com.devops00.spectra.ai.base.AiToolMarker;
import com.devops00.spectra.ai.base.ToolExecutor;
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
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/9 16:36
 */
@Slf4j
@Component
public class UserTools implements AiToolMarker {

    /// 获取当前日期和时间，返回标准的 ISO 8601 格式字符串
    ///
    /// @param token 当前请求token
    @Tool("获取当前日期和时间，返回标准的 ISO 8601 格式字符串")
    public String getCurrentDateTimeISO(@ToolMemoryId String token) {
        return ToolExecutor.execute(token, _ -> {
            String zoneId = SecUtil.getCurrentUserZoneId();
            log.debug("{}当前用户时区:{}", LogPrefix.AI.p(), zoneId);
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));
            return now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        });

    }

}
