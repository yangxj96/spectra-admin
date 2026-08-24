/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.framework.configure.mybatis;

import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import org.springframework.beans.factory.config.BeanDefinition;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据资源元数据注册表。
 * <p>
 * 除了从 mappedStatementId 推导实体，还按实际表名注册所有 {@code @DataScope} 实体。
 * 这样 XML 自定义 SQL、别名或非标准 Mapper 命名无法绕过已声明的数据隔离。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Component
@Slf4j
public class DataScopeEntityRegistry {

    private final Map<String, DataScope> scopes = new ConcurrentHashMap<>();

    /**
     * 执行内部处理逻辑（{@code initialize}）。
     */
    @PostConstruct
    public void initialize() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(DataScope.class));
        try {
            for (BeanDefinition definition : scanner.findCandidateComponents("com.devops00.spectra")) {
                register(definition.getBeanClassName());
            }
        } catch (RuntimeException exception) {
            log.error("扫描数据权限实体失败", exception);
            throw exception;
        }
    }

    /**
     * 根据表名或带 schema 的表名查找数据权限元数据。
     */
    public DataScope find(String tableName) {
        if (tableName == null) {
            return null;
        }
        DataScope scope = scopes.get(tableName);
        return scope != null ? scope : scopes.get(tableName.replace("\"", ""));
    }

    /**
     * 执行内部处理逻辑（{@code register}）。
     */
    private void register(String className) {
        if (className == null) {
            return;
        }
        try {
            Class<?> entityClass = ClassUtils.forName(className, getClass().getClassLoader());
            DataScope dataScope = entityClass.getAnnotation(DataScope.class);
            TableName tableName = entityClass.getAnnotation(TableName.class);
            if (dataScope == null || tableName == null) {
                return;
            }
            scopes.put(tableName.value(), dataScope);
            if (!tableName.schema().isBlank()) {
                scopes.put(tableName.schema() + "." + tableName.value(), dataScope);
            }
        } catch (ClassNotFoundException | LinkageError ignored) {
            // 单个可选模块缺少依赖不应阻断框架启动；该资源仍会在 SQL 解析阶段按类名处理。
        }
    }
}
