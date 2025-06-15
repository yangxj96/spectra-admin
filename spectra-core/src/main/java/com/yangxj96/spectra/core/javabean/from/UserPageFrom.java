package com.yangxj96.spectra.core.javabean.from;

import lombok.*;

/**
 * 用户分页查询入参
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
public class UserPageFrom {

    /**
     * 用户名称
     */
    private String name;

}
