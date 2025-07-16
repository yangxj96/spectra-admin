package com.yangxj96.spectra.share.javabean;

import com.yangxj96.spectra.common.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 共享账号信息
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareAccount {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 密码
     */
    private Long userId;

    /**
     * 登录方式
     */
    private AccountType type;

}
