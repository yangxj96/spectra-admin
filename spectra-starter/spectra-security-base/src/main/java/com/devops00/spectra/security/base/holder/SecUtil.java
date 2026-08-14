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

import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Security 静态工具类
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/10 09:19
 */
@NullMarked
public class SecUtil {

    /**
     * 具体业务持有者
     */
    @Nullable
    private static volatile SecHolderStrategy strategy;

    private SecUtil() {
    }

    /**
     * 内部调用获取 Holder
     */
    private static SecHolderStrategy getStrategy() {
        SecHolderStrategy s = strategy;
        if (s == null) {
            throw new IllegalStateException("SecUtil尚未初始化，请确保已加载对应的Security策略");
        }
        return s;
    }

    public static void setStrategy(SecHolderStrategy s) {
        if (strategy != null) {
            throw new IllegalStateException("SecHolderStrategy already initialized");
        }
        strategy = s;
    }

    public static void setHolder(SecHolderStrategy holder) {
        SecUtil.strategy = holder;
    }

    /**
     * 登录（默认 WEB 端）
     */
    public static TokenVO login(SecurityUser su) {
        return getStrategy().createToken(su);
    }

    /**
     * 登出指定 token
     */
    public static void logout(String token) {
        getStrategy().deleteToken(token);
    }

    /**
     * 登出当前用户
     */
    public static void logout() {
        var token = getStrategy().getCurrentToken();
        if (token == null) {
            throw new SpectraException("无Token/Token无效");
        }
        logout(token);
    }

    /**
     * 根据刷新token登出
     */
    public static void logoutByRefreshToken(String refreshToken) {
        getStrategy().deleteByRefreshToken(refreshToken);
    }

    /**
     * 踢出用户所有端
     */
    public static void kick(UUID id) {
        getStrategy().deleteByUserId(id);
    }

    /**
     * 根据刷新token签发新的token对
     */
    public static TokenVO refreshByRefreshToken(String refreshToken) {
        return getStrategy().refreshByRefreshToken(refreshToken);
    }

    /**
     * 记录登录失败
     */
    public static void recordLoginFail(String username) {
        getStrategy().recordLoginFail(username);
    }

    /**
     * 检查是否被锁定
     */
    public static boolean isLockedOut(String username) {
        return getStrategy().isLockedOut(username);
    }

    /**
     * 清除登录失败计数
     */
    public static void clearLoginFail(String username) {
        getStrategy().clearLoginFail(username);
    }

    /**
     * 获取在线用户列表
     */
    public static List<UserOnlineVO> online() {
        return getStrategy().listOnlineUsers();
    }

    /**
     * 根据 token 获取用户信息
     */
    public static @Nullable SecurityUser getCurrentUser(String token) {
        return getStrategy().getCurrentUser(token);
    }

    /**
     * 获取当前请求的用户信息
     */
    public static @Nullable SecurityUser getCurrentUser() {
        return getStrategy().getCurrentUser();
    }

    /**
     * 获取当前用户的 token
     */
    public static @Nullable String getCurrentToken() {
        return getStrategy().getCurrentToken();
    }

    /**
     * 获取当前用户 ID
     */
    public static @Nullable UUID getCurrentUserId() {
        return getStrategy().getCurrentUserId();
    }

    /**
     * 获取当前用户时区ID
     */
    public static String getCurrentUserZoneId() {
        return getStrategy().getCurrentUserZoneId();
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        return getStrategy().getCurrentUsername();
    }
}
