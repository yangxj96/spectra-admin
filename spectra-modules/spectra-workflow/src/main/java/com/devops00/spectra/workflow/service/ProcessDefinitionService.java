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

/**
 * 流程定义Service
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/18
 */
public interface ProcessDefinitionService {

    /**
     * 获取所有流程定义
     *
     * @return 返回当前部署的全部流程定义；没有已部署定义时返回空列表，不返回 null。
     */
    List<ProcessDefinitionVO> listAll();

    /**
     * 获取流程定义详情
     *
     * @param id 要读取元数据和状态的流程定义唯一标识。
     * @return 返回指定流程定义的元数据和当前状态；定义不存在时抛出业务异常，不返回 null。
     */
    ProcessDefinitionVO getDetail(String id);

    /**
     * 获取流程定义图（PNG）
     *
     * @param id 要生成 BPMN 图的流程定义唯一标识。
     * @return 返回指定流程定义 BPMN 图的 PNG 字节数组；定义不存在或图生成失败时抛出业务异常，不返回 null 或空数组。
     */
    byte[] getDiagram(String id);

    /**
     * 挂起流程定义
     *
     * @param id 要挂起的流程定义唯一标识；挂起后不再允许启动新实例。
     */
    void suspend(String id);

    /**
     * 激活流程定义
     *
     * @param id 要激活的流程定义唯一标识。
     */
    void activate(String id);

    /**
     * 获取流程定义的 BPMN XML 源码
     *
     * @param id 要读取 BPMN XML 资源的流程定义唯一标识。
     * @return 返回指定流程定义的 BPMN XML 资源；定义或资源不存在时抛出业务异常，不返回 null。
     */
    ProcessDefinitionResourceVO getResource(String id);

    /**
     * 部署流程定义
     *
     * @param from 待部署 BPMN 资源、流程定义键和名称等部署字段。
     * @return 返回部署成功后的流程定义视图，包含定义标识和最新版本；校验或部署失败时抛出业务异常，不返回 null。
     */
    ProcessDefinitionVO deploy(DeployProcessFrom from);
}
