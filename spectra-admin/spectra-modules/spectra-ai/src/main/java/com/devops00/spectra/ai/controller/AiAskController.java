package com.devops00.spectra.ai.controller;


import com.devops00.spectra.ai.javabean.form.AiAskForm;
import com.devops00.spectra.ai.javabean.vo.OpenAIStreamVO;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.tool.AgentTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
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
    public Flux<OpenAIStreamVO> stream(@RequestBody AiAskForm form) {
        // AgentTool tree = agent.getToolkit().getTool("get_dept_tree");
        var msg = Msg
                .builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(form.getMessage()).build())
                .build();

        // 生成唯一的响应ID和当前时间戳，保证整个流返回中这两个字段保持一致
        var responseId = "chatcmpl-" + UUID.randomUUID().toString().replace("-", "");
        long createdTime = System.currentTimeMillis() / 1000;

        return agent.stream(msg)
                .map(event -> {
                    var response = new OpenAIStreamVO();
                    response.setId(responseId);
                    response.setCreated(createdTime);
                    response.setModel("agentscope-java-agent");

                    var choice = new OpenAIStreamVO.Choice();
                    choice.setIndex(0);

                    var delta = new OpenAIStreamVO.Delta();
                    delta.setRole("assistant");

                    // 核心修改：使用 isLast() 判断是否为最后一块
                    if (event.isLast()) {
                        // 如果是最后一块，设置结束原因，并且不携带具体的文本内容
                        choice.setFinish_reason("stop");
                    } else {
                        // 如果不是最后一块，提取当前的增量文本内容
                        var textContent = event.getMessage().getContent().stream()
                                .filter(block -> block instanceof TextBlock)
                                .map(block -> ((TextBlock) block).getText())
                                .reduce("", String::concat);

                        delta.setContent(textContent);
                    }

                    choice.setDelta(delta);
                    var choices = new ArrayList<OpenAIStreamVO.Choice>();
                    choices.add(choice);
                    response.setChoices(choices);

                    return response;
                })
                .onErrorResume(error -> {
                    // 建议在这里打印具体的 error 日志，例如：log.error("Agent stream error", error);
                    log.error("Agent stream error", error);

                    var errorResponse = new OpenAIStreamVO();
                    errorResponse.setId(responseId);
                    errorResponse.setCreated(createdTime);
                    errorResponse.setModel("agentscope-java-agent");

                    var choice = new OpenAIStreamVO.Choice();
                    choice.setIndex(0);
                    choice.setFinish_reason("error"); // 标记为错误结束
                    choice.setDelta(new OpenAIStreamVO.Delta()); // 返回空的 delta

                    var choices = new ArrayList<OpenAIStreamVO.Choice>();
                    choices.add(choice);
                    errorResponse.setChoices(choices);

                    // 返回封装好的 VO 对象流
                    return Flux.just(errorResponse);
                });
    }

}
