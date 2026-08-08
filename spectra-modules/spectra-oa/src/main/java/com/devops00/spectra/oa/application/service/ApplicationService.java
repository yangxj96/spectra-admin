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

package com.devops00.spectra.oa.application.service;

import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.javabean.from.ApplicationPageFrom;
import com.devops00.spectra.oa.application.javabean.from.ApplicationTypeSaveFrom;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationTypeVO;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationVO;

/// OA 通用申请生命周期服务。
public interface ApplicationService {
    IPage<ApplicationVO> page(PageFrom page, ApplicationPageFrom params);

    ApplicationVO get(UUID id);

    List<ApplicationTypeVO> listTypes();

    List<ApplicationTypeVO> listAllTypes();

    java.util.UUID createdType(ApplicationTypeSaveFrom from);

    void modifyType(java.util.UUID id, ApplicationTypeSaveFrom from);

    void deleteType(java.util.UUID id);

    Application createDraft(String typeCode, UUID bizId, String title);

    void bindBizId(UUID id, UUID bizId);

    void bindProcessInstance(UUID id, String processInstanceId);

    Application require(UUID id);

    void submit(UUID id);

    void withdraw(UUID id);

    void cancel(UUID id);

    void updateStatus(UUID id, String status, String reason);

    long countMine(String status);
}
