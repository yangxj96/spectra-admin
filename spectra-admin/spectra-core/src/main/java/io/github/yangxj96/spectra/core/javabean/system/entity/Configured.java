package io.github.yangxj96.spectra.core.javabean.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.constant.ConfiguredValueType;
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
@TableName(value = "sys_config", schema = "domain_core")
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
     * 值类型
     */
    @TableField(value = "type")
    private ConfiguredValueType type;

    /**
     * 字典组CODE,可能会有选项之类的,直接关联一个字典做下拉选项
     */
    @TableField(value = "dict_code")
    private String dictCode;

    /**
     * 备注说明
     */
    @TableField(value = "remarks")
    private String remarks;

}