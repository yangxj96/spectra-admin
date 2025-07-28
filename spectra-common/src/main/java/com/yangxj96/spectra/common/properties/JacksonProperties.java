package com.yangxj96.spectra.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Jackson配置
 */
@Data
@ConfigurationProperties(prefix = "spectra.jackson")
public class JacksonProperties {

    /**
     * LocalDateTime类序列化方式.
     */
    private String localDateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * LocalDate类序列化方式.
     */
    private String localDateFormat = "yyyy-MM-dd";

    /**
     * LocalTime类序列化方式.
     */
    private String localTimeFormat = "HH:mm:ss";

}
