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

package com.devops00.spectra.core.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.core.security.authorization.javabean.entity.SecurityRole;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.javabean.from.RolePageFrom;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;

import java.util.List;
import java.util.UUID;

/**
 * 角色service层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
public interface RoleService {

    /**
     * 保存角色基本信息，ID 为空时新增，否则修改。
     *
     * @param params 角色名称、编码、状态及角色 ID；ID 为空时创建新角色，否则更新对应角色。
     * @return 返回保存后的角色视图，包含角色标识、名称和状态；校验或写入失败时抛出业务异常，不返回 null。
     */
    RoleVO save(RoleFrom params);

    /**
     * 启用角色。
     *
     * @param id 待启用角色的唯一标识；角色不存在或无权操作时抛出业务异常。
     */
    void enable(UUID id);

    /**
     * 禁用角色。
     *
     * @param id 待禁用角色的唯一标识；角色不存在或无权操作时抛出业务异常。
     */
    void disable(UUID id);

    /**
     * 逻辑删除角色。
     *
     * @param id 待逻辑删除角色的唯一标识；角色不存在或仍被引用时抛出业务异常。
     */
    void deleteById(UUID id);

    /**
     * 分页查询角色信息
     *
     * @param page   分页页码、页大小及允许的排序字段。
     * @param params 角色名称、编码和启用状态等筛选条件。
     * @return 返回按分页条件查询的角色视图分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<RoleVO> page(PageFrom page, RolePageFrom params);

    /**
     * 查询所有角色列表
     *
     * @return 返回当前用户可见的全部角色；没有可见角色时返回空列表，不返回 null。
     */
    List<RoleVO> all();

    /**
     * 查询角色详情。
     *
     * @param id 角色 ID
     * @return 返回指定角色的完整视图；角色不存在或当前用户无权查看时抛出业务异常，不返回 null。
     */
    RoleVO detail(UUID id);

    /**
     * 获取系统默认角色.
     *
     * @return 返回系统默认用户角色实体；默认角色未配置或数据缺失时抛出业务异常，不返回 null。
     */
    SecurityRole getSystemDefaultUserRole();
}
