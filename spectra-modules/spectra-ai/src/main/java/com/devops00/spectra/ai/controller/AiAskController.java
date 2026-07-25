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

package com.devops00.spectra.ai.controller;


import com.devops00.spectra.ai.configuration.SpectraAssistant;
import com.devops00.spectra.ai.base.AiMemoryId;
import com.devops00.spectra.ai.javabean.from.AiAskFrom;
import com.devops00.spectra.ai.javabean.vo.OpenAIStreamVO;
import com.devops00.spectra.ai.service.AiConversationService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/// Ai对话控制器
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/26 10:35
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/ask")
public class AiAskController {

    private final SpectraAssistant assistant;

    private final AiConversationService conversationService;

    private final ObjectMapper om;

    @ULog("'AI对话流式问答'")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE, version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public Flux<String> stream(@Validated @RequestBody AiAskFrom from) {
        UUID conversationId = from.getConversationId();
        if (conversationId == null) {
            conversationId = conversationService.create(SecUtil.getCurrentUserId(), from.getMessage());
        }
        AiMemoryId memoryId = new AiMemoryId(conversationId.toString(), SecUtil.getCurrentToken());

        String streamId = "chatcmpl-" + java.util.UUID.randomUUID().toString().replace("-", "");
        final String convId = conversationId.toString();
        return Flux.create(sink -> {
            assistant.stream(memoryId, from.getMessage())
                    .onPartialResponse(token -> {
                        try {
                            OpenAIStreamVO vo = buildOpenAIDelta(streamId, token, null, convId);
                            sink.next(om.writeValueAsString(vo));
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    })
                    .onCompleteResponse(chatResponse -> {
                        try {
                            String finishReason = "stop";
                            if (chatResponse != null && chatResponse.finishReason() != null) {
                                finishReason = chatResponse.finishReason().name().toLowerCase();
                            }
                            OpenAIStreamVO finalVo = buildOpenAIDelta(streamId, null, finishReason, convId);
                            sink.next(om.writeValueAsString(finalVo));
                            sink.complete();
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    })
                    .onError(sink::error)
                    .start();
        });
    }

    private OpenAIStreamVO buildOpenAIDelta(String id, String content, String finishReason, String conversationId) {
        OpenAIStreamVO vo = new OpenAIStreamVO();
        vo.setId(id);
        vo.setObject("chat.completion.chunk");
        vo.setModel("deepseek-chat");
        vo.setCreated(System.currentTimeMillis() / 1000);
        vo.setConversationId(conversationId);

        List<OpenAIStreamVO.Choice> choices = new ArrayList<>();
        OpenAIStreamVO.Choice choice = new OpenAIStreamVO.Choice();
        choice.setIndex(0);

        OpenAIStreamVO.Delta delta = new OpenAIStreamVO.Delta();
        if (content != null) {
            delta.setContent(content);
            delta.setRole("assistant");
        }
        choice.setDelta(delta);

        if (finishReason != null) {
            choice.setFinish_reason(finishReason);
        }

        choices.add(choice);
        vo.setChoices(choices);
        return vo;
    }
}
