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

package com.devops00.spectra.framework.security.configuration.session;

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.common.port.security.SecurityAuthenticationPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecuritySessionQueryPort;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.port.security.SecurityUserLookupPort;
import com.devops00.spectra.framework.security.session.lifecycle.SecurityLoginFailureTracker;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionIssuer;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionRefresher;
import com.devops00.spectra.framework.security.session.query.SecuritySessionQuery;
import com.devops00.spectra.framework.security.session.query.SecuritySessionReader;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionRevoker;
import com.devops00.spectra.framework.security.session.token.SecurityTokenAccessor;
import com.devops00.spectra.framework.security.session.query.SecuritySessionContextAccessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Security Session 适配端口配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/19 22:37
 */
@Configuration
public class SecuritySessionPortConfiguration {

    /**
     * 处理内部业务逻辑（{@code securityContextAccessor}）。
     *
     * @param sessionReader             从当前请求或安全 Session 读取主体资料的端口。
     * @param tokenAccessor             从请求头或 Cookie 提取访问令牌的端口。
     * @param systemConfigValueProvider 可选的系统配置读取端口，用于提供用户时区等运行时配置。
     * @return 返回从窄 Session 读取端口、令牌访问器和系统配置适配出的安全上下文 Bean；Bean 创建失败时启动失败，不返回 null。
     */
    @Bean
    public SecurityContextAccessor securityContextAccessor(SecuritySessionReader sessionReader,
                                                           SecurityTokenAccessor tokenAccessor,
                                                           ObjectProvider<SystemConfigValueProvider> systemConfigValueProvider) {
        return new SecuritySessionContextAccessor(sessionReader, tokenAccessor, systemConfigValueProvider);
    }

    /**
     * 处理内部业务逻辑（{@code securitySessionRevocationPort}）。
     *
     * @param sessionRevoker 执行按用户、按令牌和按刷新令牌撤销会话的用例。
     * @return 返回封装用户 Session 撤销操作的安全端口 Bean；端口创建成功后始终非 null。
     */
    @Bean
    public SecuritySessionRevocationPort securitySessionRevocationPort(SecuritySessionRevoker sessionRevoker) {
        return new SecuritySessionRevocationPort() {
            /**
             * 通过会话撤销端口使用户会话失效。
             *
             * @param userId 目标用户的唯一标识，用于限定会话和授权范围。
             */
            @Override
            public void revokeUserSessions(UUID userId) {
                sessionRevoker.deleteByUserId(userId);
            }

            /**
             * 撤销除当前令牌外的用户会话。
             *
             * @param userId      目标用户的唯一标识，用于限定会话和授权范围。
             * @param accessToken 待排除或撤销的访问令牌；不得写入日志。
             */
            @Override
            public void revokeUserSessionsExceptToken(UUID userId, String accessToken) {
                sessionRevoker.deleteByUserIdExceptToken(userId, accessToken);
            }

            /**
             * 撤销用户指定客户端的全部会话。
             *
             * @param userId     目标用户的唯一标识，用于限定会话和授权范围。
             * @param clientType 客户端类型，用于选择对应的安全会话策略。
             */
            @Override
            public void revokeUserClientSessions(UUID userId, com.devops00.spectra.common.constant.ClientType clientType) {
                sessionRevoker.deleteByUserIdAndClient(userId.toString(), clientType);
            }
        };
    }

    /**
     * 处理内部业务逻辑（{@code securitySessionQueryPort}）。
     *
     * @param sessionQuery 查询在线用户和当前会话状态的用例。
     * @return 返回封装在线用户和当前 Session 查询操作的安全端口 Bean；端口创建成功后始终非 null。
     */
    @Bean
    public SecuritySessionQueryPort securitySessionQueryPort(SecuritySessionQuery sessionQuery) {
        return sessionQuery::listOnlineUsers;
    }

    /**
     * 处理内部业务逻辑（{@code securityAuthenticationPort}）。
     *
     * @param sessionIssuer       创建登录成功后安全会话和令牌的用例。
     * @param sessionRefresher    校验刷新令牌并执行令牌轮换的用例。
     * @param sessionRevoker      注销单个令牌或用户会话的用例。
     * @param loginFailureTracker 记录、读取和清理登录失败锁定计数的用例。
     * @return 返回封装登录、刷新、失败锁定和登出操作的认证端口 Bean；端口创建成功后始终非 null。
     */
    @Bean
    public SecurityAuthenticationPort securityAuthenticationPort(SecuritySessionIssuer sessionIssuer,
                                                                 SecuritySessionRefresher sessionRefresher,
                                                                 SecuritySessionRevoker sessionRevoker,
                                                                 SecurityLoginFailureTracker loginFailureTracker) {
        return new SecurityAuthenticationPort() {
            /**
             * 通过登录端口创建认证会话。
             *
             * @param user 当前认证用户资料，不包含可记录的敏感凭据。
             * @return 返回登录成功后创建的访问令牌和刷新令牌；Session 写入或策略校验失败时抛出异常，不返回 null。
             */
            @Override
            public SecurityToken login(SecurityPrincipal user) {
                return sessionIssuer.createToken(user);
            }

            /**
             * 通过登录端口注销当前会话。
             *
             * @param token 待摘要、校验或撤销的访问令牌；不得写入日志。
             */
            @Override
            public void logout(String token) {
                sessionRevoker.deleteToken(token);
            }

            /**
             * 通过刷新令牌注销关联会话。
             *
             * @param refreshToken 待轮换或撤销的刷新令牌；不得写入日志。
             */
            @Override
            public void logoutByRefreshToken(String refreshToken) {
                sessionRevoker.deleteByRefreshToken(refreshToken);
            }

            /**
             * 初始化或配置 Framework 的 refreshByRefreshToken。
             *
             * @param refreshToken 待轮换或撤销的刷新令牌；不得写入日志。
             * @return 返回刷新成功后轮换出的访问令牌和刷新令牌；刷新令牌无效、重放或 Redis 失败时抛出异常，不返回 null。
             */
            @Override
            public SecurityToken refreshByRefreshToken(String refreshToken) {
                return sessionRefresher.refreshByRefreshToken(refreshToken);
            }

            /**
             * 获取或判断 Framework 的 isLockedOut 结果。
             *
             * @param username 要查询登录失败锁定状态的用户名；不写入日志。
             * @return 返回用户名当前是否被登录失败策略锁定；未锁定时返回 false，Redis 无法确认状态时抛出异常。
             */
            @Override
            public boolean isLockedOut(String username) {
                return loginFailureTracker.isLockedOut(username);
            }

            /**
             * 记录登录失败次数并在达到阈值时锁定登录。
             *
             * @param username 发生登录失败的用户名；用于更新安全 Redis 计数，不写入日志。
             */
            @Override
            public void recordLoginFail(String username) {
                loginFailureTracker.recordLoginFail(username);
            }

            /**
             * 清除已成功认证用户的登录失败计数。
             *
             * @param username 已完成认证并需要清理失败计数的用户名。
             */
            @Override
            public void clearLoginFail(String username) {
                loginFailureTracker.clearLoginFail(username);
            }
        };
    }

    /**
     * 处理内部业务逻辑（{@code securityUserLookupPort}）。
     *
     * @param sessionReader 读取访问令牌对应用户资料的安全 Session 端口。
     * @return 返回从安全 Session 读取用户资料的用户查找端口 Bean；端口创建成功后始终非 null。
     */
    @Bean
    public SecurityUserLookupPort securityUserLookupPort(SecuritySessionReader sessionReader) {
        return token -> sessionReader.getCurrentUser(token);
    }

}
