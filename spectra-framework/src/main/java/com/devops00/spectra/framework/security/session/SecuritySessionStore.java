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

import com.devops00.spectra.common.security.policy.SecurityPolicyUnavailableException;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 安全 Session 用例共用的 Redis 访问边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
@Component
public final class SecuritySessionStore {

    private final RedisTemplate<String, Object> redis;

    private final SecurityProperties properties;

    private final ObjectProvider<SecuritySessionPolicyProvider> policyProvider;

    public SecuritySessionStore(@Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis,
                                SecurityProperties properties,
                                ObjectProvider<SecuritySessionPolicyProvider> policyProvider) {
        this.redis = redis;
        this.properties = properties;
        this.policyProvider = policyProvider;
    }

    /** 返回安全 Redis 模板。 */
    public RedisTemplate<String, Object> redis() {
        return redis;
    }

    /** 返回安全配置。 */
    public SecurityProperties properties() {
        return properties;
    }

    /** 读取数据库会话策略；策略不可用时拒绝签发和刷新。 */
    public SessionPolicy sessionPolicy(String clientCode) {
        SecuritySessionPolicyProvider provider = policyProvider.getIfAvailable();
        if (provider == null) {
            throw new SecurityPolicyUnavailableException("会话策略 Provider 未配置，拒绝创建或刷新会话", null);
        }
        SessionPolicy policy = provider.find(clientCode);
        if (policy == null) {
            throw new SecurityPolicyUnavailableException("客户端会话策略不存在，拒绝创建或刷新会话", null);
        }
        return policy;
    }

    /** 读取安全 Hash；Redis 不可用或返回 null 时 fail-closed。 */
    public Map<Object, Object> hash(String operation, String key) {
        return SecurityRedisExecutor.require(operation, () -> redis.opsForHash().entries(key));
    }

    /** 读取安全值；Redis 不可用时 fail-closed。 */
    public Object value(String operation, String key) {
        return SecurityRedisExecutor.execute(operation, () -> redis.opsForValue().get(key));
    }

    /** 读取安全 Set；不存在的索引按空集合处理。 */
    public Set<Object> members(String operation, String key) {
        Set<Object> members = SecurityRedisExecutor.execute(operation, () -> redis.opsForSet().members(key));
        return members == null ? Set.of() : members;
    }

    /** 批量读取安全值；Redis 不可用或返回 null 时 fail-closed。 */
    public List<Object> multiGet(String operation, List<String> keys) {
        return SecurityRedisExecutor.require(operation, () -> redis.opsForValue().multiGet(keys));
    }

    /** 检查安全 Key 是否存在；Redis 无法返回结果时 fail-closed。 */
    public boolean hasKey(String operation, String key) {
        return Boolean.TRUE.equals(SecurityRedisExecutor.require(operation, () -> redis.hasKey(key)));
    }
}
