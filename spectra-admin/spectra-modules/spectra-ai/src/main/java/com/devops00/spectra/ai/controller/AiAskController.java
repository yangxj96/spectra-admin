package com.devops00.spectra.ai.controller;


import com.devops00.spectra.ai.configuration.DeepSeekAssistant;
import com.devops00.spectra.ai.javabean.form.AiAskForm;
import com.devops00.spectra.ai.javabean.vo.OpenAIStreamVO;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

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

    private final DeepSeekAssistant assistant;

    private final ObjectMapper om;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody AiAskForm form) {
        String streamId = "chatcmpl-" + java.util.UUID.randomUUID().toString().replace("-", "");
        // 利用 Sinks 桥接 LangChain4j 的异步回调流和 Spring WebFlux 的 Flux 流
        return Flux.create(sink -> {
            assistant.stream(SecUtil.getCurrentUserId().toString(), form.getMessage())
                    // 1. 每当 DeepSeek 吐出一个字（增量文本）
                    .onNext(token -> {
                        OpenAIStreamVO vo = buildOpenAIDelta(streamId, token, null);
                        sink.next(om.writeValueAsString(vo));
                    })
                    // 2. 整个流顺利结束时
                    .onComplete(response -> {
                        // 按照 OpenAI 标准，流结束时通常会发一个带 finish_reason="stop" 的空内容包
                        String finishReason = "stop";
                        if (response != null && response.finishReason() != null) {
                            finishReason = response.finishReason().name().toLowerCase();
                        }

                        OpenAIStreamVO finalVo = buildOpenAIDelta(streamId, null, finishReason);
                        sink.next(om.writeValueAsString(finalVo));
                        // 真正关闭 Flux 通道
                        sink.complete();
                    })
                    // 3. 异常处理
                    .onError(sink::error)
                    // 4. 驱动 LangChain4j 的底层 OkHttp 异步线程开始传输
                    .start();
        });
    }

    private OpenAIStreamVO buildOpenAIDelta(String id, String content, String finishReason) {
        OpenAIStreamVO vo = new OpenAIStreamVO();
        vo.setId(id);
        vo.setObject("chat.completion.chunk"); // OpenAI 流式报文的固定对象类型
        vo.setModel("deepseek-chat");
        vo.setCreated(System.currentTimeMillis() / 1000);

        List<OpenAIStreamVO.Choice> choices = new ArrayList<>();
        OpenAIStreamVO.Choice choice = new OpenAIStreamVO.Choice();
        choice.setIndex(0);

        // 组装 Delta
        OpenAIStreamVO.Delta delta = new OpenAIStreamVO.Delta();
        if (content != null) {
            delta.setContent(content);
            // 只有当有内容且是第一包或常规包时设置，一般流式可以默认一直带角色，或者在第一包带角色
            delta.setRole("assistant");
        }
        choice.setDelta(delta);

        // 设置结束状态
        if (finishReason != null) {
            choice.setFinish_reason(finishReason);
        }

        choices.add(choice);
        vo.setChoices(choices);
        return vo;
    }
}
