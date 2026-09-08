/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.security.session.concurrency;

import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.common.security.policy.SessionPolicy;

import java.util.Set;
import java.util.function.Consumer;

/**
 * 新建安全 Session 时执行并发策略判定的策略端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
public interface SessionConcurrencyStrategy {

    /**
     * 返回该策略处理的持久化并发模式。
     *
     * @return 返回当前实现对应的 {@link SessionConcurrencyMode}；每个模式必须只有一个 Spring 策略 Bean，不返回 null。
     */
    SessionConcurrencyMode mode();

    /**
     * 根据会话策略检查活动会话，并在策略要求时撤销旧会话。
     *
     * @param policy             当前客户端的会话并发、容量和 TTL 策略快照。
     * @param clientCode         当前新会话所属客户端编码，用于隔离不同客户端的会话。
     * @param activeTokenDigests 当前用户仍存在于安全 Redis 的活动访问令牌摘要集合。
     * @param revokeAccessDigest 按访问令牌摘要撤销会话的受控回调，不得将令牌原文传入回调。
     */
    void enforce(SessionPolicy policy, String clientCode, Set<String> activeTokenDigests,
                 Consumer<String> revokeAccessDigest);
}
