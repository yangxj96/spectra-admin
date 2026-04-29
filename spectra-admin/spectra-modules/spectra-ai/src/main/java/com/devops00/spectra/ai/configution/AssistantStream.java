package com.devops00.spectra.ai.configution;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

/**
 * 流式客户端
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/4/29 16:30
 */
@AiService
public interface AssistantStream {

    @SystemMessage("""
                你是企业数据分析助手：
                1. 不允许编造数据
                2. 需要分析的数据使用提供的tool获取
            """)
    TokenStream chat(String message);

}
