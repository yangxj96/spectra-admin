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
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

/**
 * 允许创建新会话且不处理既有会话的并发策略。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
@Component
public final class AllowSessionConcurrencyStrategy implements SessionConcurrencyStrategy {

    /**
     * 返回允许并发模式。
     *
     * @return 返回 {@link SessionConcurrencyMode#ALLOW}，表示不因已有会话拒绝或撤销新会话。
     */
    @Override
    public SessionConcurrencyMode mode() {
        return SessionConcurrencyMode.ALLOW;
    }

    /**
     * 允许新会话；该模式不读取或撤销既有会话。
     *
     * @param policy             当前客户端会话策略，允许模式下不需要额外判定。
     * @param clientCode         当前客户端编码，允许模式下不需要额外判定。
     * @param activeTokenDigests 当前活动会话摘要，允许模式下保持不变。
     * @param revokeAccessDigest 受控撤销回调，允许模式下不会调用。
     */
    @Override
    public void enforce(SessionPolicy policy, String clientCode, Set<String> activeTokenDigests,
                        Consumer<String> revokeAccessDigest) {
        // ALLOW 的行为就是不改变活动会话集合。
    }
}
