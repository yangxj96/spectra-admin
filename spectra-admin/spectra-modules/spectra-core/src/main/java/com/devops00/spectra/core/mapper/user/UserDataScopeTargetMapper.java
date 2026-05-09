package com.devops00.spectra.core.mapper.user;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.javabean.user.entity.UserDataScopeTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/// 用户数据范围目标表Mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/23 11:36
@Mapper
public interface UserDataScopeTargetMapper extends BaseMapper<UserDataScopeTarget> {

    /// 根据用户ID获取数据范围目标信息
    ///
    /// @param userId 用户ID
    /// @return 数据范围目标列表
    List<UserDataScopeTarget> findByUserId(@Param("userId") String userId);

    /// 根据用户ID删除用户的数据范围内容
    ///
    /// @param userId 用户ID
    void removeByUserId(@Param("userId") UUID userId);
}
