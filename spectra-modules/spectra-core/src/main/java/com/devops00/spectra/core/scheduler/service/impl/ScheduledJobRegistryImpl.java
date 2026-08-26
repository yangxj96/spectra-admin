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

package com.devops00.spectra.core.scheduler.service.impl;

import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobHandler;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledLoopHandler;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.scheduler.service.ScheduledJobRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 默认调度处理器注册表。 */
@Component
public class ScheduledJobRegistryImpl implements ScheduledJobRegistry {

    private final Map<String, ScheduledJobDescriptor> descriptors;
    private final Map<String, ScheduledJobHandler> jobHandlers;
    private final Map<String, ScheduledLoopHandler> loopHandlers;

    public ScheduledJobRegistryImpl(List<ScheduledJobHandler> jobHandlers, List<ScheduledLoopHandler> loopHandlers) {
        var descriptorMap = new LinkedHashMap<String, ScheduledJobDescriptor>();
        var discreteMap = new LinkedHashMap<String, ScheduledJobHandler>();
        var loopMap = new LinkedHashMap<String, ScheduledLoopHandler>();
        var handlerKeys = new HashSet<String>();
        for (var handler : safeList(jobHandlers)) {
            var descriptor = requireDescriptor(handler.descriptor());
            if (descriptor.jobType() == ScheduledJobType.LOOP) {
                throw new IllegalStateException("离散处理器不能注册 LOOP 任务: " + descriptor.jobKey());
            }
            registerDescriptor(descriptorMap, handlerKeys, descriptor);
            discreteMap.put(descriptor.jobKey(), handler);
        }
        for (var handler : safeList(loopHandlers)) {
            var descriptor = requireDescriptor(handler.descriptor());
            if (descriptor.jobType() != ScheduledJobType.LOOP
                    || descriptor.scheduleKind() != ScheduledScheduleKind.FIXED_DELAY) {
                throw new IllegalStateException("循环处理器必须注册 LOOP/FIXED_DELAY 任务: " + descriptor.jobKey());
            }
            registerDescriptor(descriptorMap, handlerKeys, descriptor);
            loopMap.put(descriptor.jobKey(), handler);
        }
        this.descriptors = Collections.unmodifiableMap(descriptorMap);
        this.jobHandlers = Collections.unmodifiableMap(discreteMap);
        this.loopHandlers = Collections.unmodifiableMap(loopMap);
    }

    @Override
    public Collection<ScheduledJobDescriptor> descriptors() {
        return Collections.unmodifiableCollection(new ArrayList<>(descriptors.values()));
    }

    @Override
    public Optional<ScheduledJobDescriptor> find(String jobKey) {
        return Optional.ofNullable(descriptors.get(jobKey));
    }

    @Override
    public Optional<ScheduledJobHandler> findJobHandler(String jobKey) {
        return Optional.ofNullable(jobHandlers.get(jobKey));
    }

    @Override
    public Optional<ScheduledLoopHandler> findLoopHandler(String jobKey) {
        return Optional.ofNullable(loopHandlers.get(jobKey));
    }

    private static ScheduledJobDescriptor requireDescriptor(ScheduledJobDescriptor descriptor) {
        if (descriptor == null) {
            throw new IllegalStateException("调度处理器没有返回任务描述");
        }
        return descriptor;
    }

    private static void registerDescriptor(Map<String, ScheduledJobDescriptor> descriptors,
                                           HashSet<String> handlerKeys,
                                           ScheduledJobDescriptor descriptor) {
        var previous = descriptors.putIfAbsent(descriptor.jobKey(), descriptor);
        if (previous != null) {
            throw new IllegalStateException("重复注册调度任务: " + descriptor.jobKey());
        }
        if (!handlerKeys.add(descriptor.handlerKey())) {
            descriptors.remove(descriptor.jobKey());
            throw new IllegalStateException("重复注册调度处理器: " + descriptor.handlerKey());
        }
    }

    private static <T> List<T> safeList(List<T> handlers) {
        return handlers == null ? List.of() : handlers;
    }
}
