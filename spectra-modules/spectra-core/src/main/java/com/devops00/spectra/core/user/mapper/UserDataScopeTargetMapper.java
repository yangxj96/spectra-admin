/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.user.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.user.javabean.entity.UserDataScopeTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/// 用户数据范围目标表Mapper
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/23 11:36
@Mapper
public interface UserDataScopeTargetMapper extends BaseMapper<UserDataScopeTarget> {

    /// 根据用户ID获取数据范围目标信息
    ///
    /// @param userId 用户ID
    /// @return 数据范围目标列表
    List<UserDataScopeTarget> findByUserId(@Param("userId") UUID userId);

    /// 根据用户ID删除用户的数据范围内容
    ///
    /// @param userId 用户ID
    void removeByUserId(@Param("userId") UUID userId);
}
