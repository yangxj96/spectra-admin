package com.devops00.spectra.ai.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.javabean.user.from.UserPageFrom;
import com.devops00.spectra.core.javabean.user.vo.UserPageVO;
import com.devops00.spectra.core.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Ai对话控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/4/26 10:35
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/ask")
public class AiAskController {

    private final ChatClient chatClient;

    private final UserService userService;

    @PreAuthorize("permitAll()")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody Map<String, String> req) throws IllegalAccessException {
        String msg = req.get("message");

        PageFrom page = new PageFrom();
        page.setPageNum(1L);
        page.setPageSize(1000L);
        UserPageFrom parms = new UserPageFrom();
        IPage<UserPageVO> users = userService.page(page, parms);

        String chatId = "chatcmpl-" + System.currentTimeMillis();
        long created = System.currentTimeMillis() / 1000;

        return Flux.just(buildFirstSSE(chatId, created))
                .concatWith(chatClient.prompt()
                        .user("用户问题：" + msg + "\n\n用户数据：" + users)
                        .stream()
                        .content()
                        .map(content -> buildContentSSE(chatId, created, content)))
                .concatWith(Flux.just(buildDoneSSE(chatId, created)));
    }

    private String buildFirstSSE(String chatId, long created) {
        String json = String.format("""
                {"id":"%s","object":"chat.completion.chunk","created":%d,"model":"deepseek-chat","choices":[{"delta":{"role":"assistant","content":""},"index":0,"finish_reason":null}]}
                """, chatId, created);
        return json + "\n\n";
    }

    private String buildContentSSE(String chatId, long created, String content) {
        String escaped = escapeJson(content);
        String json = String.format("""
                {"id":"%s","object":"chat.completion.chunk","created":%d,"model":"deepseek-chat","choices":[{"delta":{"content":"%s"},"index":0,"finish_reason":null}]}
                """, chatId, created, escaped);
        return json + "\n\n";
    }

    private String buildDoneSSE(String chatId, long created) {
        String json = String.format("""
                {"id":"%s","object":"chat.completion.chunk","created":%d,"model":"deepseek-chat","choices":[{"delta":{},"index":0,"finish_reason":"stop"}]}
                """, chatId, created);
        return json + "\n\n" + "data: [DONE]\n\n";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> req) throws IllegalAccessException {

        String msg = req.get("message");

        // 👉 这里先“手动控制数据”（最小实现核心）
        PageFrom page = new PageFrom();
        page.setPageNum(1L);
        page.setPageSize(1000L);
        UserPageFrom parms = new UserPageFrom();
        IPage<UserPageVO> users = userService.page(page, parms);

        return chatClient.prompt()
                .system("""
                            你是企业数据分析助手：
                            1. 必须基于提供的数据回答
                            2. 不允许编造数据
                            3. 用简洁中文回答
                        """)
                .user("用户问题：" + msg + "\n\n用户数据：" + users)
                .call()
                .content();
    }

}
