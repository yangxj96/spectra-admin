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

package com.devops00.spectra.security.base.exception;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * 安全 Redis 依赖不可用异常。
 *
 * <p>安全 Redis 是 Token、Session、验证码和防重放状态的事实源。
 * 发生连接、超时或命令执行失败时，调用方不得把故障解释为“数据不存在”并继续处理请求。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/20
 */
public final class SecurityRedisUnavailableException extends DataAccessResourceFailureException {

    public SecurityRedisUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
