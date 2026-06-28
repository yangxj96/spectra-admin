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

package com.devops00.spectra.security.base.properties;


import com.devops00.spectra.security.base.enums.SecMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// 权限配置相关内容
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/4 10:39
@Data
@ConfigurationProperties(prefix = "spectra.security")
public class SecurityProperties {

    ///  验证白名单
    private List<String> whitelists = new ArrayList<>(Arrays.asList(
            "/common/kaptcha",
            "/auth/login",
            "/actuator/**",
            // 图片预览接口
            "/file/preview/**"
    ));

    /// token有效期时长(秒)
    /// 暂时未启用
    private Long tokenExpire = 7200L;

    /// 超管角色名称
    private String administrators = "ROLE_DEV_OPS";

    /// SecUtil工具当前类型
    private SecMode secMode = SecMode.REDIS;

    /// 登录失败锁定：最大尝试次数
    private int lockoutMaxAttempts = 5;

    /// 登录失败锁定：锁定时长（秒），0=不锁定
    private long lockoutSeconds = 1800L;

    /// Token自动续期间隔（秒），请求距上次续期超过此值才刷新
    private long tokenRefreshInterval = 300L;

}
