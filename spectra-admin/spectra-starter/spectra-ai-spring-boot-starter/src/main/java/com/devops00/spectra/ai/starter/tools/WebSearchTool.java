//package com.devops00.spectra.ai.starter.tools;
//
//
//import io.agentscope.core.agent.RuntimeContext;
//import io.agentscope.core.message.TextBlock;
//import io.agentscope.core.message.ToolResultBlock;
//import io.agentscope.core.tool.ToolBase;
//import io.agentscope.core.tool.ToolCallParam;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.context.SecurityContextHolder;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//import java.util.Map;
//
///**
// *
// *
// * @author Jack Young
// * @version 1.0
// * @since 2026/6/8 15:07
// */
//@Slf4j
//public class WebSearchTool extends ToolBase {
//
//    public WebSearchTool() {
//        super(
//                ToolBase.builder()
//                        .name("get_current_time")
//                        .description("获取当前系统时间")
//                        .inputSchema(Map.of(
//                                "type", "object",
//                                "properties", Map.of(),
//                                "required", List.of()
//                        ))
//                        .readOnly(true)
//                        .concurrencySafe(true));
//    }
//
//    @Override
//    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
//        return Mono.fromSupplier(() -> "当前系统时间：" + java.time.LocalDateTime.now())
//                .doOnSubscribe(_ -> {
//                    RuntimeContext context = param.getRuntimeContext();
//                    log.debug("当前用户:{}", context.getUserId());
//
//                })
//                .map(text ->
//                        ToolResultBlock.builder()
//                                .id("1")
//                                .name(getName())
//                                .output(List.of(TextBlock.builder().text(text).build()))
//                                .build());
//    }
//
//}
