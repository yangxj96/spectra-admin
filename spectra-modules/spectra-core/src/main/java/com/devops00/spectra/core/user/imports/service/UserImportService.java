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
     */
    UserImportTaskVO preview(UserImportPreviewFrom params);

    /**
     * 查询或获取目标数据（{@code detail}）。
     */
    UserImportTaskVO detail(UUID id);

    /**
     * 处理内部业务逻辑（{@code errors}）。
     */
    List<UserImportRowVO> errors(UUID id);

    /**
     * 更新或推进目标状态（{@code apply}）。
     */
    UserImportTaskVO apply(UUID id, UserImportApplyFrom params);
}
