package com.devops00.spectra.ai.configuration;

import com.devops00.spectra.ai.base.AiToolMarker;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssistantFactory {

    private final ChatModel chatModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    private final List<AiToolMarker> availableTools;

    public AssistantFactory(ChatModel chatModel,
                            EmbeddingStore<TextSegment> embeddingStore,
                            EmbeddingModel embeddingModel,
                            List<AiToolMarker> availableTools) {
        this.chatModel = chatModel;
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.availableTools = availableTools;
    }

    /**
     * 动态创建一个具备 RAG 能力的智能体
     */
    public DeepSeekAssistant createInstance() {
        // 1. 构建知识库检索器：提问时自动去你那个独立的 Schema 里翻书
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)   // 每次最多捞 3 条最相关的知识碎块塞给 DeepSeek
                .minScore(0.68)  // 过滤相似度太低的无关数据
                .build();



        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10) // 自动记住最近 10 条上下文
                .build();

        // 2. 组装并返回智能体实例
        return AiServices.builder(DeepSeekAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(availableTools)
                .contentRetriever(contentRetriever) // 挂载知识库
                .build();
    }
}