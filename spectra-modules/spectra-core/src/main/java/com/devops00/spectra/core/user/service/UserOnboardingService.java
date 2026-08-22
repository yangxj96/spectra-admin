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

import com.devops00.spectra.core.user.javabean.from.UserOnboardingFrom;
import com.devops00.spectra.core.user.javabean.vo.UserOnboardingVO;

/**
 * 用户资料和角色授权的连续提交服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface UserOnboardingService {

    /**
     * 在同一事务中提交用户资料和多角色授权变更。
     *
     * @param params 用户资料与授权配置
     * @return 提交后的用户标识
     */
    UserOnboardingVO submit(UserOnboardingFrom params);
}
