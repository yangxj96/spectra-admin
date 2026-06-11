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

package com.devops00.spectra.launch;

import dev.langchain4j.http.client.jdk.JdkHttpClientBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/// 启动类
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/7/23
@Slf4j
@SpringBootApplication
public class LaunchApplication {

    private LaunchApplication() {
    }

    static void main(String[] args) {
        System.setProperty("langchain4j.http.clientBuilderFactory", "dev.langchain4j.http.client.jdk.JdkHttpClientBuilderFactory");
        // 强制程序整体使用UTC时区.在展示的时候在格式化为对应时区
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(LaunchApplication.class, args);
    }

}
