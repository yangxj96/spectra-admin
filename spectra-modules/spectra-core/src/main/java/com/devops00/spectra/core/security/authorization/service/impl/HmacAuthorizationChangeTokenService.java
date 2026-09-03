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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.core.security.change.AuthorizationChangeToken;
import com.devops00.spectra.core.security.change.AuthorizationChangeTokenService;
import com.devops00.spectra.framework.configure.security.properties.SecurityProperties;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * 授权 Preview/Apply token 的 HMAC 实现。
 * <p>
 * Token 只携带短期、绑定操作者/目标/版本/请求摘要的声明，不保存授权数据，也不接受客户端篡改。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Service
public class HmacAuthorizationChangeTokenService implements AuthorizationChangeTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    private final Clock clock;

    @Autowired
    public HmacAuthorizationChangeTokenService(ObjectProvider<SecurityProperties> propertiesProvider) {
        this(propertiesProvider.getIfAvailable(SecurityProperties::new).getAuthorizationChangeTokenHmacKey(), Clock.systemUTC());
    }

    HmacAuthorizationChangeTokenService(String secret, Clock clock) {
        this.secret = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        this.clock = clock;
    }

    @Override
    public String issue(AuthorizationChangeToken token) {
        requireSecret();
        if (token == null || token.expiresAt() == null || !token.expiresAt().isAfter(clock.instant())) {
            throw new IllegalArgumentException("授权变更 token 必须具有未来过期时间");
        }
        String payload = String.join("|", "1", uuidText(token.tokenId()), uuidText(token.operatorId()),
                uuidText(token.targetUserId()), uuidText(token.roleId()), uuidText(token.assignmentId()),
                Long.toString(token.expectedVersion()),
                token.requestHash(), Long.toString(token.expiresAt().toEpochMilli()));
        return encode(payload) + "." + encode(sign(payload));
    }

    @Override
    public AuthorizationChangeToken verify(String encodedToken) {
        requireSecret();
        if (encodedToken == null || encodedToken.isBlank()) {
            throw new IllegalArgumentException("授权变更 token 不能为空");
        }
        String[] parts = encodedToken.split("\\.", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("授权变更 token 格式无效");
        }
        String payload = decode(parts[0]);
        byte[] actualSignature = decodeBytes(parts[1]);
        if (!MessageDigest.isEqual(sign(payload), actualSignature)) {
            throw new IllegalArgumentException("授权变更 token 签名无效");
        }
        try {
            String[] values = payload.split("\\|", -1);
            if (values.length != 9 || !"1".equals(values[0])) {
                throw new IllegalArgumentException("授权变更 token 版本无效");
            }
            var expiresAt = Instant.ofEpochMilli(Long.parseLong(values[8]));
            if (!expiresAt.isAfter(clock.instant())) {
                throw new IllegalArgumentException("授权变更 token 已过期");
            }
            return new AuthorizationChangeToken(parseUuid(values[1]), parseUuid(values[2]),
                    parseUuid(values[3]), parseUuid(values[4]), parseUuid(values[5]),
                    Long.parseLong(values[6]), values[7], expiresAt);
        } catch (RuntimeException exception) {
            if (exception instanceof IllegalArgumentException && "授权变更 token 已过期".equals(exception.getMessage())) {
                throw exception;
            }
            throw new IllegalArgumentException("授权变更 token 载荷无效", exception);
        }
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireSecret}）。
     */
    private void requireSecret() {
        if (secret.length < 32) {
            throw new IllegalStateException("未配置满足长度要求的授权变更 token HMAC 密钥");
        }
    }

    /**
     * 处理内部业务逻辑（{@code uuidText}）。
     */
    private static String uuidText(UUID value) {
        return value == null ? "-" : value.toString();
    }

    /**
     * 转换、解析或规范化数据（{@code parseUuid}）。
     */
    private static UUID parseUuid(String value) {
        return "-".equals(value) ? null : UUID.fromString(value);
    }

    /**
     * 处理内部业务逻辑（{@code sign}）。
     */
    private byte[] sign(String payload) {
        try {
            var mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("生成授权变更 token 签名失败", exception);
        }
    }

    /**
     * 转换、解析或规范化数据（{@code encode}）。
     */
    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 转换、解析或规范化数据（{@code encode}）。
     */
    private static String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    /**
     * 转换、解析或规范化数据（{@code decode}）。
     */
    private static String decode(String value) {
        return new String(decodeBytes(value), StandardCharsets.UTF_8);
    }

    /**
     * 转换、解析或规范化数据（{@code decodeBytes}）。
     */
    private static byte[] decodeBytes(String value) {
        try {
            return Base64.getUrlDecoder().decode(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("授权变更 token 编码无效", exception);
        }
    }
}
