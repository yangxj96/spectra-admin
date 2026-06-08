package com.devops00.spectra.ai.starter.configuration;

import com.devops00.spectra.common.constant.LogPrefix;
import io.agentscope.core.tool.Toolkit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@Slf4j
public class AgentScopeProxyConfiguration {

    @Bean
    public Toolkit toolkit() {
        log.debug("{}配置Toolkit", LogPrefix.AI.p());
        return new Toolkit();
    }

}