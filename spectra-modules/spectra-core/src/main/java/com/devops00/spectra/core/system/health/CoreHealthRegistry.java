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

package com.devops00.spectra.core.system.health;

import com.devops00.spectra.common.health.DependencyHealthContributor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core 统一健康 contributor 注册表。
 *
 * <p>注册表只收集 Spring 已装配的 contributor，不创建可选模块的伪实现；名称、模块、依赖类型和超时预算
 * 在启动阶段完成校验，避免同名 contributor 被后装配的 Bean 静默覆盖。</p>
 */
@Component
public class CoreHealthRegistry {

    private final List<DependencyHealthContributor> contributors;
    private final Map<String, ContributorMetadata> metadata;

    public CoreHealthRegistry(List<DependencyHealthContributor> contributors) {
        var source = contributors == null ? List.<DependencyHealthContributor>of() : contributors;
        var names = new HashMap<String, DependencyHealthContributor>();
        var metadata = new HashMap<String, ContributorMetadata>();
        for (var contributor : source) {
            if (contributor == null) {
                throw new IllegalStateException("健康 contributor 不能为 null");
            }
            var name = requireText(contributor.contributorName(), "contributorName");
            var module = requireText(contributor.moduleName(), "moduleName");
            var dependencyType = requireText(contributor.dependencyType(), "dependencyType");
            var timeout = contributor.timeout();
            if (timeout == null || timeout.isZero() || timeout.isNegative()) {
                throw new IllegalStateException("健康 contributor 超时预算必须为正数: " + name);
            }
            if (names.putIfAbsent(name, contributor) != null) {
                throw new IllegalStateException("健康 contributor 名称重复: " + name);
            }
            metadata.put(name, new ContributorMetadata(name, module, dependencyType, timeout));
        }
        this.contributors = names.values()
                .stream()
                .sorted(java.util.Comparator.comparing(DependencyHealthContributor::contributorName))
                .toList();
        this.metadata = Map.copyOf(metadata);
    }

    /**
     * 返回按名称稳定排序的已注册 contributor。
     *
     * @return 不可变 contributor 列表
     */
    public List<DependencyHealthContributor> contributors() {
        return contributors;
    }

    /**
     * 查询 contributor 的归属元数据。
     *
     * @param contributorName contributor 名称
     * @return 模块、依赖类型和超时预算
     */
    public ContributorMetadata metadata(String contributorName) {
        var value = metadata.get(contributorName);
        if (value == null) {
            throw new IllegalArgumentException("健康 contributor 未注册: " + contributorName);
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("健康 contributor " + field + " 不能为空");
        }
        return value.trim();
    }

    /** 已注册 contributor 的只读归属元数据。 */
    public record ContributorMetadata(String contributorName, String moduleName,
                                      String dependencyType, Duration timeout) {
    }
}
