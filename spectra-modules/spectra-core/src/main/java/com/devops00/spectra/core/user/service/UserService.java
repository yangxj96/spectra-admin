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
import com.devops00.spectra.framework.persistence.base.BaseService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.from.ChangePasswordFrom;
import com.devops00.spectra.core.user.javabean.from.UserPageFrom;
import com.devops00.spectra.core.user.javabean.from.UserProfileFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.from.OnlineUserPageFrom;
import com.devops00.spectra.core.user.javabean.vo.UserPageVO;
import com.devops00.spectra.core.user.javabean.vo.OnlineUserPageVO;
import com.devops00.spectra.core.user.javabean.vo.UserProfileVO;
import com.devops00.spectra.core.user.javabean.vo.UserCreatedVO;
import com.devops00.spectra.core.user.javabean.vo.UserPasswordResetVO;

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
     * 按登录用户名查找用户资料，供通知和管理域使用；认证登录使用 identity hash，不直接信任该查询。
     *
     * @param username 登录用户名，用于查询用户身份资料。
     * @return 返回与登录用户名匹配的用户实体；用户名为空、用户不存在或用户未启用时返回 null。
     */
    User getByUsername(String username);

    /**
     * 查询安全运维目标候选用户；仅返回未软删除用户，调用方负责实际权限控制。
     *
     * @param keyword 用户编号、用户名、姓名或工号关键字。
     * @param limit   返回数量上限，由实现进一步限制为安全运维固定上限。
     * @return 返回候选用户实体列表；无匹配时返回空列表。
     */
    List<User> searchSecurityCandidates(String keyword, int limit);

    /**
     * 分页查询用户列表
     *
     * @param page   用户列表的页码、页大小及排序字段。
     * @param params 用户名、状态、部门等用户列表筛选条件。
     * @return 返回按筛选条件和分页排序查询的用户分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     * @throws IllegalAccessException 无法访问需要导出的字段时抛出。
     */
    IPage<UserPageVO> page(PageFrom page, UserPageFrom params) throws IllegalAccessException;

    /**
     * 获取管理员用户详情
     *
     * @param userId 要查看的用户唯一标识；权限过滤仍按当前操作者执行。
     * @return 返回符合条件的用户分页视图详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     * @throws IllegalAccessException 无法访问需要导出的字段时抛出。
     */
    UserPageVO detail(UUID userId) throws IllegalAccessException;

    /**
     * 创建用户
     *
     * @param params 待创建用户的账号、资料、部门和初始状态等字段。
     * @return 返回新建用户的标识、用户名和初始状态；校验或写入失败时抛出业务异常，不返回 null。
     */
    UserCreatedVO create(UserSaveFrom params);

    /**
     * 根据用户ID更新用户
     *
     * @param params 待修改用户的唯一标识及账号、资料、部门和状态等字段。
     */
    void modify(UserSaveFrom params);

    /**
     * 重置用户密码
     *
     * @param uid 需要重置密码的用户唯一标识。
     * @return 返回密码重置结果；处理失败时抛出业务异常，不返回 null。
     */
    UserPasswordResetVO passwordResetById(UUID uid);

    /**
     * 按用户分组分页获取在线用户和会话摘要。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param filter 按登录账号、姓名和部门筛选的条件。
     * @return 返回用户数作为 total 的在线用户分页；没有匹配记录时 records 为空、total 为 0。
     * @throws IllegalAccessException 无法访问需要填充的部门名称字段时抛出。
     */
    IPage<OnlineUserPageVO> online(PageFrom page, OnlineUserPageFrom filter) throws IllegalAccessException;

    /**
     * 获取当前用户详情
     *
     * @param userId 要读取资料的用户唯一标识；返回内容受当前操作者权限限制。
     * @return 返回符合条件的用户个人资料视图详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    UserProfileVO getProfile(UUID userId);

    /**
     * 更新当前用户信息
     *
     * @param userId 要更新资料的用户唯一标识。
     * @param params 待更新的用户资料字段及用户唯一标识。
     */
    void updateProfile(UUID userId, UserProfileFrom params);

    /**
     * 修改当前用户密码
     *
     * @param userId 要修改密码的用户唯一标识。
     * @param params 旧密码、新密码及确认值，用于完成密码变更校验。
     */
    void changePassword(UUID userId, ChangePasswordFrom params);

    /**
     * 执行用户生命周期状态变化。状态变更必须经过安全审计事务，并撤销全部 Session。
     *
     * @param userId 目标用户
     * @param target 目标状态
     * @param reason 本次启用、禁用或其他状态迁移的审计原因，将随状态变更记录保存。
     */
    void changeStatus(UUID userId, UserStatus target, String reason);
}
