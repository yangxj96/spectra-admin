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


import com.devops00.spectra.ai.configuration.DeepSeekAssistant;
import com.devops00.spectra.ai.javabean.form.AiAskForm;
import com.devops00.spectra.ai.javabean.vo.OpenAIStreamVO;
import com.devops00.spectra.log.base.annotation.ULog;
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
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/26 10:35
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/ask")
public class AiAskController {

    private final DeepSeekAssistant assistant;

    private final ObjectMapper om;

    @ULog("'AI对话流式问答'")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE, version = "1.0.0+")
    public Flux<String> stream(@RequestBody AiAskForm form) {
        String streamId = "chatcmpl-" + java.util.UUID.randomUUID().toString().replace("-", "");
        // 利用 Sinks 桥接 LangChain4j 的异步回调流和 Spring WebFlux 的 Flux 流
        return Flux.create(sink -> {
            assistant.stream(SecUtil.getCurrentToken(), form.getMessage())
                    // 获取到响应的时候触发
                    .onPartialResponse(token -> {
                        try {
                            OpenAIStreamVO vo = buildOpenAIDelta(streamId, token, null);
                            sink.next(om.writeValueAsString(vo));
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    })
                    // 响应完成时触发
                    .onCompleteResponse(chatResponse -> {
                        try {
                            String finishReason = "stop";
                            // 从 1.0.0 的全新 ChatResponse 中提取结束原因
                            if (chatResponse != null && chatResponse.finishReason() != null) {
                                finishReason = chatResponse.finishReason().name().toLowerCase();
                            }
                            OpenAIStreamVO finalVo = buildOpenAIDelta(streamId, null, finishReason);
                            sink.next(om.writeValueAsString(finalVo));
                            // 真正关闭 Flux 通道
                            sink.complete();
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    })
                    // 异常处理
                    .onError(sink::error)
                    // 发送请求给大模型开始传输
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
