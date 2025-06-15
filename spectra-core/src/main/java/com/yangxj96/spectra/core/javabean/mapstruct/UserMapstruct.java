package com.yangxj96.spectra.core.javabean.mapstruct;

import com.yangxj96.spectra.core.javabean.entity.User;
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

    UserPageVO toVO(User user);

    @Mapping(target = "roles", ignore = true)
    List<UserPageVO> toVOs(List<User> users);
}
