/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.common.base.BaseService;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.core.javabean.entity.User;
import com.yangxj96.spectra.core.javabean.from.UserPageFrom;
import com.yangxj96.spectra.core.javabean.from.UserRelevanceRolesFrom;
import com.yangxj96.spectra.core.javabean.from.UserSaveFrom;
import com.yangxj96.spectra.core.javabean.vo.UserPageVO;

/**
 * 用户service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface UserService extends BaseService<User> {

    /**
     * 分页查询用户列表
     *
     * @param page   分页参数
     * @param params 查询条件参数
     * @return 分页结果
     */
    IPage<UserPageVO> page(PageFrom page, UserPageFrom params);

    /**
     * 用户关联角色列表
     *
     * @param params 请求参数
     */
    void relevanceRoles(UserRelevanceRolesFrom params);

    /**
     * 创建用户
     *
     * @param params 请求参数
     */
    void create(UserSaveFrom params);

    /**
     * 根据用户ID更新用户
     *
     * @param params 请求参数
     */
    void updateById(UserSaveFrom params);

    /**
     * 根据用户ID删除用户信息
     *
     * @param uid 用户ID
     */
    void deleteById(String uid);
}
