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

    /**
     * 返回安全 Redis 模板。
     *
     * @return 返回安全 Redis 专用模板；该 Bean 由 Spring 注入并始终非 null。
     */
    public RedisTemplate<String, Object> redis() {
        return redis;
    }

    /**
     * 返回安全配置。
     *
     * @return 返回安全会话和 Cookie 配置；该配置由 Spring 注入并始终非 null。
     */
    public SecurityProperties properties() {
        return properties;
    }

    /**
     * 读取数据库会话策略；策略不可用时拒绝签发和刷新。
     *
     * @param clientCode 客户端编码，用于读取对应的会话策略。
     * @return 返回指定客户端的会话 TTL、并发限制和刷新策略；Provider 缺失或未配置该客户端时抛出异常，不返回 null。
     */
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

    /**
     * 读取安全 Hash；Redis 不可用或返回 null 时 fail-closed。
     *
     * @param operation 安全 Redis 操作标识，用于诊断并统一处理存储失败。
     * @param key       已按安全命名空间生成的 Redis 键。
     * @return 返回指定安全 Hash 的字段快照；Hash 不存在时返回空 Map，Redis 无法确认状态时抛出异常，不返回 null。
     */
    public Map<Object, Object> hash(String operation, String key) {
        return SecurityRedisExecutor.require(operation, () -> redis.opsForHash().entries(key));
    }

    /**
     * 读取安全值；Redis 不可用时 fail-closed。
     *
     * @param operation 安全 Redis 操作标识，用于诊断并统一处理存储失败。
     * @param key       已按安全命名空间生成的 Redis 键。
     * @return 返回指定安全 Redis Key 的值；Key 不存在时返回 null，Redis 命令失败时抛出异常，不以空值掩盖故障。
     */
    public Object value(String operation, String key) {
        return SecurityRedisExecutor.execute(operation, () -> redis.opsForValue().get(key));
    }

    /**
     * 读取安全 Set；不存在的索引按空集合处理。
     *
     * @param operation 安全 Redis 操作标识，用于诊断并统一处理存储失败。
     * @param key       已按安全命名空间生成的 Redis 键。
     * @return 返回指定安全 Set 的成员；Set 不存在时返回空 Set，不返回 null，Redis 命令失败时抛出异常。
     */
    public Set<Object> members(String operation, String key) {
        Set<Object> members = SecurityRedisExecutor.execute(operation, () -> redis.opsForSet().members(key));
        return members == null ? Set.of() : members;
    }

    /**
     * 批量读取安全值；Redis 不可用或返回 null 时 fail-closed。
     *
     * @param operation 安全 Redis 操作标识，用于诊断并统一处理存储失败。
     * @param keys      待处理的对象集合。
     * @return 返回按 keys 顺序排列的安全 Redis 值列表；没有匹配值时列表中对应位置为 null，Redis 返回 null 结果或命令失败时抛出异常。
     */
    public List<Object> multiGet(String operation, List<String> keys) {
        return SecurityRedisExecutor.require(operation, () -> redis.opsForValue().multiGet(keys));
    }

    /**
     * 检查安全 Key 是否存在；Redis 无法返回结果时 fail-closed。
     *
     * @param operation 安全 Redis 操作标识，用于诊断并统一处理存储失败。
     * @param key       已按安全命名空间生成的 Redis 键。
     * @return 返回指定安全 Redis Key 是否存在；不存在时返回 false，Redis 无法确认状态时抛出异常，不返回 null。
     */
    public boolean hasKey(String operation, String key) {
        return Boolean.TRUE.equals(SecurityRedisExecutor.require(operation, () -> redis.hasKey(key)));
    }
}
