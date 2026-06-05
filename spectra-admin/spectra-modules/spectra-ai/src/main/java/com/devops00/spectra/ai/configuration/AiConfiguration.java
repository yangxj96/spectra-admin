package com.devops00.spectra.ai.configuration;


import com.devops00.spectra.ai.service.AiSessionService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.session.Session;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// AI配置类
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/5/15 17:15
@Configuration
@RequiredArgsConstructor
public class AiConfiguration {

    /**
     * ai session存储
     *
     * @return {@link Session}
     */
    @Bean
    public Session aiSession(AiSessionService service) {
        return new PostgreSQLSession(service);
    }

    /**
     * 模型信息
     *
     * @return 模型
     */
    @Bean
    public Model model() {
        return OpenAIChatModel
                .builder()
                .apiKey("sk-c73be100f54240208a7437238d7afe61")
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-pro")
                .build();
    }

    /**
     * 智能体agent
     *
     * @return agent
     */
    @Bean
    public HarnessAgent agent(Model model, Session session) {
        return HarnessAgent.builder()
                .name("Assistant")
                .sysPrompt("你是企业数据分析助手:\\n1. 不允许编造数据")
                .maxIters(10)
                .model(model)
                .session(session)
                .build();
    }


}
