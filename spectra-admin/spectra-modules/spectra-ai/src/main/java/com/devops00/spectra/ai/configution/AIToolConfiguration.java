package com.devops00.spectra.ai.configution;


import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// AI TOOL注解配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/29 09:21
@Slf4j
@Configuration
public class AIToolConfiguration {

    @Bean
    public List<Object> langChainTools(ApplicationContext context) {
        List<Object> tools = new ArrayList<>();
        for (Object bean : context.getBeansOfType(Object.class).values()) {
            Class<?> clazz = AopUtils.getTargetClass(bean);
            boolean hasTool = Arrays.stream(clazz.getDeclaredMethods())
                    .anyMatch(m -> AnnotatedElementUtils.hasAnnotation(m, Tool.class));
            if (hasTool) {
                log.info("注册LangChain4j Tool: {}", clazz.getName());
                tools.add(bean);
            }
        }
        return tools;
    }

}
