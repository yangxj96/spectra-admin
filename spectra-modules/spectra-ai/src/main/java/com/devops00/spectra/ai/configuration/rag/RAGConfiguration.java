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

package com.devops00.spectra.ai.configuration.rag;

import com.devops00.spectra.ai.configuration.rag.store.PostgresEmbeddingStore;
import com.devops00.spectra.ai.properties.AiRAGProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import tools.jackson.databind.ObjectMapper;

/// RAG向量库配置
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/6/11 17:44
@EnableAsync
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiRAGProperties.class)
public class RAGConfiguration {

    private final AiRAGProperties properties;

    /// 配置向量存储器
    ///
    /// @param template JDBC连接
    /// @param om       Jackson序列化
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(JdbcTemplate template, ObjectMapper om) {
        return new PostgresEmbeddingStore(template, om);
    }

    /// 配置向量化（Embedding）模型：用于将文本片段及用户提问翻译成数学向量。
    ///
    /// 这里以阿里 DashScope 为例（组合拳模式：阿里生成向量，DeepSeek 负责聊天）。
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                // 阿里的标准 OpenAI 兼容端点
                .baseUrl(properties.getBaseUrl())
                // 填入你的阿里 DashScope API_KEY
                .apiKey(properties.getApiKey())
                // 同样选用通用的 text-embedding-v2
                .modelName(properties.getModelName())
                .build();
    }
}
