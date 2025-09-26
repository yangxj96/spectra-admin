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

package io.github.yangxj96.spectra.core.user.javabean.converter;

import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.javabean.vo.RoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 角色转换用的
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleConverter {

    /**
     * 实体转分页VO
     *
     * @param entity 实体
     * @return 分页实体
     */
    RoleVO toVO(Role entity);

    /**
     * 实体列表转分页VO列表
     *
     * @param coll 实体列表
     * @return 分页vo列表
     */
    List<RoleVO> toVOs(List<Role> coll);

}
