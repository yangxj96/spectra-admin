package com.devops00.spectra.ai.starter.tools;


import com.devops00.spectra.common.constant.LogPrefix;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 测试用的工具
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/8 11:55
 */
@Slf4j
@Component
public class TestTools {

    @Tool(name = "get_current_time",
            description = "Returns the current time in a given IANA timezone.",
            readOnly = true)
    public String getCurrentTime(
            @ToolParam(name = "timezone", description = "IANA timezone, e.g. Asia/Shanghai")
            String timezone,
            String userId) {
        log.info("{}获得注入的user id: {}", LogPrefix.AI.p(), userId);
        return LocalDateTime.now(ZoneId.of(timezone)).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
