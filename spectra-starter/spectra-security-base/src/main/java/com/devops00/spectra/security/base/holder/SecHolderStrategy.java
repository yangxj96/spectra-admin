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

package com.devops00.spectra.security.base.holder;

import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Token存储相关
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/11 10:06
 */
public interface SecHolderStrategy {

    /**
     * 获取定义的超管角色
     */
    String administrators();

    /**
     * 创建 token（默认 WEB 端）
     */
    TokenVO createToken(SecurityUser user);

    /**
     * 创建 token，同端复用：同一用户同一端已有有效 token 则直接返回
     */
    TokenVO createToken(SecurityUser user, ClientType clientType);

    /**
     * 兼容旧会话实现的 TTL 操作；请求过滤器不得调用该方法进行滑动续期。
     */
    @Deprecated(forRemoval = true)
    void refreshToken(String token);

    /**
     * 根据刷新token签发新的token对
     */
    TokenVO refreshByRefreshToken(String refreshToken);

    /**
     * 删除单个 token
     */
    void deleteToken(String token);

    /**
     * 根据刷新token删除
     */
    void deleteByRefreshToken(String refreshToken);

    /**
     * 踢出用户所有端
     */
    void deleteByUserId(UUID userId);

    /**
     * 踢出用户指定端
     */
    void deleteByUserIdAndClient(String userId, ClientType clientType);

    /**
     * 获取在线用户列表
     */
    List<UserOnlineVO> listOnlineUsers();

    /**
     * 获取当前用户信息
     */
    @Nullable
    SecurityUser getCurrentUser();

    /**
     * 根据 token 获取用户信息
     */
    @Nullable
    SecurityUser getCurrentUser(String token);

    /**
     * 获取当前 token
     */
    @Nullable
    String getCurrentToken();

    /**
     * 获取当前用户 ID
     */
    @Nullable
    UUID getCurrentUserId();

    /**
     * 获取当前用户时区ID
     */
    String getCurrentUserZoneId();

    /**
     * 获取当前用户名
     */
    String getCurrentUsername();

    /**
     * 记录登录失败
     */
    void recordLoginFail(String username);

    /**
     * 检查是否被锁定
     */
    boolean isLockedOut(String username);

    /**
     * 清除登录失败计数
     */
    void clearLoginFail(String username);
}
