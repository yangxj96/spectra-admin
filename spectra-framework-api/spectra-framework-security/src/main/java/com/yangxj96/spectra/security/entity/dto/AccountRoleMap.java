package com.yangxj96.spectra.security.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yangxj96.spectra.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 角色表<->账户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "db_user.t_account_role_map")
public class AccountRoleMap extends BaseEntity {
    /**
     * 账号ID
     */
    @TableField(value = "account_id")
    private Long accountId;

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
    private Long roleId;
}