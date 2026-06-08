package com.devops00.spectra.ai.starter.middleware;


import com.devops00.spectra.common.constant.LogPrefix;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.middleware.AgentInput;
import io.agentscope.core.middleware.MiddlewareBase;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * 日志中间件
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/8 14:14
 */
@Slf4j
public class LoggerMiddleware implements MiddlewareBase {

    @Override
    public Flux<AgentEvent> onAgent(Agent agent, AgentInput input, Function<AgentInput, Flux<AgentEvent>> next) {
        return next.apply(input)
                .doOnComplete(() -> log.debug("{}日志中间件:{}", LogPrefix.AI.p(), agent.getName()));
    }

}
