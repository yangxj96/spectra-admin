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
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 默认用户密码配置启动初始化测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class UserDefaultPasswordSettingRunnerTest {

    private static final String RUNNER_CLASS = "com.devops00.spectra.core.system.runner.UserDefaultPasswordSettingRunner";

    @Test
    void shouldEnsureAnEmptySecretSettingWithoutReplacingItsValue() throws Exception {
        var configuredService = mock(ConfiguredService.class);
        var arguments = mock(ApplicationArguments.class);
        var runnerType = Class.forName(RUNNER_CLASS);
        var runner = runnerType.getConstructor(ConfiguredService.class).newInstance(configuredService);

        runnerType.getMethod("run", ApplicationArguments.class).invoke(runner, arguments);

        verify(configuredService).ensureExists(SystemConfigKeys.USER_DEFAULT_PASSWORD, "", ConfiguredValueType.SECRET,
                "新建、导入和管理员重置用户使用的默认密码");
    }
}
