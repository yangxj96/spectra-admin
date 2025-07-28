package com.yangxj96.spectra.common.properties;

import com.yangxj96.spectra.common.enums.KaptchaType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码生成相关配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/25
 */
@Data
@ConfigurationProperties(prefix = "spectra.kaptcha")
public class KaptchaProperties {

    /**
     * 登录是否验证
     */
    private Boolean verify = Boolean.TRUE;

    /**
     * 是什么类型的验证码
     */
    private KaptchaType type = KaptchaType.MATH;

}
