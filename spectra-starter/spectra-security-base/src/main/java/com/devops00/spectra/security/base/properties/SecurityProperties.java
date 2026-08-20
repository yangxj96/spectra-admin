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
import com.devops00.spectra.security.base.session.SessionConcurrencyMode;
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
            // MFA 二阶段登录与首次 TOTP 登记
            "/auth/mfa/verify",
            "/auth/mfa/complete",
            "/security/mfa/setup/totp/enroll",
            "/security/mfa/setup/totp/confirm",
            // 首次系统初始化；接口内部仍要求一次性初始化令牌或 Redis Challenge
            "/system/initialization/status",
            "/system/initialization/start",
            "/system/initialization/mfa/confirm",
            "/system/initialization/complete",
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
     * 安全会话适配模式。
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

    /**
     * Preview/Apply 授权变更 token 的 HMAC 密钥；未配置时相关写入口 fail-closed。
     */
    private String authorizationChangeTokenHmacKey = "";

    /** v2 会话并发策略；正式部署可由 session_policy 表覆盖。 */
    private SessionConcurrencyMode sessionConcurrencyMode = SessionConcurrencyMode.ALLOW;

    /** v2 用户最大活动会话数。 */
    private int maxSessions = 5;

    /** TOTP 密钥加密密钥；必须由环境变量或密钥管理系统提供。 */
    private String mfaEncryptionKey = "";

    /** TOTP 密钥加密版本，用于轮换和回迁。 */
    private String mfaEncryptionKeyVersion = "v1";

    /** TOTP 密钥轮换期间允许解密旧设备密钥的上一版本；只用于迁移，不用于新加密。 */
    private String mfaPreviousEncryptionKey = "";

    /** 上一 TOTP 密钥的版本号；必须和上一密钥同时配置。 */
    private String mfaPreviousEncryptionKeyVersion = "";

    /** TOTP provisioning URI 的发行方名称。 */
    private String mfaTotpIssuer = "Spectra";

    /** DEV_OPS 没有已验证 MFA 时禁止创建普通 Root Session。 */
    private boolean mfaRequiredForDevOps = true;

    /** MFA 登录预认证挑战有效期（秒）。 */
    private long mfaChallengeExpire = 300L;

    /** 单个 MFA 登录预认证挑战允许的最大失败次数。 */
    private int mfaChallengeMaxAttempts = 5;

    /** Web Refresh Token 的 Host-only Cookie 名称。 */
    private String refreshCookieName = "__Host-spectra-refresh";

    /** Web Refresh Cookie 必须默认 Secure。 */
    private boolean refreshCookieSecure = true;

    /**
     * 是否显式允许开发环境使用 HTTP Web Cookie。
     * <p>生产环境必须保持关闭；开启后不得使用 {@code __Host-} Cookie 名称。</p>
     */
    private boolean allowInsecureRefreshCookie;

    /** Web Refresh Cookie 的 SameSite 属性，默认 Strict。 */
    private String refreshCookieSameSite = "Strict";

    /**
     * 是否经过部署评审允许 Web Refresh Cookie 使用 SameSite=None。
     * <p>跨站 Cookie 必须同时满足 Secure、精确 Origin allowlist 和 CSRF 校验。</p>
     */
    private boolean refreshCookieSameSiteNoneAllowed;

    /** Web Refresh Cookie Path；Host-only Cookie 固定为 /。 */
    private String refreshCookiePath = "/";

    /** Web Refresh Cookie Domain；默认空值表示 Host-only。 */
    private String refreshCookieDomain = "";

    /** 双提交 CSRF Cookie 名称。 */
    private String csrfCookieName = "XSRF-TOKEN";

    /** 双提交 CSRF Header 名称。 */
    private String csrfHeaderName = "X-XSRF-TOKEN";
}
