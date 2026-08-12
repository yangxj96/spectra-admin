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

package com.devops00.spectra.core.user.javabean.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.core.user.javabean.entity.Role;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 角色转换用的
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/7/16 00:00
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface RoleConverter {

    /**
     * 实体转分页VO
     *
     * @param source 实体
     * @return 分页实体
     */
    RoleVO toVO(Role source);

    /**
     * 保存入参转实体。
     */
    Role toEntity(RoleFrom source);

    /**
     * 分页实体转分页视图。
     */
    @Mapping(target = "pages", ignore = true)
    Page<RoleVO> toVOPage(Page<Role> source);
}
