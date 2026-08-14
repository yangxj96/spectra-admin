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

package com.devops00.spectra.security.base.constant;

import com.devops00.spectra.common.constant.RedisKey;

/**
 * 登录需要存储的key的枚举
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/8 18:41
 */
public enum AuthRedisKey implements RedisKey {

    /** v2 会话详情（事实源）Hash，格式参数为 Access Token digest。 */
    SESSION("sec:v2:sess:%s"),

    /**
     * 用户+端 → token（同端复用 & 按端踢出）
     */
    USER_CLIENT("sec:v2:uc:%s:%s"),

    /**
     * 用户所有 token 集合（全端踢出 & 在线查询）
     */
    USER_TOKENS("sec:v2:ut:%s"),

    /**
     * 在线用户 ID 集合
     */
    ONLINE_USERS("sec:v2:online"),

    /** Token Family 下的 Access digest 集合。 */
    SESSION_FAMILY("sec:v2:family:%s"),

    /**
     * 登录失败计数（锁定账号）
     */
    LOGIN_FAIL("sec:v2:fail:%s"),

    /**
     * 刷新token → accessToken 映射
     */
    REFRESH_TOKEN("sec:v2:rt:%s"),

    /**
     * Refresh Token 重放后的用户级撤销栅栏。
     */
    REFRESH_REPLAY_FENCE("sec:v2:replay:%s"),

    /**
     * Refresh Token 一次性消费声明。与 Refresh Token Hash 分离，避免受 HashValueSerializer 影响。
     */
    REFRESH_CLAIM("sec:v2:rt:claim:%s");

    private final String pattern;

    AuthRedisKey(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
}
