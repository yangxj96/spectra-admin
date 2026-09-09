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

package com.devops00.spectra.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * 光谱平台运行参数绑定。
 *
 * <p>属性来自 {@code spectra.system} 配置前缀，供 framework 的 Web 配置和 Core 的文件目录检查使用；
 * 该类只描述运行参数，不承载系统配置表中的业务 Key-Value。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/19 00:00
 */
@Data
@ConfigurationProperties(prefix = "spectra.system")
public class SystemProperties {

    /**
     * 文件存储根目录；上传文件及其临时/派生文件都以此目录作为默认父目录。
     */
    private String baseDir = "files";

    /**
     * 平台 Java 包前缀；用于仍需生成或匹配完整类名的少数技术场景，不能改变源码实际包名。
     * 以下位置仍包含固定类名，修改该属性不会替换这些源码引用：
     * <ol>
     * <li>com.devops00.spectra.framework.persistence.configuration.MyBatisPlusConfiguration</li>
     * <li>com.devops00.spectra.framework.web.advice.crypto.ResponseEncryptAdvice</li>
     * <li>com.devops00.spectra.framework.web.advice.crypto.ResponseModifyAdvice</li>
     * <li>com.devops00.spectra.launch.LaunchApplication</li>
     * </ol>
     */
    private String packagePrefix = "com.devops00.spectra";

    /**
     * MVC 运行参数，包括 API 版本请求头和默认版本号。
     */
    private SpectraMvc mvc = new SpectraMvc();

    /**
     * CORS 运行参数，包括允许的来源、方法、请求头和预检缓存策略。
     */
    private SpectraCors cors = new SpectraCors();

    /**
     * MVC 相关运行参数。
     */
    @Data
    public static class SpectraMvc {

        /**
         * 客户端传递 API 版本的请求头名称。
         */
        private String apiHeader = "Api-Version";

        /**
         * 未显式传递版本时使用的 API 契约版本号。
         */
        private String apiVersion = "1.0.0";
    }

    /**
     * CORS 相关运行参数。
     */
    @Data
    public static class SpectraCors {

        /**
         * 应用 CORS 规则的请求路径模式。
         */
        private String mapping = "/**";

        /**
         * 精确允许的 Origin 列表；空列表表示不启用跨源访问。
         */
        private List<String> originPatterns = Collections.emptyList();

        /**
         * 是否要求部署必须显式提供至少一个跨源 Origin。生产 Web 部署开启，纯同源/API 部署可关闭。
         */
        private boolean required;

        /**
         * 预检请求允许声明的 HTTP 方法列表。
         */
        private List<String> methods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

        /**
         * 跨源请求允许携带的请求头名称列表。
         */
        private List<String> headers = List.of("Accept", "Authorization", "Content-Type", "Api-Version", "X-Client-Type",
                "X-CSRF-Token", "X-XSRF-TOKEN", "X-Requested-With", "X-Spectra-Initialization-Token");

        /**
         * 是否允许跨源请求携带 Cookie 或其他凭证。
         */
        private Boolean credentials = Boolean.TRUE;

        /**
         * 浏览器缓存预检结果的时长，单位为秒；默认缓存一小时。
         */
        private Long maxAge = 3600L;
    }
}
