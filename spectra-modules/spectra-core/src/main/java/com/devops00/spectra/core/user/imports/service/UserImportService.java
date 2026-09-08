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

package com.devops00.spectra.core.user.imports.service;

import com.devops00.spectra.core.user.imports.javabean.from.UserImportApplyFrom;
import com.devops00.spectra.core.user.imports.javabean.from.UserImportPreviewFrom;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportRowVO;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportTaskVO;

import java.util.List;
import java.util.UUID;

/** 用户批量导入应用服务。 */
public interface UserImportService {

    /**
     * 处理内部业务逻辑（{@code preview}）。
     *
     * @param params 导入幂等键、文件名、文件摘要、重复用户处理策略和待导入用户行。
     * @return 返回用户导入任务的校验预览结果；输入不合法或当前用户无权操作时抛出业务异常，不返回 null。
     */
    UserImportTaskVO preview(UserImportPreviewFrom params);

    /**
     * 查询或获取目标数据（{@code detail}）。
     *
     * @param id 要读取导入任务详情的导入任务唯一标识。
     * @return 返回符合条件的用户导入任务详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    UserImportTaskVO detail(UUID id);

    /**
     * 处理内部业务逻辑（{@code errors}）。
     *
     * @param id 要查看导入错误行的导入任务唯一标识。
     * @return 返回符合查询条件的用户导入错误行列表；无匹配时返回空列表，不返回 null。
     */
    List<UserImportRowVO> errors(UUID id);

    /**
     * 更新或推进目标状态（{@code apply}）。
     *
     * @param id     目标用户记录的唯一标识。
     * @param params 预览阶段签发的一次性导入凭证，用于确认并应用已校验的导入任务。
     * @return 返回已应用导入结果的任务，包含成功、跳过和失败行数及完成状态；凭证无效或写入失败时抛出业务异常，不返回 null。
     */
    UserImportTaskVO apply(UUID id, UserImportApplyFrom params);
}
