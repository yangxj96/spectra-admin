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

package io.github.yangxj96.spectra.core.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.yangxj96.spectra.core.javabean.system.entity.Organization;
import io.github.yangxj96.spectra.core.javabean.system.from.OrganizationFrom;
import io.github.yangxj96.spectra.core.javabean.system.vo.OrganizationTreeVo;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 组织机构业务层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-15
 */
public interface OrganizationService extends IService<Organization> {

    /**
     * 新增组织机构
     *
     * @param from 请求入参
     */
    void created(OrganizationFrom from);

    /**
     * 根据ID删除组织机构
     *
     * @param id ID
     */
    void deleteById(String id);

    /**
     * 编辑组织机构
     *
     * @param from 请求入参
     */
    void modify(OrganizationFrom from);

    /**
     * 计算组织机构路径
     *
     * @param id 组织机构ID
     * @return 组织机构路径
     */
    String generatePath(Long id);

    /**
     * 根据ID获取他的所有子级,包含孙级..曾孙级...等 <br/>
     * 使用递归实现主要是为了后期如果适配其他数据库少点修改
     *
     * @param organizationId 组织机构ID
     * @return 所有子级列表
     */
    List<Organization> getAllChildrenById(Long organizationId);

    /**
     * 组织机构树形结构
     *
     * @return 组织机构树形结构数组
     */
    @Nullable List<OrganizationTreeVo> tree();
}
