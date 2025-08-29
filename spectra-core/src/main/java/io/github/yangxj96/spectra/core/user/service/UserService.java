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

package io.github.yangxj96.spectra.core.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.yangxj96.spectra.common.base.BaseService;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.core.user.javabean.entity.User;
import io.github.yangxj96.spectra.core.user.javabean.from.UserPageFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.UserSaveFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.UserPageVO;
import jakarta.validation.constraints.NotEmpty;

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

    /**
     * 重置用户密码
     *
     * @param uid 用户ID
     */
    void passwordResetById(String uid);

    /**
     * 根据用户邮箱查询用户信息
     *
     * @param email 用于邮箱
     * @return 用户信息,可能为null
     */
    User getByEmail(@NotEmpty(message = "用户名不能为空") String email);
}
