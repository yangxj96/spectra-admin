/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.configure.mvc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * CORS配置化
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@Data
@ConfigurationProperties(prefix = "spectra.system.cors")
public class CorsProperties {

    /**
     * 指定的路径
     */
    private String mapping = "/**";

    /**
     * 指定允许的源
     */
    private List<String> originPatterns = Collections.singletonList("*");

    /**
     * 指定允许的方法
     */
    private List<String> methods = Collections.singletonList("*");

    /**
     * 指定运行的头信息
     */
    private List<String> headers = Collections.singletonList("*");

    /**
     * 是否支持凭证
     */
    private Boolean credentials = Boolean.TRUE;

    /**
     * 预检后缓存策略时长,单位为妙<br/>
     * 默认一小时
     */
    private Long maxAge = 3600L;
}
