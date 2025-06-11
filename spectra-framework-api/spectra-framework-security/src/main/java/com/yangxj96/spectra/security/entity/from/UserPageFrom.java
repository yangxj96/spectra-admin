package com.yangxj96.spectra.security.entity.from;

import lombok.*;

/**
 * 用户分页查询入参
 *
 * @author 杨新杰
 * @since 2025/6/11 14:53
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
