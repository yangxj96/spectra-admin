package com.yangxj96.spectra.core.javabean.from;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * <p>
 * 用户关联角色
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/15
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRelevanceRolesFrom {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "需要关联的角色ID列表不能为空")
    private List<Long> roleIds;
}
