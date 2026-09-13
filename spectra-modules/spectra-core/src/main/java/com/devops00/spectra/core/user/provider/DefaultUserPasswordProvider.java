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

package com.devops00.spectra.core.user.provider;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.service.ConfiguredService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 从系统设置读取新建、导入和重置用户使用的默认密码哈希。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@RequiredArgsConstructor
public class DefaultUserPasswordProvider {

    /** 读取系统默认密码设置并在缺失时阻止用户凭据写入。 */
    private final ConfiguredService configuredService;

    /**
     * 返回系统默认密码的编码值。
     *
     * @return 当前已配置的密码哈希。
     * @throws DataException 未配置默认密码时抛出。
     */
    public String requireEncodedPassword() {
        return configuredService.findValue(SystemConfigKeys.USER_DEFAULT_PASSWORD)
                .orElseThrow(() -> new DataException("系统默认密码未配置，请先在系统设置中配置"));
    }
}
