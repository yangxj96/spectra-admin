package io.github.yangxj96.spectra.core.system.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;


/**
 * 系统配置表
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/9/18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "SYS_CONFIG")
public class SysConfig extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置key
     */
    @TableField(value = "\"KEY\"")
    private String key;

    /**
     * 配置VALUE
     */
    @TableField(value = "\"VALUE\"")
    private String value;

    /**
     * 备注说明
     */
    @TableField(value = "REMARKS")
    private String remarks;

}