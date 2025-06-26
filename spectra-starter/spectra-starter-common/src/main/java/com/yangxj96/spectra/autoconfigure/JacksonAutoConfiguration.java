package com.yangxj96.spectra.autoconfigure;


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
import com.yangxj96.spectra.properties.JacksonProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@AutoConfigureBefore(org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
@ConditionalOnProperty(name = "spectra.jackson.enable", havingValue = "true", matchIfMissing = true)
public class JacksonAutoConfiguration implements Jackson2ObjectMapperBuilderCustomizer {

    private static final String PREFIX = "[Jackson]:";

    private final JacksonProperties properties;

    public JacksonAutoConfiguration(JacksonProperties properties) {
        this.properties = properties;
    }

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
        var sdf = new SimpleDateFormat(properties.getLocalDateTimeFormat());
        log.atDebug().log("{} 加载时间格式化", PREFIX);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        builder.dateFormat(sdf);
        log.atDebug().log("{} 加载java8新时间序列化", PREFIX);
        var serializers = new HashMap<Class<?>, JsonSerializer<?>>();
        serializers.put(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
        serializers.put(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
        serializers.put(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));
        builder.serializersByType(serializers);
        log.atDebug().log("{} 加载java8新时间反序列化", PREFIX);
        var deserializers = new HashMap<Class<?>, JsonDeserializer<?>>();
        deserializers.put(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateTimeFormat())));
        deserializers.put(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(properties.getLocalDateFormat())));
        deserializers.put(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(properties.getLocalTimeFormat())));
        builder.deserializersByType(deserializers);
        log.atDebug().log("{} 配置完成", PREFIX);
    }
}
