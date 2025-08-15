package com.yangxj96.spectra.core.system.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yangxj96.spectra.common.base.BaseEntity;
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
@TableName(value = "T_SYS_DICT_DATA")
public class DictData extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID
     */
    @TableField(value = "GID")
    private Long gid;

    /**
     * 标签
     */
    @TableField(value = "LABEL")
    private String label;

    /**
     * 值
     */
    @TableField(value = "VALUE")
    private String value;

    /**
     * 排序
     */
    @TableField(value = "SORT")
    private Short sort;

    /**
     * 状态
     */
    @TableField(value = "STATE")
    private Short state;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;
}
