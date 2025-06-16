package com.yangxj96.spectra.core.javabean.from;

import com.yangxj96.spectra.common.base.Verify;
import com.yangxj96.spectra.common.enums.UserState;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * <p>
 * 用户新增/编辑操作入参
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/16
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveFrom {

    /**
     * 姓名
     */
    @Null(message = "用户ID不能为空", groups = Verify.Insert.class)
    @NotNull(message = "用户ID不能为空", groups = Verify.Update.class)
    private Long id;

    /**
     * 姓名
     */
    @NotBlank(message = "用户名不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String name;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确", groups = {Verify.Insert.class, Verify.Update.class})
    @NotEmpty(message = "邮箱不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String email;

    /**
     * 用户状态
     */
    @NotNull(message = "用户状态不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private UserState state;

    /**
     * 角色ID列表
     */
    @Size(message = "角色ID列表不能为空,最少需要有一个角色", min = 1, groups = {Verify.Insert.class, Verify.Update.class})
    private List<Long> roleIds;
}
