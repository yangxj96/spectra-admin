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

package com.devops00.spectra.framework.security.properties;

import com.devops00.spectra.framework.security.redis.key.SecurityRedisNamespace;
import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

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
@Validated
@ConfigurationProperties(prefix = "spectra.security")
public class SecurityProperties {

    /**
     * 验证白名单
     */
    @NotEmpty
    @Size(max = 64)
    private List<@NotBlank @Size(max = 200) String> whitelists = new ArrayList<>(Arrays.asList(
            // 生成图形验证码
            "/common/kaptcha",
            // 用户登陆
            "/security/authentication/login",
            // 首次系统初始化；接口内部要求一次性初始化令牌和初始化 ID
            "/system/initialization/status",
            "/system/initialization/start",
            "/system/initialization/complete",
            // 刷新token
            "/security/authentication/refresh",
            // 发送短信验证码
            "/security/authentication/sms",
            // 发送邮箱验证码
            "/security/authentication/email",
            // 健康检查与版本信息；禁止把整个 Actuator 暴露为匿名接口
            "/actuator/health",
            "/actuator/info",
            // 获取系统加密配置接口
            "/system/crypto/config",
            // Web 端启动阶段公开配置聚合接口
            "/system/bootstrap",
            // 外部 Provider 回执由通知模块使用渠道 Secret HMAC 验签
            "/notification/provider/callback/**"));

    /**
     * accessToken有效期（秒），默认5分钟
     */
    @Min(1)
    @Max(31_536_000)
    private long accessTokenExpire = 300L;

    /**
     * refreshToken有效期（秒），默认7天
     */
    @Min(1)
    @Max(31_536_000)
    private long refreshTokenExpire = 604800L;

    /**
     * 安全会话适配模式。
     */
    private SecMode secMode = SecMode.REDIS;

    /**
     * 登录失败锁定：最大尝试次数
     */
    @Min(1)
    @Max(100)
    private int lockoutMaxAttempts = 5;

    /**
     * 登录失败锁定：锁定时长（秒），0=不锁定
     */
    @Min(0)
    @Max(86_400)
    private long lockoutSeconds = 300L;

    /**
     * 验证码有效期（秒）。
     */
    @Min(1)
    @Max(86_400)
    private long verificationCodeExpire = 300L;

    /**
     * 单个验证码窗口允许的最大校验尝试次数。
     */
    @Min(1)
    @Max(100)
    private int verificationCodeMaxAttempts = 5;

    /**
     * 验证码长度。当前只允许 6 位数字。
     */
    @Min(6)
    @Max(6)
    private int verificationCodeLength = 6;

    /**
     * Root 最少有效用户数；首版始终保护最后一个有效 Root。
     */
    @Min(1)
    @Max(100)
    private int minEffectiveDevOpsUsers = 1;

    /**
     * Root 最大用户数，默认 3（推荐 2 个日常 Root + 1 个 break-glass）。
     */
    @Min(1)
    @Max(100)
    private int maxDevOpsUsers = 3;

    /** v2 会话并发策略；正式部署可由 session_policy 表覆盖。 */
    private SessionConcurrencyMode sessionConcurrencyMode = SessionConcurrencyMode.ALLOW;

    /** v2 用户最大活动会话数。 */
    @Min(1)
    @Max(100)
    private int maxSessions = 5;

    /** 加密请求体的最大字节数，防止在 JSON 解析前无界占用内存。 */
    @Min(1_024)
    @Max(10_485_760)
    private long cryptoRequestMaxBodyBytes = 1_048_576L;

    /** 加密请求时间戳和 nonce 的防重放窗口。 */
    @Min(1)
    @Max(3_600)
    private long cryptoReplayWindowSeconds = 300L;

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

    /** 安全 Redis 运行时契约。 */
    private RedisContractProperties redis = new RedisContractProperties();

    /**
     * 安全 Redis 运行时契约配置。
     *
     * <p>连接/命令超时和连接池由 Spring Boot 的 {@code spring.data.redis} 管理；这里仅登记安全数据格式、
     * fail-closed 不变量和后续可靠 worker 共用的批量边界。</p>
     */
    @Data
    public static class RedisContractProperties {

