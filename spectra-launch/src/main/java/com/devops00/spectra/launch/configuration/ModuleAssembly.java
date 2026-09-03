/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.launch.configuration;

import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 启动模块的能力装配描述。
 *
 * <p>该类型只描述模块之间的装配约束，不持有业务 Bean、Entity 或 Mapper。未来新增 ERP
 * 等模块时，只要由 launch 提供对应自动配置入口，就可以在不修改 core 的前提下加入能力。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/31
 */
public record ModuleAssembly(Set<String> enabledModules) {

    public static final String CORE = "core";
    public static final String OA = "oa";
    public static final String WORKFLOW = "workflow";

    private static final List<String> KNOWN_OPTIONAL_MODULES = List.of(WORKFLOW, OA);

    private static final Map<String, List<String>> REQUIRED_MODULES = Map.of(
            OA, List.of(WORKFLOW));

    public ModuleAssembly {
        var normalizedModules = new LinkedHashSet<>(enabledModules);
        if (!normalizedModules.contains(CORE)) {
            throw new IllegalArgumentException("模块装配失败：spectra-core 是系统必需模块，不能被禁用");
        }
        enabledModules = Set.copyOf(normalizedModules);
    }

    /**
     * 从启动环境读取模块启用状态；缺省值为启用，以保持现有全量 launch 的行为。
     *
     * @param environment 启动环境
     * @return 模块装配描述
     */
    public static ModuleAssembly from(Environment environment) {
        var enabledModules = new LinkedHashSet<String>();
        enabledModules.add(CORE);
        KNOWN_OPTIONAL_MODULES.stream()
                .filter(module -> Boolean.parseBoolean(environment.getProperty(
                        "spectra.modules." + module + ".enabled", "true")))
                .forEach(enabledModules::add);
        return new ModuleAssembly(enabledModules);
    }

    /**
     * 创建测试或装配预览使用的模块集合。
     *
     * @param modules 模块名称
     * @return 模块装配描述
     */
    public static ModuleAssembly of(String... modules) {
        return new ModuleAssembly(new LinkedHashSet<>(Arrays.asList(modules)));
    }

    /**
     * 校验已启用模块的必需 adapter 是否同时装配。
     */
    public void validate() {
        REQUIRED_MODULES.forEach((module, requiredModules) -> {
            if (!enabledModules.contains(module)) {
                return;
            }
            requiredModules.forEach(requiredModule -> {
                if (!enabledModules.contains(requiredModule)) {
                    throw new IllegalStateException("模块装配失败：已启用 spectra.modules." + module
                            + ".enabled=true，但缺少必需 adapter 模块 '" + requiredModule
                            + "'。请在 spectra-launch 引入该模块并确保其启用，或关闭当前模块。");
                }
            });
        });
    }
}
