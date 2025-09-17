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

package io.github.yangxj96.spectra.core.user.javabean.mapstruct;

import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 权限mapstruct
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Mapper(componentModel = "spring")
public interface AuthorityMapstruct {

    /**
     * 实体转VO
     *
     * @param entity 实体对象
     * @return VO对象
     */
    AuthorityVO toVO(Authority entity);

    /**
     * 实体列表转VO列表
     *
     * @param coll 实体列表
     * @return VO列表
     */
    List<AuthorityVO> toVOS(List<Authority> coll);

}