        /** 安全 Redis Key 固定命名空间。 */
        private String namespace = SecurityRedisNamespace.PREFIX;

        /** 安全 Redis 不可用时必须拒绝安全操作。 */
        private boolean failClosed = true;

        /** 可靠 worker 默认批量上限，避免后续 worker 无界读取。 */
        private int workerBatchSize = 100;
    }

    /**
     * 校验安全 Redis 契约；不允许通过配置关闭 fail-closed 或改变已发布 Key 命名空间。
     */
    public void validateRedisContract() {
        if (redis == null) {
            throw new IllegalStateException("安全 Redis contract 不得为空");
        }
        if (!SecurityRedisNamespace.PREFIX.equals(redis.getNamespace())) {
            throw new IllegalStateException("安全 Redis namespace 必须固定为 " + SecurityRedisNamespace.PREFIX);
        }
        if (!redis.isFailClosed()) {
            throw new IllegalStateException("安全 Redis 必须启用 fail-closed");
        }
        if (redis.getWorkerBatchSize() < 1 || redis.getWorkerBatchSize() > 1000) {
            throw new IllegalStateException("安全 Redis worker 批量大小必须在 1 到 1000 之间");
        }
    }

    /**
     * 校验安全运行参数，在 Spring 容器启动阶段调用。
     */
    public void validate() {
        validateRedisContract();
        validateWhitelist();
        requireRange(accessTokenExpire, 1, 31_536_000, "accessTokenExpire 必须在 1 到 31536000 之间");
        requireRange(refreshTokenExpire, 1, 31_536_000, "refreshTokenExpire 必须在 1 到 31536000 之间");
        requireRange(lockoutMaxAttempts, 1, 100, "lockoutMaxAttempts 必须在 1 到 100 之间");
        requireRange(lockoutSeconds, 0, 86_400, "lockoutSeconds 必须在 0 到 86400 之间");
        requireRange(verificationCodeExpire, 1, 86_400, "verificationCodeExpire 必须在 1 到 86400 之间");
        requireRange(verificationCodeMaxAttempts, 1, 100,
                "verificationCodeMaxAttempts 必须在 1 到 100 之间");
        requireExact(verificationCodeLength, 6, "verificationCodeLength 必须为 6");
        requireRange(minEffectiveDevOpsUsers, 1, 100, "minEffectiveDevOpsUsers 必须在 1 到 100 之间");
        requireRange(maxDevOpsUsers, 1, 100, "maxDevOpsUsers 必须在 1 到 100 之间");
        if (maxDevOpsUsers < minEffectiveDevOpsUsers) {
            throw new IllegalStateException("DevOps Root 用户数量边界无效");
        }
        requireRange(maxSessions, 1, 100, "maxSessions 必须在 1 到 100 之间");
        requireRange(cryptoRequestMaxBodyBytes, 1_024, 10_485_760,
                "cryptoRequestMaxBodyBytes 必须在 1024 到 10485760 之间");
        requireRange(cryptoReplayWindowSeconds, 1, 3_600,
                "cryptoReplayWindowSeconds 必须在 1 到 3600 之间");
    }

    /** 校验匿名路径必须保持窄匹配，禁止使用覆盖全部请求的通配路径。 */
    private void validateWhitelist() {
        if (whitelists == null || whitelists.isEmpty() || whitelists.size() > 64) {
            throw new IllegalStateException("安全白名单不能为空");
        }
        if (whitelists.stream()
                .anyMatch(path -> path == null
                        || path.isBlank()
                        || !path.startsWith("/")
                        || path.length() > 200
                        || "/**".equals(path.trim()))) {
            throw new IllegalStateException("安全白名单必须是以 / 开头的窄路径，禁止 /**");
        }
    }

    /** 校验数值配置的闭区间边界。 */
    private static void requireRange(long value, long minimum, long maximum, String message) {
        if (value < minimum || value > maximum) {
            throw new IllegalStateException(message);
        }
    }

    /** 校验只能取单一允许值的配置。 */
    private static void requireExact(long value, long expected, String message) {
        if (value != expected) {
            throw new IllegalStateException(message);
        }
    }
}
