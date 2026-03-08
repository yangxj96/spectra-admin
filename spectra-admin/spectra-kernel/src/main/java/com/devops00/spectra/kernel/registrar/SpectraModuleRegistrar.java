package com.devops00.spectra.kernel.registrar;


import com.devops00.spectra.kernel.manager.SpectraModuleManager;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/// Spring Bean 注册器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:34
@Slf4j
@NullMarked
public class SpectraModuleRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        // 发现模块
        var manager = new SpectraModuleManager();
        var modules = manager.discoverModules();

        // 拓扑
        modules = manager.sortModules(modules);

        var scanner = new ClassPathBeanDefinitionScanner(registry);

        for (var module : modules) {

            // 扫描模块包
            for (String pkg : module.getScanPackages()) {
                scanner.scan(pkg);
            }

            // 注册 Mapper
            for (String mapperPkg : module.getMapperPackages()) {
                manager.registerMapperScan(registry, mapperPkg);
            }
        }

        // 执行生命周期
        manager.initializeModules(modules);
    }


}
