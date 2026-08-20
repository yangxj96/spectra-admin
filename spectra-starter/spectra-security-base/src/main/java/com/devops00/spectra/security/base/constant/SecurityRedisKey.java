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
 * Security Session and login-protection Redis keys.
 *
 * <p>All session keys use the {@code sec:} namespace. Values are token
 * digests or non-sensitive identifiers; plaintext tokens must never be used
 * as a key or value.</p>
 */
public enum SecurityRedisKey implements RedisKey {

    /** 会话详情（事实源）Hash，格式参数为 Access Token digest。 */
    SESSION("sec:sess:%s"),

    /** 用户+端 → token（同端复用 & 按端踢出）。 */
    USER_CLIENT("sec:uc:%s:%s"),

    /** 用户所有 token 集合（全端踢出 & 在线查询）。 */
    USER_TOKENS("sec:ut:%s"),

    /** 在线用户 ID 集合。 */
    ONLINE_USERS("sec:online"),

    /** Token Family 下的 Access digest 集合。 */
    SESSION_FAMILY("sec:family:%s"),

    /** Token Family 下的 Refresh digest 集合，用于整条会话链撤销时清理轮换残留。 */
    REFRESH_FAMILY("sec:rt:family:%s"),

    /** 登录失败计数（锁定账号）。 */
    LOGIN_FAIL("sec:fail:%s"),

    /** 刷新 token → access token 映射。 */
    REFRESH_TOKEN("sec:rt:%s"),

    /** Refresh Token 重放后的用户级撤销栅栏。 */
    REFRESH_REPLAY_FENCE("sec:replay:%s"),

    /** Refresh Token 一次性消费声明。 */
    REFRESH_CLAIM("sec:rt:claim:%s"),

    /** MFA 登录预认证挑战；只保存短期非敏感的用户标识和挑战状态。 */
    MFA_CHALLENGE("sec:mfa:challenge:%s"),

    /** 首次系统初始化令牌的 SHA-256 摘要；不保存令牌明文。 */
    INITIALIZATION_TOKEN("sec:init:token");

    private final String pattern;

    SecurityRedisKey(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
}
