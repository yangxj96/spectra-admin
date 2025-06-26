package com.yangxj96.spectra.service.core.javabean.entity;

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
 * 字典-字典数据
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
@TableName(value = "db_system.t_dict_data")
public class DictData extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID
     */
    @TableField(value = "dict_type_id")
    private Long dictTypeId;

    /**
     * 标签
     */
    @TableField(value = "\"label\"")
    private String label;

    /**
     * 值
     */
    @TableField(value = "\"value\"")
    private String value;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Short sort;

    /**
     * 状态
     */
    @TableField(value = "\"state\"")
    private CommonState state;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
