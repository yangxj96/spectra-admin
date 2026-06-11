/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.ai.runner;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

/// 文件夹检查
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/7/28
@Slf4j
//@Component
public class RAGTestRunner implements ApplicationRunner {

    // 1. 注入你之前在配置类里定义好的 pgvector 存储器
    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    // 2. 注入你配置好的阿里 text-embedding-v2 模型
    @Resource
    private EmbeddingModel embeddingModel;

    @Override
    public void run(@Nullable ApplicationArguments args) {
        // 1. 准备一个本地固定的测试文件路径（可以先在 D 盘手建一个 test_knowledge.txt，里面随便写几段Spectra系统的介绍）
        Path filePath = Paths.get("D:\\rules.txt");

        // 2. 使用 LangChain4j 的文件加载器和 Tika 解析器读取文档
        DocumentParser argumentParser = new ApacheTikaDocumentParser();
        Document document = loadDocument(filePath, argumentParser);
        log.info("====== 1. 成功读取本地文档，字数：{}", document.text().length());

        // 3. 定义切片策略（这里设置每段最多 300 字，重叠 30 字，保证上下文不因切断而丢失）
        DocumentSplitter splitter = DocumentSplitters.recursive(300, 30);

        // 4. 构建一个标准流水线（Ingestor）
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)      // 怎么切
                .embeddingModel(embeddingModel)  // 用什么模型向量化
                .embeddingStore(embeddingStore)  // 存到哪个数据库
                .build();

        log.info("====== 2. 开始启动流水线：切片 -> 阿里向量化 -> 写入 PG 数据库 ======");

        // 5. 执行导入
        ingestor.ingest(document);

        log.info("====== 3. 恭喜！固定路径文件已成功存入 pgvector 向量数据库 ======");
    }
}
