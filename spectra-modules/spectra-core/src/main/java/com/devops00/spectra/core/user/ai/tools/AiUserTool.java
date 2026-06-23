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

package com.devops00.spectra.core.user.ai.tools;

import com.devops00.spectra.ai.base.AiToolMarker;
import com.devops00.spectra.ai.base.ToolExecutor;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.core.user.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/// 用户相关AI调用的tool
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/29 16:06
@Slf4j
@Component
@RequiredArgsConstructor
public class AiUserTool implements AiToolMarker {

    private final UserService userService;

    /// 获取所有用户信息
    ///
    /// @param token 当前请求token
    @Tool("获取所有用户信息")
    public String getAllUsers(@ToolMemoryId String token) {
        return ToolExecutor.execute(token, _ -> {
            log.debug("{}获取所有用户信息", LogPrefix.AI.p());
            return userService.list();
        });
    }

}
