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

package com.devops00.spectra.core.quartz.catalog;

import com.devops00.spectra.common.port.quartz.QuartzJobDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 不可变、无重复的代码白名单目录，不保存 Quartz 运行状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Slf4j
@Component
public class QuartzJobCatalog {

    private final Map<String, QuartzJobDefinition> definitions;

    /** 校验并冻结 Spring 注入的全部 Job 定义。 */
    public QuartzJobCatalog(List<QuartzJobDefinition> definitions) {
        var source = definitions == null ? List.<QuartzJobDefinition>of() : definitions;
        source.forEach(this::validateDefinition);
        this.definitions = source.stream()
                .collect(Collectors.toUnmodifiableMap(
                        QuartzJobDefinition::typeKey, Function.identity(), (first, second) -> {
                            throw new IllegalArgumentException("Quartz Job 类型键重复: " + first.typeKey());
                        }));
    }

    /** 按类型键查询白名单定义。 */
    public Optional<QuartzJobDefinition> find(String typeKey) {
        return Optional.ofNullable(definitions.get(typeKey));
    }

    /** 返回不可变 Job 定义列表。 */
    public List<QuartzJobDefinition> definitions() {
        return List.copyOf(definitions.values());
    }

    /** 校验单个 Job 定义的受信任边界。 */
    private void validateDefinition(QuartzJobDefinition definition) {
        if (definition == null || definition.typeKey() == null || definition.typeKey().isBlank()) {
            throw new IllegalArgumentException("Quartz Job 类型键不能为空");
        }
        if (definition.displayName() == null || definition.displayName().isBlank()) {
            throw new IllegalArgumentException("Quartz Job 展示名称不能为空: " + definition.typeKey());
        }
        if (definition.jobClass() == null || !org.quartz.Job.class.isAssignableFrom(definition.jobClass())) {
            throw new IllegalArgumentException("Quartz Job 实现类不合法: " + definition.typeKey());
        }
        if (definition.parameterSchema() == null) {
            throw new IllegalArgumentException("Quartz Job 参数 schema 不能为空: " + definition.typeKey());
        }
        if (definition.builtIn() && definition.builtInJobKey().isEmpty()) {
            throw new IllegalArgumentException("内置 Quartz Job 必须声明固定 JobKey: " + definition.typeKey());
        }
        definition.defaultTrigger().ifPresent(trigger -> {
            if (trigger.triggerType() == null) {
                throw new IllegalArgumentException("Quartz Trigger 类型不能为空: " + definition.typeKey());
            }
        });
        log.debug("加载 Quartz Job 定义: typeKey={}, builtIn={}", definition.typeKey(), definition.builtIn());
    }
}
