package com.yangxj96.spectra.core.javabean.from;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 角色分页查询
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
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
