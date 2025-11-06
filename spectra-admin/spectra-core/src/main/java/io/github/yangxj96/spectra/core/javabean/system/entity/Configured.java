package io.github.yangxj96.spectra.core.javabean.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 系统配置表
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "SYS_CONFIG")
public class Configured extends BaseEntity implements Serializable {

    /**
     * 配置key
     */
    @TableField(value = "key")
    private String key;

    /**
     * 配置VALUE
     */
    @TableField(value = "value")
    private String value;

    /**
     * 备注说明
     */
    @TableField(value = "remarks")
    private String remarks;

}