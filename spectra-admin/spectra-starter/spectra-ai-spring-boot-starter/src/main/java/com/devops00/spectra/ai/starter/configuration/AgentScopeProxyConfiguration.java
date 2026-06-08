package com.devops00.spectra.ai.starter.configuration;

import io.agentscope.core.tool.Toolkit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentScopeProxyConfiguration {

    @Bean
    public Toolkit toolkit() {
        return new Toolkit();
    }

}