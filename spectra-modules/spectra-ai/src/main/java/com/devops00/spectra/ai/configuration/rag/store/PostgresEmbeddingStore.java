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

package com.devops00.spectra.ai.configuration.rag.store;


import com.devops00.spectra.common.constant.LogPrefix;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/// PGSQL向量数据库存储操作
///
/// 配置向量存储器：绑定到 PgSQL 独立的 ai_rag schema 中
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/6/11 17:49
@Slf4j
public class PostgresEmbeddingStore implements EmbeddingStore<TextSegment> {

    // 可扩展点：后续若需动态切表，可将以下常量改为动态配置或方法入参
    private static final String SCHEMA_TABLE = "spectra_rag.ai_knowledge_chunks";
    private static final String VECTOR_TYPE = "::spectra_rag.vector";
    private static final String VECTOR_OP = "OPERATOR(spectra_rag.<=>)";
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PostgresEmbeddingStore(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    // ==================== 1. 数据写入端 (Ingestion) ====================

    @Override
    public String add(Embedding embedding) {
        List<String> ids = addAll(List.of(embedding), null);
        return ids.isEmpty() ? null : ids.getFirst();
    }

    @Override
    public void add(String id, Embedding embedding) {
        throw new UnsupportedOperationException("Spectra RAG 规范：请使用带 TextSegment 的 add(Embedding, TextSegment) 方法以完整保存知识文本");
    }

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        List<String> ids = addAll(List.of(embedding), List.of(textSegment));
        return ids.isEmpty() ? null : ids.getFirst();
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        return addAll(embeddings, null);
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> textSegments) {
        List<String> generatedIds = new ArrayList<>();

        // 拼接完全对齐的 Upsert 核心 SQL
        String sql = String.format(
                "INSERT INTO %s (embedding_id, text, embedding, metadata) VALUES (?, ?, ?%s, ?::jsonb) " +
                        "ON CONFLICT (embedding_id) DO UPDATE SET text = EXCLUDED.text, embedding = EXCLUDED.embedding, metadata = EXCLUDED.metadata",
                SCHEMA_TABLE, VECTOR_TYPE
        );

        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 0; i < embeddings.size(); i++) {
            Embedding embedding = embeddings.get(i);
            TextSegment segment = (textSegments != null && textSegments.size() > i) ? textSegments.get(i) : null;

            // 🌟 保持 UUIDv7 派发自控权：后续可在此处无缝替换为你的分布式递增主键逻辑
            UUID uuidV7 = UUID.randomUUID();
            generatedIds.add(uuidV7.toString());

            String contentText = (segment != null) ? segment.text() : "";
            String jsonMetadata = "{}";
            try {
                if (segment != null && segment.metadata() != null) {
                    jsonMetadata = objectMapper.writeValueAsString(segment.metadata().toMap());
                }
            } catch (Exception ignored) {
                // 后续扩展：可以引入你的 Slf4j 进行错误日志审计
            }

            batchArgs.add(new Object[]{
                    uuidV7,
                    contentText,
                    embedding.vectorAsList().toString(),
                    jsonMetadata
            });
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
        return generatedIds;
    }

    // ==================== 2. 数据检索端 (Search) ====================

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        log.debug("{}向量数据库搜索了:{}", LogPrefix.AI.p(), request.query());
        Embedding queryEmbedding = request.queryEmbedding();
        int maxResults = request.maxResults();
        double minScore = request.minScore();

        // 拼接带有 Schema 限定操作符的检索 SQL，杜绝 operator does not exist 隐患
        String sql = String.format(
                "SELECT embedding_id, text, metadata, (1 - (embedding %s ?%s)) AS score " +
                        "FROM %s " +
                        "WHERE (1 - (embedding %s ?%s)) >= ? " +
                        "ORDER BY embedding %s ?%s ASC " +
                        "LIMIT ?",
                VECTOR_OP, VECTOR_TYPE, SCHEMA_TABLE, VECTOR_OP, VECTOR_TYPE, VECTOR_OP, VECTOR_TYPE
        );

        String vectorStr = queryEmbedding.vectorAsList().toString();

        List<EmbeddingMatch<TextSegment>> matches = jdbcTemplate.query(sql, (rs, _) -> {
                    String embeddingId = rs.getString("embedding_id");
                    String text = rs.getString("text");
                    double score = rs.getDouble("score");

                    // 后续扩展点：如果你的 metadata 里存了别的信息，可以进一步还原到 TextSegment 中
                    TextSegment segment = TextSegment.from(text);

                    return new EmbeddingMatch<>(score, embeddingId, queryEmbedding, segment);
                },
                vectorStr, vectorStr, minScore, vectorStr, maxResults);

        return new EmbeddingSearchResult<>(matches);
    }
}
