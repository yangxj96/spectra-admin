package com.devops00.spectra.ai.starter;


import com.devops00.spectra.ai.starter.configuration.AgentScopeProxyConfiguration;
import com.devops00.spectra.ai.starter.configuration.AgentScopeToolRegistrar;
import com.devops00.spectra.ai.starter.configuration.MiddlewareConfiguration;
import com.devops00.spectra.ai.starter.properties.AiProperties;
import com.devops00.spectra.common.constant.LogPrefix;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.tool.Toolkit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ai自动化配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/8 10:51
 */
@Slf4j
@AutoConfiguration
@Import({
        AgentScopeToolRegistrar.class,
        AgentScopeProxyConfiguration.class,
        MiddlewareConfiguration.class
})
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfiguration {

    private final AiProperties properties;

    private final List<MiddlewareBase> middlewares;


    public AiAutoConfiguration(AiProperties properties, ObjectProvider<MiddlewareBase> middlewares) {
        this.properties = properties;
        this.middlewares = middlewares.orderedStream().collect(Collectors.toList());
    }


    /// 模型信息
    ///
    /// @return 模型
    @Bean
    public Model model() {
        log.debug("{}配置Model", LogPrefix.AI.p());
        return OpenAIChatModel
                .builder()
                .apiKey(properties.getApiKey())
                .baseUrl(properties.getBaseUrl())
                .modelName(properties.getModelName())
                .build();
    }

    /// 智能体agent
    ///
    /// @return 智能体
    @Bean
    public ReActAgent agent(Model model, Toolkit toolkit) {
        log.debug("{}配置Agent", LogPrefix.AI.p());
        return ReActAgent.builder()
                .name(properties.getName())
                .sysPrompt(properties.getPrompt())
                .maxIters(10)
                .model(model)
                .toolkit(toolkit)
                .middlewares(middlewares)
                .build();
    }

}
