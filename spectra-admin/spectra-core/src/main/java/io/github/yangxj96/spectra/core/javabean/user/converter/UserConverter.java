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

import io.github.yangxj96.spectra.core.javabean.user.entity.User;
import io.github.yangxj96.spectra.core.javabean.user.from.UserSaveFrom;
import io.github.yangxj96.spectra.core.javabean.user.vo.UserPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

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
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConverter {

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
    List<UserPageVO> toVOs(List<User> users);

    /**
     * 入参vo转实体
     *
     * @param vo 入参vo
     * @return 实体
     */
    User toEntity(UserSaveFrom vo);

    /**
     * 使用params更新现有的user实体
     *
     * @param params 更新的参数
     * @param user   现有的实体
     */
    void updateUserFrom(UserSaveFrom params, @MappingTarget User user);
}
