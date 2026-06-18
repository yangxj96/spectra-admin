package com.devops00.spectra.core.auth.javabean.converter;

import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.mapstruct.Mapper;

/// 用户认证和登录相关的转换器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/2 21:15
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface AuthConverter {

    /// 用户信息转换为UserDTO,用于认证
    ///
    /// @param source 用户信息
    /// @return 转换结果
    SecurityUser toSecurityUser(User source);

}
