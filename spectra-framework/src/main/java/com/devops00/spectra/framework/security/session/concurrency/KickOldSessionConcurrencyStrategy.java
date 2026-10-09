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

package com.devops00.spectra.framework.security.session.concurrency;

import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.value.SecurityRedisValueParser;
import com.devops00.spectra.framework.security.session.SecuritySessionStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 新会话创建前撤销同一客户端活动会话的并发策略。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
@Component
public final class KickOldSessionConcurrencyStrategy implements SessionConcurrencyStrategy {

    private final SecuritySessionStore store;

    /**
     * 创建依赖安全 Session Redis 读取边界的策略。
     *
     * @param store 读取活动 Session Hash 的安全存储边界。
     */
    public KickOldSessionConcurrencyStrategy(SecuritySessionStore store) {
        this.store = Objects.requireNonNull(store, "store 不能为空");
    }

    /**
     * 返回撤销旧会话模式。
     *
     * @return 返回 {@link SessionConcurrencyMode#KICK_OLD}，表示新会话覆盖同客户端旧会话。
     */
    @Override
    public SessionConcurrencyMode mode() {
        return SessionConcurrencyMode.KICK_OLD;
    }

    /**
     * 只撤销与新会话客户端编码相同的活动 Session。
     *
     * @param policy             当前客户端会话策略；撤销旧会话模式下不使用容量值。
     * @param clientCode         新会话客户端编码，用于过滤其他客户端的活动 Session。
     * @param activeTokenDigests Access 或关联 Refresh 仍有效的会话摘要集合。
     * @param revokeAccessDigest 按访问令牌摘要执行受控撤销的回调。
     */
    @Override
    public void enforce(SessionPolicy policy, String clientCode, Set<String> activeTokenDigests,
                        Consumer<String> revokeAccessDigest) {
        Objects.requireNonNull(clientCode, "clientCode 不能为空");
        Objects.requireNonNull(revokeAccessDigest, "revokeAccessDigest 不能为空");
        for (String activeTokenDigest : activeTokenDigests) {
            String existingClient = clientTypeOf(activeTokenDigest);
            if (clientCode.equals(existingClient)) {
                revokeAccessDigest.accept(activeTokenDigest);
            }
        }
    }

    /** Access 过期后仍可通过有效 Refresh 辨认并撤销同端旧会话。 */
    private String clientTypeOf(String accessDigest) {
        Map<Object, Object> session = store.hash("读取活动安全会话", SecurityRedisKey.SESSION.format(accessDigest));
        if (!session.isEmpty()) {
            return SecurityRedisValueParser.requiredText(session.get("clientType"), "Session.clientType");
        }
        Object refreshValue = store.value("读取过期 Access 的 Refresh 映射",
                SecurityRedisKey.REFRESH_TOKEN.format(accessDigest));
        if (refreshValue == null) {
            return "";
        }
        String refreshDigest = SecurityRedisValueParser.requiredText(refreshValue, "Access.refreshDigest");
        Map<Object, Object> refresh = store.hash("读取过期 Access 的 Refresh 状态",
                SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest));
        if (refresh.isEmpty()) {
            return "";
        }
        String mappedAccess = SecurityRedisValueParser.requiredText(refresh.get("accessToken"), "Refresh.accessToken");
        if (!accessDigest.equals(mappedAccess)) {
            throw new SecurityRedisUnavailableException("安全 Redis Access Refresh 映射不一致", null);
        }
        return SecurityRedisValueParser.requiredText(refresh.get("clientType"), "Refresh.clientType");
    }
}
