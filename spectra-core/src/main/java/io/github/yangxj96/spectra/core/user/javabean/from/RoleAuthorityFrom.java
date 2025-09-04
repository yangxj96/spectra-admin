package io.github.yangxj96.spectra.core.user.javabean.from;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色关联权限入参
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAuthorityFrom {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 权限ID列表
     */
    @NotNull(message = "权限列表不能为空")
    @Size(min = 1, message = "权限列表至少需要一个权限ID")
    private List<Long> authorityIds;

}
