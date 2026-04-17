package com.devops00.spectra.core.mapper.user;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.javabean.user.entity.UserDataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

/// 用户数据范围Mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/23 11:36
@Mapper
public interface UserDataScopeMapper extends BaseMapper<UserDataScope> {

    /// 根据用户ID查询数据范围
    ///
    /// @param userId 用户ID
    /// @return 数据范围
    UserDataScope findByUserId(@Param("userId") UUID userId);
}
