package com.devops00.spectra.ai.starter.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ai相关配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/8 10:52
 */
@Data
@ConfigurationProperties(prefix = "spectra.ai")
public class AiProperties {

    /// Key
    private String apiKey;

    /// base地址
    private String baseUrl;

    /// 模型名称
    private String modelName;

    /// AI提示词
    private String prompt;

    /// 智能体名称
    private String name;
}
