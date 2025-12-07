package io.github.yangxj96.spectra.core.configure.mvc.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MVC相关配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/5 14:48
 */
@Data
@ConfigurationProperties(prefix = "spectra.system.mvc")
public class MvcProperties {

    /**
     * api版本号请求头
     */
    private String apiHeader = "Api-Version";

    /**
     * 默认API版本号
     */
    private String apiVersion = "1.0.0";

}
