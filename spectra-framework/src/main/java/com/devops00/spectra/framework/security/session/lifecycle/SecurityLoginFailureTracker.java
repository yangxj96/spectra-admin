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

package com.devops00.spectra.framework.security.session.lifecycle;

/**
 * 登录失败计数与临时锁定窄端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface SecurityLoginFailureTracker {

    /** 记录一次登录失败；参数为登录标识规范化后的摘要桶，不传原文。 */
    void recordLoginFail(String identityBucket);

    /** 判断登录失败锁定状态；参数为登录标识规范化后的摘要桶。 */
    boolean isLockedOut(String identityBucket);

    /** 清理登录失败计数；参数与登录锁定检查使用同一摘要桶。 */
    void clearLoginFail(String identityBucket);
}
