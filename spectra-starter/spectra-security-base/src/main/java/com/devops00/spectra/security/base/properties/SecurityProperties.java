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

/**
 * 权限配置相关内容
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/4 10:39
 */
@Data
@ConfigurationProperties(prefix = "spectra.security")
public class SecurityProperties {

    /**
     * 验证白名单
     */
    private List<String> whitelists = new ArrayList<>(Arrays.asList(
            // 生成图形验证码
            "/common/kaptcha",
            // 用户登陆
            "/auth/login",
            // 刷新token
            "/auth/refresh",
            // 发送短信验证码
            "/auth/sms",
            // 发送邮箱验证码
            "/auth/email",
            // 健康检查与版本信息；禁止把整个 Actuator 暴露为匿名接口
            "/actuator/health",
            "/actuator/info",
            // 获取系统加密配置接口
            "/system/crypto/config"));

    /**
     * accessToken有效期（秒），默认5分钟
     */
    private long accessTokenExpire = 300L;

    /**
     * refreshToken有效期（秒），默认7天
     */
    private long refreshTokenExpire = 604800L;

    /**
     * SecUtil工具当前类型
     */
    private SecMode secMode = SecMode.REDIS;

    /**
     * 登录失败锁定：最大尝试次数
     */
    private int lockoutMaxAttempts = 5;

    /**
     * 登录失败锁定：锁定时长（秒），0=不锁定
     */
    private long lockoutSeconds = 300L;

    /**
     * Token自动续期间隔（秒），请求距上次续期超过此值才刷新
     */
    private long tokenRefreshInterval = 300L;

    /**
     * 验证码有效期（秒）。
     */
    private long verificationCodeExpire = 300L;

    /**
     * 验证码 HMAC 密钥；必须通过环境变量或密钥管理系统提供。
     */
    private String verificationCodeHmacKey = "";

    /**
     * 单个验证码窗口允许的最大校验尝试次数。
     */
    private int verificationCodeMaxAttempts = 5;

    /**
     * 验证码长度。当前只允许 6 位数字。
     */
    private int verificationCodeLength = 6;

    /**
     * Root 最少有效用户数；首版始终保护最后一个有效 Root。
     */
    private int minEffectiveDevOpsUsers = 1;

    /**
     * Root 最大用户数，默认 3（推荐 2 个日常 Root + 1 个 break-glass）。
     */
    private int maxDevOpsUsers = 3;
}
