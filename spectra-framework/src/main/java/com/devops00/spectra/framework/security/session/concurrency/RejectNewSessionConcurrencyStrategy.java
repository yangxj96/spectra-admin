/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.security.session.concurrency;

import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

/**
 * 达到并发上限后拒绝新会话的并发策略。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
@Component
public final class RejectNewSessionConcurrencyStrategy implements SessionConcurrencyStrategy {

    /**
     * 返回拒绝新会话模式。
     *
     * @return 返回 {@link SessionConcurrencyMode#REJECT_NEW}，表示达到上限后保持既有会话并拒绝新会话。
     */
    @Override
    public SessionConcurrencyMode mode() {
        return SessionConcurrencyMode.REJECT_NEW;
    }

    /**
     * 在活动会话数达到上限时拒绝新会话。
     *
     * @param policy             当前客户端会话策略，用于读取最大活动会话数。
     * @param clientCode         当前客户端编码；该模式按用户总活动会话数判断，参数仅用于统一策略端口。
     * @param activeTokenDigests 当前活动访问令牌摘要集合。
     * @param revokeAccessDigest 受控撤销回调；拒绝模式不会撤销既有会话。
     */
    @Override
    public void enforce(SessionPolicy policy, String clientCode, Set<String> activeTokenDigests,
                        Consumer<String> revokeAccessDigest) {
        if (activeTokenDigests.size() >= policy.maxSessions()) {
            throw new IllegalStateException("已达到该账号的最大并发会话数");
        }
    }
}
