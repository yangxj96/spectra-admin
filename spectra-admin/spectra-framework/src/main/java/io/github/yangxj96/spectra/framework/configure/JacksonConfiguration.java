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
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.github.yangxj96.spectra.common.properties.JacksonProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
public class JacksonConfiguration implements Jackson2ObjectMapperBuilderCustomizer {

    private static final String PREFIX = "[Jackson]:";

    private final JacksonProperties properties;

    public JacksonConfiguration(JacksonProperties properties) {
        this.properties = properties;
    }

    @Override
    public void customize(Jackson2ObjectMapperBuilder builder) {
        log.debug(PREFIX + "自定义ObjectMapper");
        log.debug(PREFIX + "注册java8时间模块");
        builder.modules(new JavaTimeModule());
        log.debug(PREFIX + "不显示null元素");
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        log.debug(PREFIX + "格式化响应字段为下划线分割");
        builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        log.debug(PREFIX + "设置时区为UTC");
        builder.timeZone(TimeZone.getTimeZone("UTC"));
        var sdf = new SimpleDateFormat(properties.getLocalDateTimeFormat());
        log.debug(PREFIX + "加载时间格式化");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        builder.dateFormat(sdf);
        log.debug(PREFIX + "加载java8新时间序列化");
        var serializers = new HashMap<Class<?>, JsonSerializer<?>>();
        serializers.put(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
        serializers.put(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
        serializers.put(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));
        builder.serializersByType(serializers);
        log.debug(PREFIX + "加载java8新时间反序列化");
        var deserializers = new HashMap<Class<?>, JsonDeserializer<?>>();
        deserializers.put(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
        deserializers.put(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
        deserializers.put(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));
        builder.deserializersByType(deserializers);
        log.debug(PREFIX + "配置完成");
    }
}
