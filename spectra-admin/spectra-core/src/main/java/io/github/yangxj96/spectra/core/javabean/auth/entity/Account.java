package io.github.yangxj96.spectra.core.javabean.auth.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.core.configure.security.javabean.LoginType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

/**
 * 账号表
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/11 15:43
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_account", schema = "domain_core")
public class Account extends BaseEntity implements Serializable {

    /**
     * 用户 ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 登录类型
     */
    @TableField(value = "type")
    private LoginType type;

    /**
     * 用户名（用于账号密码登录）
     */
    @TableField(value = "login_name")
    private String loginName;

    /**
     * 密码(仅用作账号密码登录)
     */
    @TableField(value = "password")
    private String password;

    /**
     * 手机号（用于短信登录）
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 邮箱（用于邮箱验证码登录）
     */
    @TableField(value = "email")
    private String email;

    /**
     * 微信 openid
     */
    @TableField(value = "openid")
    private String openId;

    /**
     * 微信 unionid（跨应用唯一）
     */
    @TableField(value = "unionid")
    private String unionid;

    /**
     * 第三方来源：WECHAT, ALIPAY, APPLE 等
     */
    @TableField(value = "provider")
    private String provider;

    /**
     * 1:正常 2:禁用 3:未验证
     */
    @TableField(value = "status")
    private Boolean status;

    /**
     * 0:未验证 1:已验证（如手机号/邮箱）
     */
    @TableField(value = "verified")
    private Boolean verified;

    /**
     * 用于临时账号（如扫码未确认）
     */
    @TableField(value = "expires_at")
    private Instant expiresAt;

}
