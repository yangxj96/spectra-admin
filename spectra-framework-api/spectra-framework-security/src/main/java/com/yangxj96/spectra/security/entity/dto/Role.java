package com.yangxj96.spectra.security.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yangxj96.spectra.core.base.BaseEntity;
import com.yangxj96.spectra.core.enums.PowerScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 角色表
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "db_system.t_role")
public class Role extends BaseEntity {

    /**
     * 名称
     */
    @TableField(value = "\"name\"")
    private String name;

    /**
     * 状态
     */
    @TableField(value = "\"state\"")
    private Boolean state;

    /**
     * 范围
     */
    @TableField(value = "\"scope\"")
    private PowerScope scope;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
