package com.devops00.spectra.ai.starter.middleware;


import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.middleware.AgentInput;
import io.agentscope.core.middleware.MiddlewareBase;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * 安全注入
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/8 14:02
 */
@Slf4j
public class SecurityMiddleware implements MiddlewareBase {

    @Override
    public Flux<AgentEvent> onAgent(Agent agent, AgentInput input, Function<AgentInput, Flux<AgentEvent>> next) {
        return next.apply(input)
                .doOnSubscribe(__ -> {
                    log.info("Agent {} started", agent.getAgentId());
                })
                .doOnComplete(() -> {
                    log.debug("onAgent 当前agent:{}", agent.getName());
                });
    }

}
