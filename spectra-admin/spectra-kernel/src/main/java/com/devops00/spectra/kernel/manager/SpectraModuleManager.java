package com.devops00.spectra.kernel.manager;


import com.devops00.spectra.kernel.annotation.SpectraModule;
import com.devops00.spectra.kernel.lifecycle.SpectraModuleLifecycle;
import com.devops00.spectra.kernel.model.SpectraModuleDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.*;
import java.util.stream.Collectors;

/// 模块管理器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:34
@Slf4j
public class SpectraModuleManager {

    /// 发现模块：通过 SPI
    public List<SpectraModuleDescriptor> discoverModules() {
        var moduleInstances = new ArrayList<SpectraModuleLifecycle>();
        var loader = ServiceLoader.load(SpectraModuleLifecycle.class);

        loader.forEach(moduleInstances::add);

        var descriptors = new ArrayList<SpectraModuleDescriptor>();

        for (var instance : moduleInstances) {
            var annotation = instance.getClass().getAnnotation(SpectraModule.class);
            if (annotation == null) continue;

            var desc = new SpectraModuleDescriptor();
            desc.setModuleInstance(instance);
            desc.setName(annotation.name());
            desc.setScanPackages(annotation.scanPackages());
            desc.setMapperPackages(annotation.mapperPackages());
            desc.setDependsOn(annotation.dependsOn());
            desc.setOrder(annotation.order());

            descriptors.add(desc);
        }

        return descriptors;
    }

    /**
     * 简单拓扑排序
     */
    public List<SpectraModuleDescriptor> sortModules(List<SpectraModuleDescriptor> modules) {
        // 模块名 -> 模块映射
        var nameMap = modules
                .stream()
                .collect(Collectors.toMap(SpectraModuleDescriptor::getName, m -> m));

        var sorted = new ArrayList<SpectraModuleDescriptor>();
        var visited = new HashSet<String>();
        // 用于检测循环依赖
        var visiting = new HashSet<String>();

        for (var m : modules) {
            visitModule(m, nameMap, visited, visiting, sorted);
        }

        // 同级模块按 order 排序
        sorted.sort(Comparator.comparingInt(SpectraModuleDescriptor::getOrder));
        return sorted;
    }

    private void visitModule(SpectraModuleDescriptor module, Map<String, SpectraModuleDescriptor> nameMap, Set<String> visited, Set<String> visiting, List<SpectraModuleDescriptor> sorted) {
        var name = module.getName();
        if (visited.contains(name)) return;

        if (visiting.contains(name)) {
            throw new IllegalStateException("检测到模块循环依赖: " + name);
        }

        visiting.add(name);

        // 先访问依赖模块
        for (var depName : module.getDependsOn()) {
            var depModule = nameMap.get(depName);
            if (depModule != null) {
                visitModule(depModule, nameMap, visited, visiting, sorted);
            } else {
                // 可以根据需要抛异常或者忽略不存在的依赖
                throw new IllegalStateException("模块 " + name + " 依赖的模块不存在: " + depName);
            }
        }

        visiting.remove(name);
        visited.add(name);
        sorted.add(module);
    }

    /// Mapper扫描
    public void registerMapperScan(BeanDefinitionRegistry registry, String basePackage) {
        BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        builder.addPropertyValue("basePackage", basePackage);
        registry.registerBeanDefinition("mapperScan_" + basePackage, builder.getBeanDefinition());
    }

    /**
     * 执行模块生命周期
     */
    public void initializeModules(List<SpectraModuleDescriptor> modules) {
        for (var module : modules) {
            var instance = module.getModuleInstance();
            if (instance instanceof SpectraModuleLifecycle lifecycle) {
                log.debug("模块[{}]初始化之前", module.getName());
                lifecycle.beforeInitialize();
            }
        }
        for (var module : modules) {
            var instance = module.getModuleInstance();
            if (instance instanceof SpectraModuleLifecycle lifecycle) {
                log.debug("模块[{}]初始化之后", module.getName());
                lifecycle.afterInitialize();
            }
        }
        for (var module : modules) {
            var instance = module.getModuleInstance();
            if (instance instanceof SpectraModuleLifecycle lifecycle) {
                log.debug("模块[{}]启动", module.getName());
                lifecycle.onStart();
            }
        }
    }

}
