package com.yangxj96.spectra.framework.configure;


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
import com.yangxj96.spectra.framework.properties.JacksonProperties;
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
        log.atDebug().log(PREFIX + "自定义ObjectMapper");
        log.atDebug().log(PREFIX + "注册java8时间模块");
        builder.modules(new JavaTimeModule());
        log.atDebug().log(PREFIX + "不显示null元素");
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        log.atDebug().log(PREFIX + "格式化响应字段为下划线分割");
        builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        log.atDebug().log(PREFIX + "设置时区为UTC");
        builder.timeZone(TimeZone.getTimeZone("UTC"));
        var sdf = new SimpleDateFormat(properties.getLocalDateTimeFormat());
        log.atDebug().log(PREFIX + "加载时间格式化");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        builder.dateFormat(sdf);
        log.atDebug().log(PREFIX + "加载java8新时间序列化");
        var serializers = new HashMap<Class<?>, JsonSerializer<?>>();
        serializers.put(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
        serializers.put(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
        serializers.put(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));
        builder.serializersByType(serializers);
        log.atDebug().log(PREFIX + "加载java8新时间反序列化");
        var deserializers = new HashMap<Class<?>, JsonDeserializer<?>>();
        deserializers.put(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
        deserializers.put(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
        deserializers.put(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));
        builder.deserializersByType(deserializers);
        log.atDebug().log(PREFIX + "配置完成");
    }
}
