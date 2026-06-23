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

/// Ai相关配置
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/6/9 16:51
@Configuration
@RequiredArgsConstructor
public class AiConfiguration {

    private final ChatModel chatModel;

    private final StreamingChatModel streamingChatModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final EmbeddingModel embeddingModel;

    private final List<AiToolMarker> availableTools;

    /**
     * 动态创建一个具备 RAG 能力的智能体
     */
    @Bean
    public DeepSeekAssistant assistant() {
        // 构建知识库检索器：提问时自动去你那个独立的 Schema 里翻书
        var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                // 每次最多捞 3 条最相关的知识碎块塞给 DeepSeek
                .maxResults(3)
                // 过滤相似度太低的无关数据
                .minScore(0.68)
                .build();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                // 自动记住最近 10 条上下文
                .maxMessages(10)
                .build();

        // 组装并返回智能体实例
        return AiServices.builder(DeepSeekAssistant.class)
                // 挂载model
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                // 挂载内存记忆
                .chatMemoryProvider(chatMemoryProvider)
                // 挂载工具
                .tools(availableTools.toArray())
                // 挂载知识库
                .contentRetriever(contentRetriever)
                .build();
    }

}
