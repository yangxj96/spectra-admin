/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.security.session.concurrency;

import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.common.security.policy.SessionPolicy;
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
     * @param activeTokenDigests 当前活动访问令牌摘要集合。
     * @param revokeAccessDigest 按访问令牌摘要执行受控撤销的回调。
     */
    @Override
    public void enforce(SessionPolicy policy, String clientCode, Set<String> activeTokenDigests,
                        Consumer<String> revokeAccessDigest) {
        Objects.requireNonNull(clientCode, "clientCode 不能为空");
        Objects.requireNonNull(revokeAccessDigest, "revokeAccessDigest 不能为空");
        for (String activeTokenDigest : activeTokenDigests) {
            Map<Object, Object> activeSession = store.hash("读取活动安全会话",
                    SecurityRedisKey.SESSION.format(activeTokenDigest));
            if (!activeSession.isEmpty()
                    && clientCode.equals(SecurityRedisValueParser.requiredText(activeSession.get("clientType"),
                            "Session.clientType"))) {
                revokeAccessDigest.accept(activeTokenDigest);
            }
        }
    }
}
