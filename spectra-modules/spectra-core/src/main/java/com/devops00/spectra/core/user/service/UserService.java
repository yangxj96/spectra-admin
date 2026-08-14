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
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.from.ChangePasswordFrom;
import com.devops00.spectra.core.user.javabean.from.UserPageFrom;
import com.devops00.spectra.core.user.javabean.from.UserProfileFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.vo.UserPageVO;
import com.devops00.spectra.core.user.javabean.vo.UserProfileVO;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;

import java.util.List;
import java.util.UUID;

/**
 * 用户service层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
public interface UserService extends BaseService<User> {

    /**
     * 按用户邮箱查找身份资料，供通知和管理域使用；认证登录使用 identity hash，不直接信任该查询。
     */
    User getByEmail(String email);

    /**
     * 分页查询用户列表
     *
     * @param page   分页参数
     * @param params 查询条件参数
     * @return 分页结果
     */
    IPage<UserPageVO> page(PageFrom page, UserPageFrom params) throws IllegalAccessException;

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
    void modify(UserSaveFrom params);

    /**
     * 重置用户密码
     *
     * @param uid 用户ID
     */
    void passwordResetById(UUID uid);

    /**
     * 分页获取在线用户
     *
     * @return 获取到的数据
     */
    List<UserOnlineVO> online(PageFrom page);

    /**
     * 获取当前用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    UserProfileVO getProfile(UUID userId);

    /**
     * 更新当前用户信息
     *
     * @param userId 用户ID
     * @param params 更新参数
     */
    void updateProfile(UUID userId, UserProfileFrom params);

    /**
     * 修改当前用户密码
     *
     * @param userId 用户ID
     * @param params 修改密码参数
     */
    void changePassword(UUID userId, ChangePasswordFrom params);

    /**
     * 执行用户生命周期状态变化。状态变更必须经过安全审计事务，并撤销全部 Session。
     *
     * @param userId 目标用户
     * @param target 目标状态
     * @param reason 操作原因
     */
    void changeStatus(UUID userId, UserStatus target, String reason);
}
