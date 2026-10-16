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

package com.devops00.spectra.workflow.configuration;

import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 工作流配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/18 14:53
 */
@Configuration
public class WorkflowConfiguration {

    /**
     * 配置流程引擎
     *
     * <p>数据库 schema 更新只跟随 {@code flowable.database-schema-update} 配置，缺省为 false；
     * 生产必须由 Flyway 迁移基线管理表结构，运行角色无 DDL 权限，不能由引擎无条件尝试建表。</p>
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineConfigurer(Environment environment) {
        return configuration -> {
            // 只按配置决定是否允许 schema 自动更新，缺省 false，避免生产绕过 Flyway
            configuration.setDatabaseSchemaUpdate(
                    environment.getProperty("flowable.database-schema-update", "false"));
            // 配置流程定义部署
            configuration.setDeploymentMode("single-resource");
            // 配置流程图生成器字体（中文支持）
            configuration.setActivityFontName("宋体");
            configuration.setLabelFontName("宋体");
            configuration.setAnnotationFontName("宋体");
        };
    }

    /**
     * 配置流程图生成器
     */
    @Bean
    public ProcessDiagramGenerator processDiagramGenerator() {
        return new DefaultProcessDiagramGenerator();
    }
}
