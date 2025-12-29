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

package io.github.yangxj96.spectra.core.configure.mvc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 文件相关配置
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/19
 */
@Data
@ConfigurationProperties(prefix = "spectra.system")
public class SpectraSystemProperties {

    /// 基础文件位置,所有文件都会在这个目录下面进行存放
    private String baseDir = "files";

    ///
    /// 指定包前缀,一部分地方在使用的时候**不得不固定写死代码**,
    /// 导致如果克隆代码后需要修改包名为自己公司或者自己使用的时候,
    /// 可以直接修改这个配置,在需要写死的地方会直接使用这里,
    /// 能使用拼接的位置都尽量进行了拼接,但是依旧会有一些位置无法拼接,则注明在下方列表,
    /// 以下为没法直接使用这个属性进行修改的位置,
    /// 1. io.github.yangxj96.spectra.framework.configure.MyBatisPlusConfiguration
    /// 2. io.github.yangxj96.spectra.framework.advice.ResponseBodyModifyAdvice
    /// 3. io.github.yangxj96.spectra.launch.LaunchApplication
    ///
    private String packagePrefix = "io.github.yangxj96.spectra";

    /// mvc配置
    private MvcProperties mvc;

    /// cors配置
    private CorsProperties cors;

    @Data
    public static class MvcProperties {

        /// api版本号请求头
        private String apiHeader = "Api-Version";

        /// 默认API版本号
        private String apiVersion = "1.0.0";

    }

    @Data
    public static class CorsProperties {

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

}
