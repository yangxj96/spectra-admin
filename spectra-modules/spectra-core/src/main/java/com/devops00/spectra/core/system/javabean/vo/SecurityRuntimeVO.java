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

package com.devops00.spectra.core.system.javabean.vo;

/**
 * 封装安全运行时环境相关的响应数据。
 *
 * @param status                 业务状态
 * @param onlineSessionCount     在线会话数量
 * @param sessionStatus          会话状态
 * @param verificationStatus     安全验证功能当前的运行状态
 * @param loginFailureStatus     失败状态
 * @param nonceStatus            随机数状态
 * @param nonceCutoffEpochSecond 随机数清理截止时间的 Unix 秒数
 * @param refreshReplayStatus    刷新状态
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record SecurityRuntimeVO(
                                String status,
                                Long onlineSessionCount,
                                String sessionStatus,
                                String verificationStatus,
                                String loginFailureStatus,
                                String nonceStatus,
                                Long nonceCutoffEpochSecond,
                                String refreshReplayStatus) {
}
