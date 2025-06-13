package com.yangxj96.spectra.core.javabean.vo;

import com.yangxj96.spectra.core.javabean.entity.Role;
import com.yangxj96.spectra.core.javabean.entity.User;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;


/**
 * 用户分页的VO
 *
 * @author Jack Young
 * @since 2025/6/11 15:06
 */
@Data
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserPageVO extends User {

    /**
     * 账号状态
     */
    private Boolean state;

    /**
     * 角色列表
     */
    private List<Role> roles;

}
