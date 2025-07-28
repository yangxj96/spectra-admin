/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
@Component
@ConfigurationProperties(prefix = "spectra.system")
public class SystemProperties {

    /**
     * 基础文件位置,所有文件都会在这个目录下面进行存放
     */
    private String baseDir = "files";

    /**
     * 指定包前缀,一部分地方在使用的时候不得不固定写死代码,<br/>
     * 导致如果克隆代码后需要修改包名为自己公司或者自己使用的时候,<br/>
     * 可以直接修改这个配置,在需要写死的地方会直接使用这里.<br/>
     * 能使用拼接的位置都尽量进行了拼接,但是依旧会有一些位置无法拼接,则注明在下方列表;
     * 以下为没法直接使用这个属性进行修改的位置:<br/>
     * <ul>
     *     <li>com.yangxj96.spectra.framework.configure.MyBatisPlusConfiguration</li>
     *     <li>com.yangxj96.spectra.framework.advice.ResponseBodyModifyAdvice</li>
     *     <li>com.yangxj96.spectra.launch.LaunchApplication</li>
     * </ul>
     */
    private String packagePrefix = "com.yangxj96.spectra";
}
