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

package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.port.security.UserOnlineVO;
import com.devops00.spectra.framework.security.session.converter.UserOnlineConverter;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.value.SecurityRedisValueParser;
import com.devops00.spectra.framework.security.session.query.SecuritySessionQuery;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 在线 Session 查询用例，使用全局摘要索引和一次 MGET 消除逐 Token Hash 查询。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
@Component
@NullMarked
public class SecurityOnlineUserQueryService implements SecuritySessionQuery {

    private final SecuritySessionStore store;

    private final UserOnlineConverter userOnlineConverter;

    public SecurityOnlineUserQueryService(SecuritySessionStore store, UserOnlineConverter userOnlineConverter) {
        this.store = store;
        this.userOnlineConverter = userOnlineConverter;
    }

    /**
     * 查询当前仍有有效会话的在线用户。
     *
     * @return 返回当前安全 Redis 中可解析的在线用户视图列表；没有在线用户或无法解析的记录时返回空列表，不返回 null。
     */
    @Override
    public List<UserOnlineVO> listOnlineUsers() {
        return SecurityRedisExecutor.execute("查询在线用户", this::listOnlineUsersInternal);
    }

    /**
     * 查询内部。
     */
    private List<UserOnlineVO> listOnlineUsersInternal() {
        Set<Object> tokenDigests = store.members("读取在线会话索引", SecurityRedisKey.ONLINE_SESSIONS.getPattern());
        if (tokenDigests.isEmpty()) {
            return List.of();
        }

        List<String> summaryKeys = new ArrayList<>(tokenDigests.size());
        for (Object tokenDigest : tokenDigests) {
            summaryKeys.add(SecurityRedisKey.SESSION_SUMMARY.format(
                    SecurityRedisValueParser.requiredText(tokenDigest, "OnlineSessions.accessDigest")));
        }
        List<Object> summaries = store.multiGet("批量读取在线会话摘要", summaryKeys);
        if (summaries.size() != summaryKeys.size()) {
            throw new com.devops00.spectra.common.exception.SecurityRedisUnavailableException(
                    "安全 Redis 在线会话摘要数量不完整", null);
        }

        List<UserOnlineVO> result = new ArrayList<>(summaries.size());
        for (Object rawSummary : summaries) {
            var summary = SecurityRedisValueParser.requiredMap(rawSummary, "SessionSummary");
            String userId = SecurityRedisValueParser.requiredText(summary.get("userId"), "SessionSummary.userId");
            String username = SecurityRedisValueParser.requiredText(summary.get("username"), "SessionSummary.username");
            String clientType = SecurityRedisValueParser.requiredClientType(summary.get("clientType"),
                    "SessionSummary.clientType").getName();
            String ip = SecurityRedisValueParser.requiredText(summary.get("ip"), "SessionSummary.ip");
            long loginTime = SecurityRedisValueParser.requiredLong(summary.get("loginTime"), "SessionSummary.loginTime");
            result.add(userOnlineConverter.toVO(userId, username, clientType, ip, loginTime));
        }
        return result;
    }
}
