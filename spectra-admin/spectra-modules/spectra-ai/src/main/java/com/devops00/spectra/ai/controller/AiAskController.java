package com.devops00.spectra.ai.controller;


import com.devops00.spectra.ai.javabean.form.AiAskForm;
import com.devops00.spectra.ai.starter.dto.OpenAIStreamVO;
import com.devops00.spectra.ai.starter.response.OpenAiResponse;
import com.devops00.spectra.ai.starter.utils.AiUtils;
import com.devops00.spectra.security.base.holder.SecUtil;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

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

    private final ReActAgent agent;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OpenAIStreamVO> stream(@RequestBody AiAskForm form, @AuthenticationPrincipal UserDetails userDetails) {
        var msg = AiUtils.generateUserMsg("当前和你对话的用户是:" + userDetails.getUsername() + "\n" + form.getMessage());

        // 生成唯一的响应ID和当前时间戳，保证整个流返回中这两个字段保持一致
        var responseId = "chatcmpl-" + UUID.randomUUID().toString().replace("-", "");
        long createdTime = System.currentTimeMillis() / 1000;

        RuntimeContext.Builder builder = RuntimeContext
                .builder();
        var uid = SecUtil.getCurrentUserId();
        if (uid != null) {
            builder.userId(uid.toString());
        }

        var ctx = builder.build();
        String modelName = agent.getModel().getModelName();

        return agent.streamEvents(msg, ctx)
                .map(event -> OpenAiResponse.convertToStreamVO(modelName, event, responseId, createdTime))
                .onErrorResume(error -> OpenAiResponse.handleStreamError(modelName, error, responseId, createdTime));
    }

}
