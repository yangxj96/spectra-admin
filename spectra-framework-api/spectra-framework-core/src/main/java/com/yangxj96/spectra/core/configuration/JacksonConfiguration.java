/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.core.configuration;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
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
 * JackJSON配置
 *
 * @author 杨新杰
 * @since 2025/5/26 16:38
 */
@Slf4j
@Configuration
public class JacksonConfiguration implements Jackson2ObjectMapperBuilderCustomizer {

    private static final String PREFIX = "[Jackson]:";

    /**
     * LocalDateTime类序列化方式.
     */
    private static final String LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * LocalDate类序列化方式.
     */
    private static final String LOCAL_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * LocalTime类序列化方式.
     */
    private static final String LOCAL_TIME_FORMAT = "HH:mm:ss";


    @Override
    public void customize(Jackson2ObjectMapperBuilder builder) {
        log.atDebug().log("{} 自定义ObjectMapper", PREFIX);
        log.atDebug().log("{} 注册java8时间模块", PREFIX);
        builder.modules(new JavaTimeModule());
        log.atDebug().log("{} 不显示null元素", PREFIX);
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        log.atDebug().log("{} 格式化响应字段为下划线分割", PREFIX);
        builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        log.atDebug().log("{} 设置时区为UTC", PREFIX);
        builder.timeZone(TimeZone.getTimeZone("UTC"));
        var sdf = new SimpleDateFormat(LOCAL_DATE_TIME_FORMAT);
        log.atDebug().log("{} 加载时间格式化", PREFIX);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        builder.dateFormat(sdf);
        log.atDebug().log("{} 加载java8新时间序列化", PREFIX);
        var serializers = new HashMap<Class<?>, JsonSerializer<?>>();
        serializers.put(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT)));
        serializers.put(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT)));
        serializers.put(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(LOCAL_TIME_FORMAT)));
        builder.serializersByType(serializers);
        log.atDebug().log("{} 加载java8新时间反序列化", PREFIX);
        var deserializers = new HashMap<Class<?>, JsonDeserializer<?>>();
        deserializers.put(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT)));
        deserializers.put(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT)));
        deserializers.put(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(LOCAL_TIME_FORMAT)));
        builder.deserializersByType(deserializers);
        log.atDebug().log("{} 配置完成", PREFIX);
    }
}
