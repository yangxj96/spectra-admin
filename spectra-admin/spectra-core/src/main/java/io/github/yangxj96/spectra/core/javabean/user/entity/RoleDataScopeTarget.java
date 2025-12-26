package io.github.yangxj96.spectra.core.javabean.user.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 角色数据范围(自定义数据范围的时候使用)
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/23 11:24
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role_data_scope_target", schema = "domain_core")
public class RoleDataScopeTarget extends BaseEntity implements Serializable {

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
    private Long roleId;

    /**
     * 目标ID
     */
    @TableField(value = "target_id")
    private Long targetId;

    /**
     * 目标类型
     */
    @TableField(value = "target_type")
    private String targetType;
}
