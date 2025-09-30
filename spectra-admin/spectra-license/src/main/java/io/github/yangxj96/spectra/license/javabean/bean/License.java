package io.github.yangxj96.spectra.license.javabean.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * 许可证实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class License implements Serializable {

    /**
     * 自定义ID
     */
    private Long id;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 注册时间
     */
    private Instant issuedAt;

    /**
     * 到期时间
     */
    private Instant expiresAt;

    /**
     * 硬件ID
     */
    private String hwid;

    /**
     * 签名
     */
    private String signature;

}
