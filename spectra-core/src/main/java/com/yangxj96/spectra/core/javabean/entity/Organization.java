package com.yangxj96.spectra.core.javabean.entity;

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
 * 组织机构业务层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-15
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "db_system.t_organization")
public class Organization extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 上级ID
     */
    @TableField(value = "pid")
    private Long pid;

    /**
     * 名称
     */
    @TableField(value = "\"name\"")
    private String name;

    /**
     * 编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 类型
     */
    @TableField(value = "\"type\"")
    private Short type;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}

