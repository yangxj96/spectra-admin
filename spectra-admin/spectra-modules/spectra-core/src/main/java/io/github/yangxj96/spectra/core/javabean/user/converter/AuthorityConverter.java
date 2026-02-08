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

import io.github.yangxj96.spectra.core.configure.mapstruct.GlobalMapperConfig;
import io.github.yangxj96.spectra.core.configure.mapstruct.TimeMapper;
import io.github.yangxj96.spectra.core.javabean.user.entity.Authority;
import io.github.yangxj96.spectra.core.javabean.user.vo.AuthorityTreeVO;
import io.github.yangxj96.spectra.core.javabean.user.vo.AuthorityVO;
import org.mapstruct.Mapper;

import java.util.List;

/// 权限mapstruct
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/7/16
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface AuthorityConverter {

    /**
     * 实体转VO
     *
     * @param source 实体对象
     * @return VO对象
     */
    AuthorityVO toVO(Authority source);

    /**
     * 实体转VO(列表)
     *
     * @param source 实体对象
     * @return VO对象
     */
    List<AuthorityVO> toVOList(List<Authority> source);

    /**
     * 转成树形需要的vo
     *
     * @param source 权限列表
     * @return 树形vo
     */
    AuthorityTreeVO toTreeVO(Authority source);

    /**
     * 转成树形需要的vo
     *
     * @param source 权限列表
     * @return 树形vo
     */
    List<AuthorityTreeVO> toTreeVOList(List<Authority> source);

}
