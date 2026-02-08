/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.javabean.user.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yangxj96.spectra.core.configure.mapstruct.GlobalMapperConfig;
import io.github.yangxj96.spectra.core.configure.mapstruct.TimeMapper;
import io.github.yangxj96.spectra.core.javabean.user.entity.User;
import io.github.yangxj96.spectra.core.javabean.user.from.UserSaveFrom;
import io.github.yangxj96.spectra.core.javabean.user.vo.UserPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/// 用户mapstruct
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/15
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface UserConverter {

    /// 实体转分页VO
    ///
    /// @param source 实体
    /// @return 分页实体
    UserPageVO toVO(User source);

    /// 入参vo转实体
    ///
    /// @param source 入参vo
    /// @return 实体
    User toEntity(UserSaveFrom source);

    /**
     * 转换为分页VO
     *
     * @param source 入参
     * @return 分页的VO
     */
    Page<UserPageVO> toVOPage(Page<User> source);

    /// 使用params更新现有的user实体
    ///
    /// @param source 更新的参数
    /// @param target 现有的实体
    void updateUser(UserSaveFrom source, @MappingTarget User target);
}
