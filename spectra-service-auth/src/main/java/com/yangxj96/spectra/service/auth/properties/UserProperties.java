package com.yangxj96.spectra.service.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
@ConfigurationProperties(prefix = "spectra.user")
public class UserProperties {

    /**
     * 默认密码
     */
    private String defaultPassword;

}