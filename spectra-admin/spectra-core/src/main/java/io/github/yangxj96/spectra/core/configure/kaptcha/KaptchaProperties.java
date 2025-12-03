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

package io.github.yangxj96.spectra.core.configure.kaptcha;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 验证码生成相关配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/25
 */
@Data
@ConfigurationProperties(prefix = "spectra.kaptcha")
public class KaptchaProperties {

    /**
     * 登录是否验证
     */
    private Boolean verify = Boolean.TRUE;

    /**
     * 是什么类型的验证码
     */
    private KaptchaType type = KaptchaType.MATH;

    /**
     * 验证码存储时长,默认1分钟
     */
    private Duration duration = Duration.ofMinutes(1);
}
