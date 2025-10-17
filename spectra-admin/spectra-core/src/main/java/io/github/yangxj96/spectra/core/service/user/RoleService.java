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

package io.github.yangxj96.spectra.core.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.yangxj96.spectra.common.base.BaseService;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.core.javabean.user.entity.Role;
import io.github.yangxj96.spectra.core.javabean.user.from.RoleFrom;
import io.github.yangxj96.spectra.core.javabean.user.from.RolePageFrom;
import io.github.yangxj96.spectra.core.javabean.user.vo.RoleVO;

import java.util.List;

/**
 * 角色service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface RoleService extends BaseService<Role> {

    /**
     * 创建角色
     *
     * @param params 实体入参
     */
    void created(RoleFrom params);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void delete(Long id);

    /**
     * 修改角色
     *
     * @param params 实体入参
     */
    void modify(RoleFrom params);

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
     * 获取系统默认角色.
     *
     * @return 角色信息
     */
    Role getSystemDefaultUserRole();

}
