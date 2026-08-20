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

package com.devops00.spectra.security.base.util;

import com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException;
import org.springframework.dao.DataAccessException;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

/**
 * 安全 Redis 操作统一执行器。
 *
 * <p>只转换 Redis 数据访问异常；业务校验异常保持原始语义，避免把验证码错误、Token 无效等正常结果误报为基础设施故障。</p>
 */
@NullMarked
public final class SecurityRedisExecutor {

    private SecurityRedisExecutor() {
    }

    /**
     * 执行需要安全 Redis 的操作。
     *
     * @param operation 操作描述
     * @param action    Redis 操作
     * @param <T>       返回值类型
     * @return 操作结果
     * @throws SecurityRedisUnavailableException Redis 不可用
     */
    public static <T> T execute(String operation, Supplier<T> action) {
        try {
            return action.get();
        } catch (SecurityRedisUnavailableException exception) {
            throw exception;
        } catch (DataAccessException exception) {
            throw new SecurityRedisUnavailableException("安全 Redis 不可用，拒绝执行" + operation, exception);
        }
    }

    /**
     * 执行必须返回结果的安全 Redis 操作。
     *
     * @param operation 操作描述
     * @param action    Redis 操作
     * @param <T>       返回值类型
     * @return 非空操作结果
     */
    public static <T> T require(String operation, Supplier<T> action) {
        T result = execute(operation, action);
        if (result == null) {
            throw new SecurityRedisUnavailableException("安全 Redis 未返回" + operation + "结果", null);
        }
        return result;
    }

    /**
     * 执行无返回值的安全 Redis 操作。
     *
     * @param operation 操作描述
     * @param action    Redis 操作
     */
    public static void run(String operation, Runnable action) {
        execute(operation, () -> {
            action.run();
            return null;
        });
    }
}
