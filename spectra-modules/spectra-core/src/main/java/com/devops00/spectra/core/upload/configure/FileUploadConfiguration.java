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

package com.devops00.spectra.core.upload.configure;

import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.properties.LocalProperties;
import com.devops00.spectra.core.upload.properties.S3Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 配置文件上传相关的组件及其依赖。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Configuration
@EnableConfigurationProperties({FileUploadProperties.class, LocalProperties.class, S3Properties.class})
public class FileUploadConfiguration {

    /**
     * 处理文件上传任务相关数据。
     *
     * @param properties 文件上传功能的配置属性。
     * @return 任务数据。
     */
    @Bean(name = "fileUploadTaskExecutor")
    public TaskExecutor fileUploadTaskExecutor(FileUploadProperties properties) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getParallelism());
        executor.setMaxPoolSize(properties.getParallelism());
        executor.setQueueCapacity(properties.getParallelism() * 2);
        executor.setThreadNamePrefix("file-upload-");
        executor.initialize();
        return executor;
    }
}
