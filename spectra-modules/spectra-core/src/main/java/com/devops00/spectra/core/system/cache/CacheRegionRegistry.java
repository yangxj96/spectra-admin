/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 普通业务缓存区域注册表。
 *
 * <p>注册表是普通缓存管理的唯一入口。底层 CacheManager 中即使存在未登记
 * 的缓存，也不会因为它存在而自动获得清理权限。</p>
 */
@Component
public class CacheRegionRegistry {

    private static final List<CacheRegionDescriptor> REGISTERED = List.of(
            new CacheRegionDescriptor("core:dept", "部门名称缓存", "RedisCacheManager", "REMOTE",
                    3_600L, false, true));

    private final CacheManager cacheManager;
    private final Map<String, CacheRegionDescriptor> descriptors;

    public CacheRegionRegistry(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        var entries = new LinkedHashMap<String, CacheRegionDescriptor>();
        for (CacheRegionDescriptor descriptor : REGISTERED) {
            if (descriptor.code().startsWith("sec:")) {
                throw new IllegalStateException("安全缓存不得注册为普通缓存区域");
            }
            entries.put(descriptor.code(), descriptor);
        }
        this.descriptors = Map.copyOf(entries);
    }

    /** 返回普通缓存区域白名单的稳定快照。 */
    public List<CacheRegionDescriptor> list() {
        return List.copyOf(descriptors.values());
    }

    /** 校验并返回一个已登记普通缓存区域。 */
    public CacheRegionDescriptor require(String code) {
        if (code == null || code.isBlank() || code.startsWith("sec:")) {
            throw new IllegalArgumentException("普通缓存区域无效");
        }
        CacheRegionDescriptor descriptor = descriptors.get(code);
        if (descriptor == null) {
            throw new IllegalArgumentException("普通缓存区域未登记");
        }
        return descriptor;
    }

    /** 校验一组区域；输入为空表示全部已登记普通缓存。 */
    public List<CacheRegionDescriptor> validate(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return list();
        }
        return codes.stream().distinct().map(this::require).toList();
    }

    /** 清理已登记区域的当前实例本地/远程缓存。 */
    public long clear(Collection<String> codes, boolean allRegions) {
        List<CacheRegionDescriptor> targets = allRegions ? list() : validate(codes);
        long cleared = 0L;
        for (CacheRegionDescriptor descriptor : targets) {
            if (!descriptor.supportsClear()) {
                continue;
            }
            Cache cache = cacheManager.getCache(descriptor.code());
            if (cache == null) {
                throw new IllegalStateException("普通缓存区域未被 CacheManager 提供");
            }
            cache.clear();
            cleared++;
        }
        return cleared;
    }

    /** 返回区域统计；不把不支持的指标伪装成零。 */
    public CacheStatistics statistics(CacheRegionDescriptor descriptor) {
        require(descriptor.code());
        return CacheStatistics.unsupported();
    }
}
