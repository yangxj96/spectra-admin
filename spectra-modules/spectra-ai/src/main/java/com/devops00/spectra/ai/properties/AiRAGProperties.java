/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 权限配置相关内容
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/4 10:39
 */
@Data
@ConfigurationProperties(prefix = "spectra.ai.rag")
public class AiRAGProperties {

    /**
     * API KEY
     */
    private String apiKey;

    /**
     * 访问地址
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 单次请求 Embedding 接口的最大切片批次大小（防止厂商接口爆 400 错误）
     * 默认设为 20，安全适配绝大多数国内厂商（如阿里限制 25）
     */
    private int embeddingBatchSize = 20;

    /**
     * 单个知识切片的最大 Token 数量（对应 splitter 的第一个参数）
     */
    private int maxSegmentSize = 300;

    /**
     * 相邻知识切片之间的重叠 Token 数量（对应 splitter 的第二个参数）
     */
    private int maxOverlapSize = 30;
}
