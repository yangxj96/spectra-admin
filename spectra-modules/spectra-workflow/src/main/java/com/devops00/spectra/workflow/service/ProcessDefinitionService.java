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

package com.devops00.spectra.workflow.service;

import com.devops00.spectra.workflow.javabean.from.DeployProcessFrom;
import com.devops00.spectra.workflow.javabean.vo.ProcessDefinitionResourceVO;
import com.devops00.spectra.workflow.javabean.vo.ProcessDefinitionVO;

import java.util.List;

/// 流程定义Service
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/18
public interface ProcessDefinitionService {

    /// 获取所有流程定义
    List<ProcessDefinitionVO> listAll();

    /// 获取流程定义详情
    ProcessDefinitionVO getDetail(String id);

    /// 获取流程定义图（PNG）
    byte[] getDiagram(String id);

    /// 挂起流程定义
    void suspend(String id);

    /// 激活流程定义
    void activate(String id);

    /// 获取流程定义的 BPMN XML 源码
    ProcessDefinitionResourceVO getResource(String id);

    /// 部署流程定义
    ProcessDefinitionVO deploy(DeployProcessFrom from);
}
