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

package com.devops00.spectra.core.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.javabean.user.entity.User;
import com.devops00.spectra.core.javabean.user.from.UserPageFrom;
import com.devops00.spectra.core.javabean.user.from.UserSaveFrom;
import com.devops00.spectra.core.javabean.user.vo.UserPageVO;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;

import java.util.List;
import java.util.UUID;

/// 用户service层
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
public interface UserService extends BaseService<User> {

    /// 分页查询用户列表
    ///
    /// @param page   分页参数
    /// @param params 查询条件参数
    /// @return 分页结果
    IPage<UserPageVO> page(PageFrom page, UserPageFrom params) throws IllegalAccessException;

    /// 创建用户
    ///
    /// @param params 请求参数
    void create(UserSaveFrom params);

    /// 根据用户ID更新用户
    ///
    /// @param params 请求参数
    void updateById(UserSaveFrom params);

    /// 根据用户ID删除用户信息
    ///
    /// @param uid 用户ID
    void deleteById(UUID uid);

    /// 重置用户密码
    ///
    /// @param uid 用户ID
    void passwordResetById(UUID uid);

    /// 分页获取在线用户
    ///
    /// @return 获取到的数据
    List<UserOnlineVO> online(PageFrom page);

}
