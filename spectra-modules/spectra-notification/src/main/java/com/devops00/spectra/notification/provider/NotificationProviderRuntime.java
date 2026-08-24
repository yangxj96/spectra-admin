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

package com.devops00.spectra.notification.provider;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderHealth;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderHealthState;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.properties.NotificationModuleProperties;
import com.devops00.spectra.notification.service.NotificationProviderAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provider 运行时协调器；健康检查未通过前不允许外部渠道投递。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Component
@RequiredArgsConstructor
public class NotificationProviderRuntime {

    /**
     * 配置服务。
     */
    private final NotificationProviderAdminService configurationService;

    /**
     * Provider SPI 实现。
     */
    private final List<NotificationProvider> providers;

    /**
     * 通知模块开关。
     */
    private final NotificationModuleProperties properties;

    /**
     * 最近一次健康检查；配置更新时间变化后自动失效。
     */
    private final ConcurrentHashMap<NotificationChannel, HealthSnapshot> healthSnapshots = new ConcurrentHashMap<>();

    /**
     * 立即执行指定渠道健康检查并刷新缓存。
     */
    public NotificationProviderHealth check(NotificationChannel channel) {
        var checkedAt = Instant.now();
        if (!properties.enabled()) {
            return cache(channel, null, NotificationProviderHealth.blocked("MODULE_DISABLED", checkedAt));
        }
        var configuration = configurationService.resolve(channel);
        if (configuration.providerType() == null || configuration.providerType().isBlank()) {
            return cache(channel, configuration, NotificationProviderHealth.notConfigured("PROVIDER_NOT_CONFIGURED", checkedAt));
        }
        if (!configuration.enabled()) {
            return cache(channel, configuration, NotificationProviderHealth.disabled("DISABLED_BY_CONFIGURATION", checkedAt));
        }
        var provider = findProvider(channel, configuration.providerType());
        if (provider == null) {
            return cache(channel, configuration, NotificationProviderHealth.blocked("PROVIDER_NOT_REGISTERED", checkedAt));
        }
        try {
            return cache(channel, configuration, provider.health(configuration));
        } catch (RuntimeException exception) {
            return cache(channel, configuration, NotificationProviderHealth.unhealthy("PROVIDER_HEALTH_CHECK_FAILED", checkedAt));
        }
    }

    /**
     * 读取缓存的健康状态；未检查或配置已变化时返回明确的未健康状态。
     */
    public NotificationProviderHealth snapshot(NotificationChannel channel) {
        var configuration = configurationService.resolve(channel);
        var cached = healthSnapshots.get(channel);
        if (cached != null && Objects.equals(cached.configurationUpdatedAt(), configuration.updatedAt())) {
            return cached.health();
        }
        if (configuration.providerType() == null || configuration.providerType().isBlank()) {
            return NotificationProviderHealth.notConfigured("PROVIDER_NOT_CONFIGURED", Instant.now());
        }
        if (!configuration.enabled()) {
            return NotificationProviderHealth.disabled("DISABLED_BY_CONFIGURATION", Instant.now());
        }
        return NotificationProviderHealth.unhealthy("HEALTH_CHECK_REQUIRED", Instant.now());
    }

    /**
     * 判断指定外部渠道是否已经通过最近一次健康检查。
     */
    public boolean available(NotificationChannel channel) {
        return snapshot(channel).state() == NotificationProviderHealthState.HEALTHY;
    }

    /**
     * 返回未可用时的脱敏原因。
     */
    public String unavailableReason(NotificationChannel channel) {
        var health = snapshot(channel);
        return health.state() == NotificationProviderHealthState.HEALTHY ? "AVAILABLE" : health.reason();
    }

    /**
     * 将任务交给已通过健康检查的 Provider；否则明确阻断。
     */
    public ChannelSendResult send(NotificationChannel channel, NotificationTaskEntity task) {
        var configuration = configurationService.resolve(channel);
        var health = snapshot(channel);
        if (health.state() != NotificationProviderHealthState.HEALTHY) {
            return ChannelSendResult.blocked(providerCode(configuration), null, health.reason());
        }
        var provider = findProvider(channel, configuration.providerType());
        if (provider == null) {
            return ChannelSendResult.blocked(providerCode(configuration), null, "PROVIDER_NOT_REGISTERED");
        }
        try {
            return provider.send(task, configuration);
        } catch (RuntimeException exception) {
            return ChannelSendResult.unknown(providerCode(configuration), null, "PROVIDER_FAILURE");
        }
    }

    /**
     * 查询或获取目标数据（{@code findProvider}）。
     */
    private NotificationProvider findProvider(NotificationChannel channel, String providerType) {
        return providers.stream()
                .filter(provider -> provider.code().equals(providerType) && provider.supports(channel))
                .findFirst()
                .orElse(null);
    }

    /**
     * 处理内部业务逻辑（{@code cache}）。
     */
    private NotificationProviderHealth cache(NotificationChannel channel,
                                             NotificationProviderConfiguration configuration,
                                             NotificationProviderHealth health) {
        healthSnapshots.put(channel, new HealthSnapshot(configuration == null ? null : configuration.updatedAt(), health));
        return health;
    }

    /**
     * 处理内部业务逻辑（{@code providerCode}）。
     */
    private String providerCode(NotificationProviderConfiguration configuration) {
        return configuration == null || configuration.providerType() == null ? "NONE" : configuration.providerType();
    }

    private record HealthSnapshot(Instant configurationUpdatedAt, NotificationProviderHealth health) {
    }
}
