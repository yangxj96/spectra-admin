package com.devops00.spectra.kernel.manager;


import com.devops00.spectra.kernel.annotation.SpectraModule;
import com.devops00.spectra.kernel.lifecycle.SpectraModuleLifecycle;
import com.devops00.spectra.kernel.model.SpectraModuleDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

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
        modules.sort(Comparator.comparingInt(SpectraModuleDescriptor::getOrder));
        return modules;
    }


    /// Mapper扫描
    //public void registerMapperScan(BeanDefinitionRegistry registry, String basePackage) {
    //    BeanDefinitionBuilder builder =
    //            BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
    //    builder.addPropertyValue("basePackage", basePackage);
    //    registry.registerBeanDefinition("mapperScan_" + basePackage, builder.getBeanDefinition());
    //}

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
