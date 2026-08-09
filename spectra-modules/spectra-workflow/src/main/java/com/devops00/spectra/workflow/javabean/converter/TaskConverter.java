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

package com.devops00.spectra.workflow.javabean.converter;

import com.devops00.spectra.workflow.javabean.vo.TaskVO;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 任务相关对象转换器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/18
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface TaskConverter {

    /**
     * Flowable Task 转 TaskVO
     */
    @Mapping(source = "createTime", target = "createTime")
    TaskVO toVO(org.flowable.task.api.Task source);

    /**
     * 历史任务实例转 TaskVO（startTime 作为 createTime）
     */
    @Mapping(source = "startTime", target = "createTime")
    TaskVO fromHistoricTask(HistoricTaskInstance source);
}
