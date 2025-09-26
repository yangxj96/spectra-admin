package io.github.yangxj96.spectra.license.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 许可相关配置
 */
@Data
@ConfigurationProperties(prefix = "spectra.system.license")
public class LicenseProperties {

    /**
     * 许可位置,当项目作为客户端的时候,验证许可的许可文件位置
     */
    private String licensePath;

    /**
     * 公钥位置
     */
    private String publicKey;

    /**
     * 私钥位置
     */
    private String privateKey;

}
