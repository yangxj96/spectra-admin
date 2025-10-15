/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.framework.runner;

import io.github.yangxj96.spectra.common.properties.SystemProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 文件夹检查
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@Component
public class FolderCheckRunner implements ApplicationRunner {

    @Resource
    private SystemProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        var folder = new File(properties.getBaseDir());
        if (!folder.exists()) {
            var created = folder.mkdirs();
            if (created) {
                log.debug("已创建文件夹: {}", folder.getAbsolutePath());
            } else {
                log.debug("无法创建文件夹: {}", folder.getAbsolutePath());
            }
        } else {
            log.debug("文件夹已存在: {}", folder.getAbsolutePath());
        }
    }
}
