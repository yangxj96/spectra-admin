package com.devops00.spectra.ai.configuration;


import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.Toolkit;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/// AI配置类
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/5/15 17:15
@Component
public class AiConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private final Toolkit toolkit;

    public AiConfiguration(Toolkit toolkit) {
        this.toolkit = toolkit;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        // 遍历容器中所有的 Bean
        for (String beanName : ctx.getBeanDefinitionNames()) {
            Object bean = ctx.getBean(beanName);
            // 检查 Bean 的类及其方法上是否有 @Tool 注解
            if (hasToolMethod(bean.getClass())) {
                toolkit.registerTool(bean);
            }
        }
    }

    // 辅助方法：通过反射检查类的方法上是否有 @Tool 注解
    private boolean hasToolMethod(Class<?> clazz) {
        for (var method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                return true;
            }
        }
        return false;
    }
}
