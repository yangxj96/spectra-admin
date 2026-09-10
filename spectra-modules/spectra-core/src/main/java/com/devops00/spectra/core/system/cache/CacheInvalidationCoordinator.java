/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 普通缓存的本地清理和多实例失效事件协调器。
 *
 * <p>事件只携带操作 ID、区域编码和实例范围，不携带 Redis Key。发布成功
 * 只代表消息已被普通 Redis 接受，不代表所有实例已经完成。</p>
 */
@Component
public class CacheInvalidationCoordinator {

    public static final String CHANNEL = "spectra:cache:invalidation";

    private final CacheRegionRegistry registry;
    private final RedisTemplate<String, Object> redis;
    private final Set<UUID> applied = ConcurrentHashMap.newKeySet();
    private final Map<UUID, CacheManagementResult> operations = new ConcurrentHashMap<>();

    public CacheInvalidationCoordinator(CacheRegionRegistry registry,
                                        @Qualifier("redisTemplate") RedisTemplate<String, Object> redis) {
        this.registry = registry;
        this.redis = redis;
    }

    /** 清理当前实例，并按需要发布普通缓存失效事件。 */
    public CacheManagementResult clear(Collection<String> regionCodes, boolean allRegions,
                                       boolean allInstances, UUID operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("缓存清理操作 ID 不能为空");
        }
        CacheManagementResult existing = operations.get(operationId);
        if (existing != null) {
            return existing;
        }
        if (allRegions) {
            registry.validate(null);
        } else {
            registry.validate(regionCodes);
        }

        CacheManagementResult result;
        try {
            long cleared = applied.add(operationId) ? registry.clear(regionCodes, allRegions) : 0L;
            boolean broadcastAccepted = !allInstances || publish(operationId, regionCodes, allRegions);
            String status = broadcastAccepted ? "SUCCEEDED" : "PARTIAL";
            String message = broadcastAccepted
                    ? (allInstances ? "本实例已清理，普通 Redis 已接受多实例失效消息" : "本实例已清理")
                    : "本实例已清理，但多实例失效消息未确认";
            result = new CacheManagementResult(operationId, status, cleared, broadcastAccepted, message);
        } catch (DataAccessException exception) {
            applied.remove(operationId);
            result = new CacheManagementResult(operationId, "PARTIAL", 0L, false,
                    "普通缓存协调服务不可用");
        } catch (RuntimeException exception) {
            applied.remove(operationId);
            result = new CacheManagementResult(operationId, "FAILED", 0L, false, "普通缓存清理失败");
        }
        operations.put(operationId, result);
        return result;
    }

    /** 返回当前实例已知的操作状态；未知操作不伪造成功。 */
    public CacheManagementResult status(UUID operationId) {
        return operations.get(operationId);
    }

    /** 供 Redis 订阅适配器调用的本地幂等应用入口。 */
    public CacheManagementResult applyEvent(String wireMessage) {
        String[] fields = wireMessage == null ? new String[0] : wireMessage.split(";", -1);
        if (fields.length != 3) {
            throw new IllegalArgumentException("普通缓存失效事件格式无效");
        }
        UUID operationId = UUID.fromString(fields[0]);
        if (!"true".equals(fields[1]) && !"false".equals(fields[1])) {
            throw new IllegalArgumentException("普通缓存失效事件范围无效");
        }
        boolean allRegions = Boolean.parseBoolean(fields[1]);
        if (!allRegions && fields[2].isBlank()) {
            throw new IllegalArgumentException("普通缓存失效事件区域不能为空");
        }
        Collection<String> regions = allRegions ? Set.of() : Set.of(fields[2].split(","));
        return clear(regions, allRegions, false, operationId);
    }

    private boolean publish(UUID operationId, Collection<String> regionCodes, boolean allRegions) {
        String regions = allRegions ? "*" : String.join(",", registry.validate(regionCodes).stream()
                .map(CacheRegionDescriptor::code).toList());
        redis.convertAndSend(CHANNEL, operationId + ";" + allRegions + ";" + regions);
        return true;
    }
}
