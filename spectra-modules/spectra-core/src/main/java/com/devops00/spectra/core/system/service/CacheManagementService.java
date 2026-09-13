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

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.system.javabean.from.CacheBusinessClearFrom;
import com.devops00.spectra.core.system.javabean.from.CacheBusinessPreviewFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityLoginFailureClearFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityNonceGlobalInvalidateFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityNonceInvalidateFrom;
import com.devops00.spectra.core.system.javabean.from.SecuritySessionRevokeAllFrom;
import com.devops00.spectra.core.system.javabean.from.SecuritySessionRevokeFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityVerificationClearFrom;
import com.devops00.spectra.core.system.javabean.vo.CacheMonitorOverviewVO;
import com.devops00.spectra.core.system.javabean.vo.CacheOperationVO;
import com.devops00.spectra.core.system.javabean.vo.CacheRegionVO;
import com.devops00.spectra.core.system.javabean.vo.SecurityRuntimeVO;

import java.util.List;
import java.util.UUID;

/**
 * 缓存监控、普通缓存维护和安全运行态操作的应用服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface CacheManagementService {

    /**
     * 查询缓存和安全运行态总览。
     *
     * @return 返回普通缓存区域数、在线 Session 数和脱敏安全 Redis 状态。
     */
    CacheMonitorOverviewVO getOverview();

    /**
     * 查询显式登记的普通缓存区域。
     *
     * @return 返回区域元数据及可用统计字段，不包含 Redis Key。
     */
    List<CacheRegionVO> getRegions();

    /**
     * 查询安全运行态摘要。
     *
     * @return 返回 Session、验证码、登录失败、nonce 和 Refresh 防重放的脱敏状态。
     */
    SecurityRuntimeVO getSecurity();

    /**
     * 查询当前实例已知的普通缓存操作状态。
     *
     * @param operationId 普通缓存操作 ID。
     * @return 返回操作回执；当前实例未知时返回 UNKNOWN。
     */
    CacheOperationVO getOperation(UUID operationId);

    /**
     * 预览普通缓存清理影响范围。
     *
     * @param from 普通缓存区域、全部区域和实例范围。
     * @return 返回待清理的已登记区域数量和范围说明。
     */
    CacheOperationVO previewBusinessClear(CacheBusinessPreviewFrom from);

    /**
     * 执行普通缓存清理并按请求广播到其他实例。
     *
     * @param from 普通缓存区域、实例范围、理由和确认信息。
     * @return 返回本实例清理结果及普通 Redis 广播接受状态。
     */
    CacheOperationVO clearBusiness(CacheBusinessClearFrom from);

    /**
     * 撤销用户指定客户端的安全 Session。
     *
     * @param from 用户、客户端和二次确认信息。
     * @return 返回安全 Session 撤销结果。
     */
    CacheOperationVO revokeSession(SecuritySessionRevokeFrom from);

    /**
     * 撤销用户全部安全 Session。
     *
     * @param from 用户和二次确认信息。
     * @return 返回安全 Session 撤销结果。
     */
    CacheOperationVO revokeAllSessions(SecuritySessionRevokeAllFrom from);

    /**
     * 清理受控验证码及可选的登录验证码尝试计数。
     *
     * @param from 验证码类型、目标和二次确认信息。
     * @return 返回验证码安全状态清理结果。
     */
    CacheOperationVO clearVerification(SecurityVerificationClearFrom from);

    /**
     * 清理账号登录失败计数。
     *
     * @param from 登录账号和二次确认信息。
     * @return 返回失败计数清理结果，不改变用户生命周期状态。
     */
    CacheOperationVO clearLoginFailure(SecurityLoginFailureClearFrom from);

    /**
     * 定向失效 Web 加密请求 nonce。
     *
     * @param from nonce 和二次确认信息。
     * @return 返回定向 nonce 失效结果。
     */
    CacheOperationVO invalidateNonce(SecurityNonceInvalidateFrom from);

    /**
     * 推进 Web 加密请求 nonce 的全局失效窗口。
     *
     * @param from 操作理由和固定确认短语。
     * @return 返回全局 cutoff 和旧 nonce 记录清理结果。
     */
    CacheOperationVO invalidateAllNonces(SecurityNonceGlobalInvalidateFrom from);
}
