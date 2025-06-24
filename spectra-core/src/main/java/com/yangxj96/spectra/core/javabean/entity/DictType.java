package com.yangxj96.spectra.core.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yangxj96.spectra.common.base.BaseEntity;
import com.yangxj96.spectra.common.enums.CommonState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典-字典类型
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "db_system.t_dict_type")
public class DictType extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 父级ID
     */
    @TableField(value = "pid")
    private Long pid;

    /**
     * 字典名称
     */
    @TableField(value = "\"name\"")
    private String name;

    /**
     * 字典编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 字典状态
     */
    @TableField(value = "\"state\"")
    private CommonState state;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 是否内置字段,为true则不允许他进行修改删除操作
     */
    @TableField(value = "builtin")
    private Boolean builtin;

    /**
     * 是否隐藏,为true则前端不可直接进行修改删除等操作
     */
    @TableField(value = "hide")
    private Boolean hide;
}
