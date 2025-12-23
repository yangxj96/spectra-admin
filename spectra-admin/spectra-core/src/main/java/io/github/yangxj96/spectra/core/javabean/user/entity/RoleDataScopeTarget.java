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
@TableName(value = "SYS_ROLE_DATA_SCOPE_TARGET")
public class RoleDataScopeTarget extends BaseEntity implements Serializable {

    /**
     * 角色ID
     */
    @TableField(value = "ROLE_ID")
    private Long roleId;

    /**
     * 目标ID
     */
    @TableField(value = "TARGET_ID")
    private Long targetId;

    /**
     * 目标类型
     */
    @TableField(value = "TARGET_TYPE")
    private String targetType;
}
