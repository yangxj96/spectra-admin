package com.yangxj96.spectra.core.javabean.mapstruct;

import com.yangxj96.spectra.core.javabean.entity.User;
import com.yangxj96.spectra.core.javabean.from.UserSaveFrom;
import com.yangxj96.spectra.core.javabean.vo.UserPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * <p>
 * 用户mapstruct
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/15
 */
@Mapper(componentModel = "spring")
public interface UserMapstruct {

    /**
     * 实体转分页VO
     *
     * @param user 实体
     * @return 分页实体
     */
    UserPageVO toVO(User user);

    /**
     * 实体列表转分页VO列表
     *
     * @param users 实体列表
     * @return 分页vo列表
     */
    @Mapping(target = "roles", ignore = true)
    List<UserPageVO> toVOs(List<User> users);

    /**
     * 入参vo转实体
     *
     * @param vo 入参vo
     * @return 实体
     */
    User toEntity(UserSaveFrom vo);
}
