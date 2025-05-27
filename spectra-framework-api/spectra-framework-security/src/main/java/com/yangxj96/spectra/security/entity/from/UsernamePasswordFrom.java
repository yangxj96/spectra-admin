package com.yangxj96.spectra.security.entity.from;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "账号密码登录的入参实体")
public class UsernamePasswordFrom {

    @NotEmpty(message = "用户名不能为空")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "sysadmin")
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "sysadmin")
    private String password;

    @NotEmpty(message = "验证码不能为空")
    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "56EF")
    private String code;

}
