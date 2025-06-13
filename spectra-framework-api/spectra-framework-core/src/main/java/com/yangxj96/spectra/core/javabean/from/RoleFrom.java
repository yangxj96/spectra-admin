package com.yangxj96.spectra.core.javabean.from;

import com.yangxj96.spectra.common.base.Verify;
import com.yangxj96.spectra.common.enums.PowerScope;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;

/**
 * 角色操作入参
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
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

    /**
     * 范围
     */
    private PowerScope scope;

    /**
     * 状态
     */
    private Boolean state;

    /**
     * 备注
     */
    private String remark;

}
