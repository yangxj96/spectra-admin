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

package com.devops00.spectra.ai.configuration.rag.listener;

import com.devops00.spectra.ai.configuration.rag.utils.SpectraDocumentParser;
import com.devops00.spectra.ai.properties.AiRAGProperties;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.event.FileUploadFinishEvent;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import com.devops00.spectra.upload.service.FileInfoService;
import com.devops00.spectra.upload.service.impl.FileUploadFacade;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * 文件上传完成监听
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/15 17:29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadFinishListener {

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final EmbeddingModel embeddingModel;

    private final FileInfoService fileInfoService;

    private final FileUploadFacade fileUploadFacade;

    private final AiRAGProperties properties;

    private final NotificationGateway notificationGateway;

    @Async
    @EventListener
    public void handleFileUploaded(FileUploadFinishEvent event) {
        log.info("{}开始为文件 [{}] 构建 RAG 索引...", LogPrefix.AI.p(), event.getFileId());
        // 获取文件信息
        FileInfo fileInfo = fileInfoService.getById(event.getFileId());
        if (fileInfo == null) {
            log.error("{}文件未找到", LogPrefix.AI.p());
            return;
        }
        // 解析器
        var parser = new SpectraDocumentParser();
        // 通过底层 Service 直接打开物理流（适配 Local / S3）
        // 这样可以规避 302 跳转问题，由底层的组件自己判断是从 S3 下载还是从本地磁盘读
        try (InputStream fileStream = fileUploadFacade.openStream(fileInfo)) {
            // 解析文档
            log.info("{}开始调用智能解析器处理：{}", LogPrefix.AI.p(), fileInfo.getOriginalName());
            Document document = parser.parse(fileStream, fileInfo.getOriginalName());
            log.info("{}成功读取文档，原始总字数：{}", LogPrefix.AI.p(), document.text().length());
            // 根据文件类型选择更合适的切片器
            var splitter = DocumentSplitters.recursive(properties.getMaxSegmentSize(), properties.getMaxOverlapSize());
            // 开始启动手工流水线：切片->分批向量化->写入pgvector
            log.info("{}开始切片", LogPrefix.AI.p());
            // 手动将文档切片
            List<TextSegment> segments = splitter.split(document);
            log.info("{}文档切片完成，总切片数: {}", LogPrefix.AI.p(), segments.size());
            // 定义厂商限制的单批次大小（要求小于25，我们定 20）
            int batchSize = properties.getEmbeddingBatchSize();
            int totalSegments = segments.size();
            for (int i = 0; i < totalSegments; i += batchSize) {
                // 动态截取当前批次的切片
                int end = Math.min(i + batchSize, totalSegments);
                List<TextSegment> batchSegments = segments.subList(i, end);
                log.info("{}正在处理第 {}/{} 个切片...", LogPrefix.AI.p(), end, totalSegments);
                // 调用模型，将当前批次的文本变成向量
                Response<List<Embedding>> embeddingResponse = embeddingModel.embedAll(batchSegments);
                List<Embedding> embeddings = embeddingResponse.content();
                // 将向量和文本对应，成对写入pgvector存储
                embeddingStore.addAll(embeddings, batchSegments);
                // 如果大模型频繁报 429 限流，可以在这里加一个几百毫秒的短暂睡眠
                // Thread.sleep(200);
            }
            log.info("{}恭喜！文件 [{}] 索引已成功灌入 PgVector", LogPrefix.AI.p(), fileInfo.getOriginalName());
            notifyIndexResult(fileInfo, true);
        } catch (Exception e) {
            log.error("{}文件 [{}] RAG 索引构建失败", LogPrefix.AI.p(), fileInfo.getId(), e);
            notifyIndexResult(fileInfo, false);
        }
    }

    private void notifyIndexResult(FileInfo fileInfo, boolean success) {
        if (fileInfo.getCreatedBy() == null) {
            return;
        }
        var title = success ? "知识库索引完成" : "知识库索引失败";
        var content = success
                ? "文件「" + fileInfo.getOriginalName() + "」已完成知识库索引。"
                : "文件「" + fileInfo.getOriginalName() + "」知识库索引失败，请稍后重试。";
        notificationGateway.enqueue(NotificationRequest.inApp(
                "ai:rag:index:" + fileInfo.getId() + ":" + (success ? "success" : "failure"),
                NotificationPurpose.SYSTEM_NOTICE,
                List.of(fileInfo.getCreatedBy()),
                "ai.rag.index",
                title,
                content,
                "AI",
                fileInfo.getId().toString(),
                "AI",
                null));
    }
}
