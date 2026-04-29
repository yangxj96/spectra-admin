package com.devops00.spectra.ai.configution;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface Assistant {

    @SystemMessage("""
            你是企业数据分析助手：
            1. 不允许编造数据
            2. 需要分析的数据使用提供的tool获取
            """)
    String chat(String message);

}