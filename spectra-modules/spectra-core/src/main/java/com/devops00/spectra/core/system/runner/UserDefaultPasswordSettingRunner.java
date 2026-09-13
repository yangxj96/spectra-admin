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

package com.devops00.spectra.core.system.runner;

import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.system.service.ConfiguredService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时确保默认用户密码设置项存在。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
@RequiredArgsConstructor
public class UserDefaultPasswordSettingRunner implements ApplicationRunner {

    /** 持久化系统默认密码设置项。 */
    private final ConfiguredService configuredService;

    @Override
    public void run(ApplicationArguments args) {
        configuredService.ensureExists(SystemConfigKeys.USER_DEFAULT_PASSWORD, "", ConfiguredValueType.SECRET,
                "新建、导入和管理员重置用户使用的默认密码");
    }
}
