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

package com.devops00.spectra.framework.cache;

import com.devops00.spectra.framework.health.readiness.ReadinessVerifier;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisNamespace;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 依赖恢复后的业务缓存一致性校验。
 *
 * <p>数据库或 Redis 恢复后，仅凭 PING 不足以证明缓存读写链路可用。本校验器在专用探针缓存中执行一次
 * 写入、读取和清理，确认缓存命令、键前缀与受控序列化器均可用；同时拒绝业务缓存命名空间与安全 Redis
 * 固定命名空间重叠。校验失败时探针保持未就绪，避免缓存命令持续失败时恢复对外服务。</p>
 *
 * <p>业务缓存是派生数据，Redis 重启后允许为空并从数据库重新构建；这里不复原历史缓存内容，只验证恢复后的
 * 缓存基础设施契约。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Component
@NullMarked
public class CacheConsistencyVerifier implements ReadinessVerifier {

    static final String PROBE_CACHE_NAME = "framework:cache-consistency";

    static final String PROBE_KEY = "probe";

    private final ObjectProvider<CacheManager> cacheManagerProvider;

    public CacheConsistencyVerifier(ObjectProvider<CacheManager> cacheManagerProvider) {
        this.cacheManagerProvider = cacheManagerProvider;
    }

    @Override
    public void verifyRecovery() {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        if (cacheManager == null) {
            throw new IllegalStateException("缓存管理器未装配");
        }
        Cache cache = cacheManager.getCache(PROBE_CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("缓存探针未创建: " + PROBE_CACHE_NAME);
        }
        verifyNamespaceSeparation(cacheManager);

        UUID expected = UUID.randomUUID();
        try {
            cache.evict(PROBE_KEY);
            cache.put(PROBE_KEY, List.of(expected));
            Cache.ValueWrapper wrapper = cache.get(PROBE_KEY);
            if (wrapper == null) {
                throw new IllegalStateException("缓存探针写入后无法读取");
            }
            Object actual = wrapper.get();
            if (!(actual instanceof Collection<?> values)
                    || values.size() != 1
                    || !expected.equals(values.iterator().next())) {
                throw new IllegalStateException("业务缓存恢复校验值不一致");
            }
        } finally {
            try {
                cache.evict(PROBE_KEY);
            } catch (RuntimeException ignored) {
                // 清理失败不覆盖原始校验异常；探针键仍在专用缓存命名空间内并受默认 TTL 约束。
            }
        }
    }

    private static void verifyNamespaceSeparation(CacheManager cacheManager) {
        for (String cacheName : cacheManager.getCacheNames()) {
            if (cacheName != null && (cacheName + ":").startsWith(SecurityRedisNamespace.PREFIX)) {
                throw new IllegalStateException("业务缓存命名空间不得与安全 Redis 重叠: " + cacheName);
            }
        }
    }
}
