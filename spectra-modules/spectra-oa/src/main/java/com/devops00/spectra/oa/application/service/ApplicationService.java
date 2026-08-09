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

/**
 * OA 通用申请生命周期服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public interface ApplicationService {
    /**
     * 分页查询当前用户可见的申请。
     */
    IPage<ApplicationVO> page(PageFrom page, ApplicationPageFrom params);

    /**
     * 查询当前用户可见的申请详情。
     */
    ApplicationVO get(UUID id);

    /**
     * 查询当前用户可发起的申请类型。
     */
    List<ApplicationTypeVO> listTypes();

    /**
     * 查询全部启用的申请类型。
     */
    List<ApplicationTypeVO> listAllTypes();

    /**
     * 创建申请类型。
     */
    java.util.UUID createdType(ApplicationTypeSaveFrom from);

    /**
     * 修改申请类型。
     */
    void modifyType(java.util.UUID id, ApplicationTypeSaveFrom from);

    /**
     * 删除申请类型。
     */
    void deleteType(java.util.UUID id);

    /**
     * 创建业务申请草稿。
     */
    Application createDraft(String typeCode, UUID bizId, String title);

    /**
     * 绑定申请关联的业务主键。
     */
    void bindBizId(UUID id, UUID bizId);

    /**
     * 绑定申请对应的流程实例。
     */
    void bindProcessInstance(UUID id, String processInstanceId);

    /**
     * 查询申请实体，不校验当前用户可见性。
     */
    Application require(UUID id);

    /**
     * 查询并校验当前用户可见性的申请实体。
     */
    Application requireVisible(UUID id);

    /**
     * 提交申请进入审批流程。
     */
    void submit(UUID id);

    /**
     * 撤回审批中的申请。
     */
    void withdraw(UUID id);

    /**
     * 取消申请。
     */
    void cancel(UUID id);

    /**
     * 更新申请审批状态及原因。
     */
    void updateStatus(UUID id, String status, String reason);

    /**
     * 统计当前用户指定状态的申请数量。
     */
    long countMine(String status);
}
