package com.yangxj96.spectra.security.entity.vo;

import com.yangxj96.spectra.security.entity.dto.Role;
import com.yangxj96.spectra.security.entity.dto.User;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 用户分页的VO
 *
 * @author 杨新杰
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
