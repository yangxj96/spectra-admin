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
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 解析安全 Session 并发模式对应的 Spring 策略 Bean。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
@Component
public final class SessionConcurrencyStrategyResolver {

    private final Map<SessionConcurrencyMode, SessionConcurrencyStrategy> strategies;

    /**
     * 校验并建立不可变的并发策略索引。
     *
     * @param candidates Spring 容器发现的每个会话并发模式策略 Bean 集合；必须恰好覆盖所有持久化模式。
     */
    public SessionConcurrencyStrategyResolver(List<SessionConcurrencyStrategy> candidates) {
        Objects.requireNonNull(candidates, "会话并发策略集合不能为空");
        var indexed = new EnumMap<SessionConcurrencyMode, SessionConcurrencyStrategy>(SessionConcurrencyMode.class);
        for (SessionConcurrencyStrategy candidate : candidates) {
            Objects.requireNonNull(candidate, "会话并发策略不能为空");
            SessionConcurrencyMode mode = Objects.requireNonNull(candidate.mode(), "会话并发模式不能为空");
            if (indexed.put(mode, candidate) != null) {
                throw new IllegalStateException("会话并发模式重复注册: " + mode);
            }
        }
        if (indexed.size() != SessionConcurrencyMode.values().length) {
            throw new IllegalStateException("会话并发策略未覆盖全部持久化模式");
        }
        this.strategies = Map.copyOf(indexed);
    }

    /**
     * 取得指定会话并发模式的策略实现。
     *
     * @param mode 会话策略快照声明的并发模式。
     * @return 返回处理该模式的 Spring 策略 Bean；模式未注册时抛出异常，不返回 null。
     */
    public SessionConcurrencyStrategy resolve(SessionConcurrencyMode mode) {
        Objects.requireNonNull(mode, "会话并发模式不能为空");
        SessionConcurrencyStrategy strategy = strategies.get(mode);
        if (strategy == null) {
            throw new IllegalStateException("会话并发策略不可用: " + mode);
        }
        return strategy;
    }
}
