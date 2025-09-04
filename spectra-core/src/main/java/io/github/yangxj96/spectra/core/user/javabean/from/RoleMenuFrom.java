package io.github.yangxj96.spectra.core.user.javabean.from;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色关联菜单入参对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuFrom {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为null")
    private Long roleId;

    /**
     * 菜单ID列表
     */
    @NotNull(message = "菜单列表不能为null")
    @Size(min = 1, message = "菜单列表至少需要一个权限ID")
    private List<Long> menuIds;

}
