package com.yangxj96.spectra.core.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yangxj96.spectra.common.base.BaseEntity;
import com.yangxj96.spectra.common.enums.UserState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户信息
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
@TableName(value = "db_system.t_user")
public class User extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 姓名
     */
    @TableField(value = "\"name\"")
    private String name;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 用户状态
     */
    @TableField(value = "state")
    private UserState state;
}
