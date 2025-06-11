package com.yangxj96.spectra.security.entity.from;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 角色分页查询
 *
 * @author 杨新杰
 * @since 2025/6/11 14:15
 */
@Data
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RolePageFrom {

    /**
     * 角色名称
     **/
    private String name;

    /**
     * 角色状态
     */
    private Boolean state;
}
