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

package com.devops00.spectra.framework.persistence.scope.context;

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
     *
     * @param tableName SQL 中待查找数据权限元数据的表名，可包含 schema 前缀和引号。
     * @return 返回指定表名对应的 {@link DataScope} 元数据；表名为空、未注册或仅存在格式不匹配时返回 null。
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
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("数据权限实体加载失败: " + className, exception);
        } catch (LinkageError error) {
            throw new IllegalStateException("数据权限实体依赖加载失败: " + className, error);
        }
    }
}
