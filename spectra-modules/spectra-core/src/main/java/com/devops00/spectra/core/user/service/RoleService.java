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
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
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
     * @param params 实体入参
     * @return 保存后的角色
     */
    RoleVO save(RoleFrom params);

    /**
     * 启用角色。
     *
     * @param id 角色ID
     */
    void enable(UUID id);

    /**
     * 禁用角色。
     *
     * @param id 角色ID
     */
    void disable(UUID id);

    /**
     * 逻辑删除角色。
     *
     * @param id 角色ID
     */
    void deleteById(UUID id);

    /**
     * 分页查询角色信息
     *
     * @param page   分页信息
     * @param params 查询参数
     */
    IPage<RoleVO> page(PageFrom page, RolePageFrom params);

    /**
     * 查询所有角色列表
     *
     * @return 角色列表
     */
    List<RoleVO> all();

    /**
     * 查询角色详情。
     *
     * @param id 角色 ID
     * @return 角色详情
     */
    RoleVO detail(UUID id);

    /**
     * 获取系统默认角色.
     *
     * @return 角色信息
     */
    SecurityRole getSystemDefaultUserRole();
}
