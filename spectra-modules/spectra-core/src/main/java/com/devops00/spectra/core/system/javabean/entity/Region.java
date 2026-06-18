package com.devops00.spectra.core.system.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.constant.RegionLevel;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/// GB/T 2260的行政区域数据表
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/30 11:45
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_region")
public class Region extends BaseEntity implements Serializable {

    /// 区域名称
    @TableField(value = "name")
    private String name;

    /// 区域全称，如 北京市/北京市/东城区
    @TableField(value = "full_name")
    private String fullName;

    /// 简称
    @TableField(value = "short_name")
    private String shortName;

    /// 区域编码
    @TableField(value = "code")
    private String code;

    /// 区域路径，如 /110000/110100/110101
    @TableField(value = "path")
    private String path;

    /// 上级ID
    @TableField(value = "pid")
    private UUID pid;

    /// 行政区划层级:1省 2地级市 3县级 4乡级 5村级
    @TableField(value = "level")
    private RegionLevel level;

    /// 状态：true-启用 false-停用
    @TableField(value = "status")
    private Boolean status;

    /// 排序
    @TableField(value = "sort")
    private Integer sort;

}
