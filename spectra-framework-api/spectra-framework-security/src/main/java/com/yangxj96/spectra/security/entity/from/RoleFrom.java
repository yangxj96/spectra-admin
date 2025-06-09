package com.yangxj96.spectra.security.entity.from;

import com.yangxj96.spectra.core.base.Verify;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;

/**
 * 角色操作入参
 *
 * @author 杨新杰
 * @since 2025/6/9 23:47
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleFrom {

    /**
     * 角色ID
     */
    @Null(message = "新增不能指定角色ID", groups = Verify.Insert.class)
    @NotNull(message = "角色ID不能为空", groups = Verify.Update.class)
    private Long id;

    /**
     * 角色名称
     */
    @NotEmpty(message = "用户名不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String name;


}
