package com.devops00.spectra.ai.configuration;


import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

/// Ai相关配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/6/9 16:51
@Configuration
@RequiredArgsConstructor
public class AiConfiguration {

    private final ObjectMapper om;

    // 对应非流式大模型的 Bean 名字
    public static final String CHAT_MODEL_HTTP_CLIENT_BUILDER = "openAiChatModelHttpClientBuilder";

    // 对应流式大模型的 Bean 名字（也就是这次引爆异常的背后黑手）
    public static final String STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER = "openAiStreamingChatModelHttpClientBuilder";

    /**
     * 封印非流式的有毒代码
     */
    @Bean(name = CHAT_MODEL_HTTP_CLIENT_BUILDER)
    public HttpClientBuilder openAiChatModelHttpClientBuilder() {
        // 使用 langchain4j-http-client-jdk 包提供的原生安全构建器
        return new JdkHttpClientBuilder();
    }

    /**
     * 封印流式的有毒代码
     * 抢先定义它，触发官方 Starter 上的 @ConditionalOnMissingBean，强行截断其内部对 RestClient 的调用
     */
    @Bean(name = STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    public HttpClientBuilder openAiStreamingChatModelHttpClientBuilder() {
        // 同样塞入安全的 JDK HttpClient 驱动
        return new JdkHttpClientBuilder();
    }

    /**
     * 直接向Spring容器注册一个全局的聊天记忆提供者 Bean
     * 这样Starter自己的AiServiceFactory就能在初始化时自动识别并注入它
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        // 每当有一个新的 memoryId (比如新的会话Token) 进来，就为它分配一个独立的、能记住最近 10 条对话的内存滑窗
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .build();
    }


}
