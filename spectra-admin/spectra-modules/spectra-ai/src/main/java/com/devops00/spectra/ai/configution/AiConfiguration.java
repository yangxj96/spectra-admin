package com.devops00.spectra.ai.configution;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// Ai相关配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/26 10:35
@Configuration
public class AiConfiguration {

    /// 构建一个chat客户端
    ///
    /// @param chatModel chat模型
    /// @return {@link ChatClient}
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

}