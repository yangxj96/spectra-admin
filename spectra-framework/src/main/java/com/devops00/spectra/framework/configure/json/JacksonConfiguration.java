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

package com.devops00.spectra.framework.configure.json;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.framework.configure.json.properties.JacksonProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * jackson 相关配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(JacksonProperties.class)
public class JacksonConfiguration {

    private final JacksonProperties properties;

    public JacksonConfiguration(JacksonProperties properties) {
        this.properties = properties;
    }

    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        log.debug(LogPrefix.SERIALIZATION.f("配置JsonMapper"));
        return builder -> {

            log.debug(LogPrefix.SERIALIZATION.f("新时间序列化"));
            // 新时间的序列化 module
            var javaTimeModule = new SimpleModule();

            // 添加自定义序列化器
            javaTimeModule.addSerializer(LocalDateTime.class,
                    new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
            javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));

            // 添加反序列化器
            javaTimeModule.addDeserializer(LocalDateTime.class,
                    new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
            javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));

            // 旧时间的序列化
            // 理论上是非线程安全的,如果用不到传统time类,可以注释掉
            log.debug(LogPrefix.SERIALIZATION.f("传统time进行处理"));
            var sdf = new SimpleDateFormat(properties.getLocalDateTimeFormat());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            log.debug(LogPrefix.SERIALIZATION.f("NON_NULL,SNAKE_CASE,MixIn"));
            // 构建详情
            builder.configureForJackson2();
            builder.changeDefaultPropertyInclusion(_ -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.ALWAYS));

            // 限制数字最大长度，防止 GHSA-72hv-8253-57qq
            // .configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, false)

            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            builder.defaultDateFormat(sdf);
            builder.addModule(javaTimeModule);
            builder.defaultTimeZone(TimeZone.getTimeZone("UTC"));
            builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        };
    }
}
