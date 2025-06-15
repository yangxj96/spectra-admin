package com.yangxj96.spectra.core.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yangxj96.spectra.common.base.BaseEntity;
import com.yangxj96.spectra.common.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 账号信息
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "db_system.t_account")
public class Account extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码
     */
    @TableField(value = "\"password\"")
    private String password;

    /**
     * 密码
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 登录方式
     */
    @TableField(value = "\"type\"")
    private AccountType type;
}
