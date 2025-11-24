package io.github.yangxj96.spectra.core.javabean.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统配置分页响应
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguredVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 配置key
     */
    private String key;

    /**
     * 配置VALUE
     */
    private String value;

    /**
     * 备注说明
     */
    private String remarks;

}
