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

package io.github.yangxj96.spectra.framework.configure;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.yangxj96.spectra.common.properties.JacksonProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * jackson相关配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(JacksonProperties.class)
public class JacksonConfiguration {

    private static final String PREFIX = "[Jackson]:";

    private final JacksonProperties properties;

    public JacksonConfiguration(JacksonProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ObjectMapper objectMapper() {
        log.debug("{}开始配置jackson3", PREFIX);

        log.debug("{}java.time包下的时间格式化处理", PREFIX);
        // 注册 JavaTimeModule
        var javaTimeModule = new SimpleModule();

        // 添加自定义序列化器
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));

        // 添加反序列化器
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
        javaTimeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));

        // 理论上是非线程安全的,如果用不到传统time类,可以注释掉
        log.debug("{}传统time进行处理", PREFIX);
        var sdf = new SimpleDateFormat(properties.getLocalDateTimeFormat());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        log.debug("{}配置响应", PREFIX);
        return JsonMapper
                // 接近原来jackson2的默认配置,但是好像还不是完全等于.
                .builderWithJackson2Defaults()
                // 忽略控制
                .changeDefaultPropertyInclusion(_ -> JsonInclude.Value.construct(
                        JsonInclude.Include.NON_NULL,
                        JsonInclude.Include.ALWAYS
                ))
                // 响应下划线分割
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                // 旧的时间进行格式化
                .defaultDateFormat(sdf)
                // 新时间格式化
                .addModule(javaTimeModule)
                // 默认时区
                .defaultTimeZone(TimeZone.getTimeZone("UTC"))
                .build();
    }

}
