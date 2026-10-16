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

package com.devops00.spectra.framework.health.readiness;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * 运行期依赖就绪状态。
 *
 * <p>启动阶段由启动门确保必需依赖可用，初始为 READY；运行期依赖探针探测到数据库或安全 Redis 不可用时
 * 切换为 UNREADY，业务请求被拒绝、后台任务暂停；依赖恢复并通过校验后回到 READY。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@Component
@NullMarked
public class DependencyReadinessState {

    /**
     * 就绪状态。
     */
    public enum State {
        /** 必需依赖可用，业务可服务。 */
        READY,
        /** 必需依赖不可用或未通过校验，业务必须拒绝。 */
        UNREADY
    }

    private volatile State state = State.READY;

    private volatile @Nullable String reason;

    /**
     * 标记为就绪；依赖恢复并通过校验后调用。
     */
    public synchronized void markReady() {
        this.state = State.READY;
        this.reason = null;
    }

    /**
     * 标记为未就绪；依赖故障或校验失败时调用。
     *
     * @param reason 不可用的原因，仅用于诊断，不包含敏感信息
     */
    public synchronized void markUnready(String reason) {
        this.state = State.UNREADY;
        this.reason = reason;
    }

    public boolean isReady() {
        return state == State.READY;
    }

    public State state() {
        return state;
    }

    public @Nullable String reason() {
        return reason;
    }
}
