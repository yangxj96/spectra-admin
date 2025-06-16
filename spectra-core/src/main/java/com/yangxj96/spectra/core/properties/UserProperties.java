package com.yangxj96.spectra.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 用户配置相关
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/16
 */
@Data
@Component
@ConfigurationProperties(prefix = "spectra.user")
public class UserProperties {

    /**
     * 默认密码
     */
    private String defaultPassword;

}
