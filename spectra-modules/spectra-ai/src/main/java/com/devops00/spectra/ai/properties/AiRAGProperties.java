package com.devops00.spectra.ai.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/// 权限配置相关内容
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/4 10:39
@Data
@ConfigurationProperties(prefix = "spectra.ai.rag")
public class AiRAGProperties {

    /// API KEY
    private String apiKey;

    /// 访问地址
    private String baseUrl;

    /// 模型名称
    private String modelName;


}
