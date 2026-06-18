package com.devops00.spectra.ai.configuration;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 *
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/9 17:53
 */
public interface DeepSeekAssistant {

    /**
     * 场景一：标准的同步阻塞对话
     */
    @SystemMessage("你是一个全能的开发助手。")
    String chat(@MemoryId String token, String message);

    /**
     * 场景二：高级流式输出（打字机效果）
     * 返回 TokenStream 是 LangChain4j 流式响应的标准抽象
     */
    @SystemMessage("你是一个全能的开发助手。")
    TokenStream stream(@MemoryId String token, @UserMessage String message);

}
