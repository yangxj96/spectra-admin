package com.devops00.spectra.framework.configure.mvc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 国密配置
 */
@Data
@ConfigurationProperties(prefix = "spectra.system.sm")
public class SMProperties {

    /**
     * 公钥(Base64格式).
     */
    private String publicKey;


    /**
     * 私钥(Base64格式).
     */
    private String privateKey;

}