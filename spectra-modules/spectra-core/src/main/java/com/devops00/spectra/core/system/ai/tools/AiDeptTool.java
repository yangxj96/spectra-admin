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

package com.devops00.spectra.core.system.ai.tools;


import com.devops00.spectra.ai.base.AiToolMarker;
import com.devops00.spectra.ai.base.ToolExecutor;
import com.devops00.spectra.ai.base.AiMemoryId;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.core.system.service.DepartmentService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/// AI使用的部门相关工具
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/6/18 17:56
@Slf4j
@Component
@RequiredArgsConstructor
public class AiDeptTool implements AiToolMarker {

    private final DepartmentService departmentService;

    /// 获取所有部门信息
    ///
    /// @param memoryId 复合记忆标识
    @Tool("获取所有部门信息")
    public String getAllDepartments(@ToolMemoryId AiMemoryId memoryId) {
        return ToolExecutor.execute(memoryId.token(), _ -> {
            log.debug("{}获取所有部门信息", LogPrefix.AI.p());
            return departmentService.list();
        });
    }

}
