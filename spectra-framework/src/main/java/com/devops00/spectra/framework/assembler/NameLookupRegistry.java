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

package com.devops00.spectra.framework.assembler;

import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NameLookup 显式类型注册表。
 * <p>
 * 注册表在应用启动时接收全部 Lookup Bean，并按其用户声明的实现类型建立不可变索引。
 * {@link NameFillExecutor} 只通过本注册表解析注解声明的 Lookup，避免在装配过程中隐式访问
 * {@code ApplicationContext}。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
@Component
public final class NameLookupRegistry {

    private final Map<Class<? extends NameLookup<?>>, NameLookup<?>> lookups;

    /**
     * 创建名称查询注册表，并在启动阶段校验 Lookup 实现类型是否唯一。
     *
     * @param lookupBeans Spring 容器发现的全部名称查询 Bean；每个 Bean 必须实现 {@link NameLookup}
     * @throws IllegalStateException 当多个 Bean 声明同一个 Lookup 实现类型时抛出
     */
    public NameLookupRegistry(List<NameLookup<?>> lookupBeans) {
        Map<Class<? extends NameLookup<?>>, NameLookup<?>> index = new HashMap<>();
        for (NameLookup<?> lookup : lookupBeans) {
            Class<? extends NameLookup<?>> lookupType = lookupTypeOf(lookup);
            if (index.putIfAbsent(lookupType, lookup) != null) {
                throw new IllegalStateException("NameLookup 类型重复注册: " + lookupType.getName());
            }
        }
        this.lookups = Map.copyOf(index);
    }

    /**
     * 按注解声明的 Lookup 实现类型取得名称查询 Bean。
     *
     * @param lookupType {@link NameFill} 指定的名称查询实现类型
     * @return 已注册且可执行批量名称查询的 Bean；不会返回 null
     * @throws IllegalArgumentException 当类型为 null 或没有对应注册 Bean 时抛出
     */
    public NameLookup<?> require(Class<? extends NameLookup<?>> lookupType) {
        if (lookupType == null) {
            throw new IllegalArgumentException("NameLookup 类型不能为空");
        }
        NameLookup<?> lookup = lookups.get(lookupType);
        if (lookup == null) {
            throw new IllegalArgumentException("NameLookup 未注册: " + lookupType.getName());
        }
        return lookup;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends NameLookup<?>> lookupTypeOf(NameLookup<?> lookup) {
        Class<?> targetClass = AopUtils.getTargetClass(lookup);
        return (Class<? extends NameLookup<?>>) targetClass;
    }
}
