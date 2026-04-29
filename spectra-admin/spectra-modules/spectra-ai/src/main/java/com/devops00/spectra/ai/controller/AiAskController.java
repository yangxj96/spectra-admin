package com.devops00.spectra.ai.controller;


import com.devops00.spectra.ai.configution.Assistant;
import com.devops00.spectra.ai.configution.AssistantStream;
import com.devops00.spectra.ai.javabean.form.AiAskForm;
import com.devops00.spectra.ai.utils.AiMessageUtils;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final Assistant assistant;

    private final AssistantStream assistantStream;

    @PostMapping("/chat")
    public String chat(@RequestBody AiAskForm form) {
        return assistant.chat("用户问题：" + form.getMessage());
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody AiAskForm form) {
        String chatId = "chatcmpl-" + System.currentTimeMillis();
        long created = System.currentTimeMillis() / 1000;

        return Flux.create(sink -> {
            // ① 开始
            sink.next(AiMessageUtils.buildFirstSSE(chatId, created));
            TokenStream tokenStream = assistantStream.chat("用户问题：" + form.getMessage());
            tokenStream
                    .onPartialResponse(token -> {
                        // ② 每个 token
                        sink.next(AiMessageUtils.buildContentSSE(chatId, created, token));
                    })
                    .onCompleteResponse(resp -> {
                        // ③ 结束
                        sink.next(AiMessageUtils.buildDoneSSE(chatId, created));
                        sink.complete();
                    })
                    .onError(sink::error)
                    .start();
        });
    }

}
