/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.auth.javabean.mapstruct;

import com.yangxj96.spectra.auth.javabean.entity.User;
import com.yangxj96.spectra.auth.javabean.from.UserSaveFrom;
import com.yangxj96.spectra.auth.javabean.vo.UserPageVO;
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
    @Mapping(target = "roles", ignore = true)
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
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    User toEntity(UserSaveFrom vo);
}
