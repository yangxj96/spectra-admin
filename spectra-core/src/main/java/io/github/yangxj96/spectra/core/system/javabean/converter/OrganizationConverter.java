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

package io.github.yangxj96.spectra.core.system.javabean.converter;

import io.github.yangxj96.spectra.core.system.javabean.entity.Organization;
import io.github.yangxj96.spectra.core.system.javabean.from.OrganizationFrom;
import io.github.yangxj96.spectra.core.system.javabean.vo.OrganizationTreeVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 组织机构的数据转换使用
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/14
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationConverter {

    /**
     * 实体转树形
     *
     * @param organizations 实体列表
     * @return 树形列表
     */
    List<OrganizationTreeVo> toTreeVOS(List<Organization> organizations);

    /**
     * 实体转树形
     *
     * @param organization 实体
     * @return 树形
     */
    OrganizationTreeVo toTreeVO(Organization organization);

    /**
     * 入参转实体
     *
     * @param from 入参
     * @return 实体
     */
    Organization toEntity(OrganizationFrom from);

}

