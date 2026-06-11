package com.devops00.spectra.ai.tools;


import com.devops00.spectra.common.constant.LogPrefix;
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
public class UserTools {

    @Tool("获取当前日期和时间，返回标准的 ISO 8601 格式字符串")
    public String getCurrentDateTimeISO(@ToolMemoryId String userId) {
        log.info("{}DeepSeek 成功触发了本地 [getCurrentDateTime] 工具调用！", LogPrefix.AI.p());
        log.info("{}当前用户信息:{}", LogPrefix.AI.p(), userId);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        return now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
