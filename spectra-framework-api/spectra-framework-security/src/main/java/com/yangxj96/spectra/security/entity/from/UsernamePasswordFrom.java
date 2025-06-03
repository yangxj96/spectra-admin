package com.yangxj96.spectra.security.entity.from;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户名密码登录入参
 *
 * @author 杨新杰
 * @since 2025/5/26 18:58
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UsernamePasswordFrom {

    @NotEmpty(message = "用户名不能为空")
    private String username;

    @NotEmpty(message = "密码不能为空")
    private String password;

    @NotEmpty(message = "验证码不能为空")
    private String code;

}
