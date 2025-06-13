package com.yangxj96.spectra.core.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yangxj96.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 菜单表
 *
 * @author Jack Young
 * @since 2025/6/3 23:32
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "db_system.t_menu")
public class Menu extends BaseEntity {

    /**
     * 父级ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(value = "pid")
    private Long pid;

    /**
     * 图标
     */
    @TableField(value = "icon")
    private String icon;

    /**
     * 名称
     */
    @TableField(value = "\"name\"")
    private String name;

    /**
     * 请求路径
     */
    @TableField(value = "\"path\"")
    private String path;

    /**
     * 组件路径,为空则使用布局组件
     */
    @TableField(value = "component")
    private String component;

    /**
     * 布局
     */
    @TableField(value = "layout")
    private String layout;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;
}
