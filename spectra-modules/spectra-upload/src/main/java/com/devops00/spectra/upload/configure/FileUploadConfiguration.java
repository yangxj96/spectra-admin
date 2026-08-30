/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.configure;

import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.properties.LocalProperties;
import com.devops00.spectra.upload.properties.S3Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Upload protocol configuration. */
@Configuration
@EnableConfigurationProperties({FileUploadProperties.class, LocalProperties.class, S3Properties.class})
public class FileUploadConfiguration {

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
