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

package com.devops00.spectra.ai.configuration;

import com.devops00.spectra.ai.base.AiToolMarker;
import com.devops00.spectra.ai.store.PostgresChatMemoryStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Ai相关配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/9 16:51
 */
@Configuration
@RequiredArgsConstructor
public class AiConfiguration {

    private final ChatModel chatModel;

    private final StreamingChatModel streamingChatModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final EmbeddingModel embeddingModel;

    private final List<AiToolMarker> availableTools;

    private final PostgresChatMemoryStore chatMemoryStore;

    /**
     * 动态创建一个具备 RAG 能力的智能体
     */
    @Bean
    public SpectraAssistant assistant() {
        var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.68)
                .build();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();

        // 组装并返回智能体实例
        return AiServices.builder(SpectraAssistant.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> """
                        你是 Spectra 平台的 AI 助手。

                        ## 基本规则
                        - 始终使用简体中文回答
                        - 回答简洁直接，避免冗余

                        ## 能力边界
                        - 资料查询通过 Tool 工具和 RAG 知识库检索完成，不要编造数据
                        - 所有数据查询均在当前用户的权限范围内执行
                        - 无法获取的信息如实告知用户，不要猜测或虚构
                        """)
                .tools(availableTools.toArray())
                .contentRetriever(contentRetriever)
                .build();
    }
}
