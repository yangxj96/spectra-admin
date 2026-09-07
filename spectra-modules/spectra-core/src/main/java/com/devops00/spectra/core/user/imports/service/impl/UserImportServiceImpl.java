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

package com.devops00.spectra.core.user.imports.service.impl;

import com.devops00.spectra.core.user.imports.javabean.from.UserImportApplyFrom;
import com.devops00.spectra.core.user.imports.javabean.from.UserImportPreviewFrom;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportRowVO;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportTaskVO;
import com.devops00.spectra.core.user.imports.service.UserImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** 用户批量导入公开应用入口，只负责把请求路由到对应的用例服务。 */
@Service
@RequiredArgsConstructor
public class UserImportServiceImpl implements UserImportService {

    private final UserImportPreviewService previewService;

    private final UserImportExecutionService executionService;

    private final UserImportResultService resultService;

    @Override
    public UserImportTaskVO preview(UserImportPreviewFrom params) {
        return previewService.preview(params);
    }

    @Override
    public UserImportTaskVO detail(UUID id) {
        return resultService.detail(id);
    }

    @Override
    public List<UserImportRowVO> errors(UUID id) {
        return resultService.errors(id);
    }

    @Override
    public UserImportTaskVO apply(UUID id, UserImportApplyFrom params) {
        return executionService.apply(id, params);
    }
}
