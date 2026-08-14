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

package com.devops00.spectra.security.base.policy;

import com.devops00.spectra.security.base.session.SessionPolicy;
import org.jspecify.annotations.Nullable;

/**
 * 读取登录端会话策略的窄端口。
 * <p>
 * 基础安全适配层只依赖该端口，不依赖具体数据库实现；没有提供策略时由适配层使用部署默认值。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/15
 */
public interface SecuritySessionPolicyProvider {

    /**
     * 按登录端 code 读取活动策略。
     *
     * @param clientCode 登录端 code，例如 web/app/mini
     * @return 数据库策略，不存在时返回 null
     */
    @Nullable
    SessionPolicy find(String clientCode);
}
