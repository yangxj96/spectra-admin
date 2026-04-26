package com.devops00.spectra.ai.controller;


import com.devops00.spectra.ai.javabean.form.AiAskForm;
import com.devops00.spectra.ai.utils.AiMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/// Ai对话控制器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/26 10:35
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/ask")
public class AiAskController {

    private final ChatClient chatClient;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody AiAskForm form) {
        String chatId = "chatcmpl-" + System.currentTimeMillis();
        long created = System.currentTimeMillis() / 1000;
        return Flux.just(AiMessageUtils.buildFirstSSE(chatId, created))
                .concatWith(chatClient.prompt()
                        .user("用户问题：" + form.getMessage())
                        .stream()
                        .content()
                        .map(content -> AiMessageUtils.buildContentSSE(chatId, created, content)))
                .concatWith(Flux.just(AiMessageUtils.buildDoneSSE(chatId, created)));
    }

    @PostMapping("/chat")
    public String chat(@RequestBody AiAskForm form) {
        return chatClient.prompt()
                .system("""
                            你是企业数据分析助手：
                            1. 必须基于提供的数据回答
                            2. 不允许编造数据
                            3. 用简洁中文回答
                        """)
                .user("用户问题：" + form.getMessage())
                .call()
                .content();
    }

}
