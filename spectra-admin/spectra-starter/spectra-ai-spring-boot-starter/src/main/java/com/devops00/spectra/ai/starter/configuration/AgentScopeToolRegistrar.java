package com.devops00.spectra.ai.starter.configuration;

import com.devops00.spectra.common.constant.LogPrefix;
import io.agentscope.core.tool.Toolkit;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class AgentScopeToolRegistrar implements ApplicationListener<ContextRefreshedEvent> {

    private final ApplicationContext applicationContext;
    private final Toolkit toolkit;

    public AgentScopeToolRegistrar(ApplicationContext applicationContext, Toolkit toolkit) {
        this.applicationContext = applicationContext;
        this.toolkit = toolkit;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 确保只在 Root 上下文刷新时执行一次（防止 MVC 子上下文重复触发）
        if (event.getApplicationContext().getParent() != null) {
            return;
        }

        log.debug("{}开始安全扫描 @Tool 工具类...", LogPrefix.AI.p());

        // 1. 获取容器中所有被 @Component（或 @Service, @RestController 等）标记的 Bean
        Map<String, Object> allComponents = applicationContext.getBeansWithAnnotation(Component.class);

        for (Map.Entry<String, Object> entry : allComponents.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();

            // 排除自身
            if (bean instanceof AgentScopeToolRegistrar || bean instanceof Toolkit) {
                continue;
            }

            Class<?> targetClass = AopUtils.getTargetClass(bean);

            // 2. 检查类中是否包含任何含有 @Tool 注解的方法
            boolean hasTool = false;
            for (Method m : targetClass.getMethods()) {
                if (m.isAnnotationPresent(io.agentscope.core.tool.Tool.class)) {
                    hasTool = true;
                    break;
                }
            }

            if (hasTool) {
                // 3. 制作 CGLIB 空壳代理，保留原始注解
                ProxyFactory proxyFactory = new ProxyFactory();
                proxyFactory.setTargetClass(targetClass);
                proxyFactory.setProxyTargetClass(true); // 强行启用 CGLIB 子类代理

                proxyFactory.addAdvice((MethodInterceptor) invocation -> {
                    // 运行时动态去 Spring 容器拿真正的、带依赖、带 AOP 的真实 Bean 实例
                    Object realSpringBean = applicationContext.getBean(beanName);
                    return invocation.getMethod().invoke(realSpringBean, invocation.getArguments());
                });

                Object proxyObject = proxyFactory.getProxy();

                // 4. 将保留了完整注解的“壳对象”注册进 AgentScope
                toolkit.registerTool(proxyObject);
                log.debug("{}成功无感代理并注册工具类:{}", LogPrefix.AI.p(), targetClass.getName());
            }
        }
        log.debug("{}工具类扫描与 Toolkit 动态装配全部完成！", LogPrefix.AI.p());
    }
}