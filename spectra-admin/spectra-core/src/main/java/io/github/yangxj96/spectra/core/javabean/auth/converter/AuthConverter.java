package io.github.yangxj96.spectra.core.javabean.auth.converter;

import io.github.yangxj96.spectra.core.configure.security.javabean.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 用户认证和登录相关的转换器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 21:15
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthConverter {

    /**
     * 用户信息转换为UserDTO,用于认证
     *
     * @param datum 用户信息
     * @return 转换结果
     */
    SecurityUser toUserDTO(User datum);

}
