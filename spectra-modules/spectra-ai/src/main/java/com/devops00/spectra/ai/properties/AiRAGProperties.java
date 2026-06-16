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

    /// 单次请求 Embedding 接口的最大切片批次大小（防止厂商接口爆 400 错误）
    /// 默认设为 20，安全适配绝大多数国内厂商（如阿里限制 25）
    private int embeddingBatchSize = 20;

    /// 单个知识切片的最大 Token 数量（对应 splitter 的第一个参数）
    private int maxSegmentSize = 300;

    /// 相邻知识切片之间的重叠 Token 数量（对应 splitter 的第二个参数）
    private int maxOverlapSize = 30;

}
