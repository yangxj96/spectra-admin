package com.devops00.spectra.core.user.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.constant.DataScopeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/// 角色数据范围
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/23 11:24
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role_data_scope")
public class RoleDataScope extends BaseEntity implements Serializable {

    /// 角色ID
    @TableField(value = "role_id")
    private UUID roleId;

    /// 数据范围类型
    @TableField(value = "scope_type")
    private DataScopeType scopeType;

}
