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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.javabean.from.ApplicationPageFrom;
import com.devops00.spectra.oa.application.javabean.from.ApplicationTypeSaveFrom;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationTypeVO;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationVO;

import java.util.List;
import java.util.UUID;

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
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 申请类型、审批状态和关键字等当前用户可见申请的分页筛选条件。
     * @return 返回按分页条件查询的OA 申请分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<ApplicationVO> page(PageFrom page, ApplicationPageFrom params);

    /**
     * 查询当前用户可见的申请详情。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回符合条件的OA 申请详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    ApplicationVO get(UUID id);

    /**
     * 查询当前用户可发起的申请类型。
     *
     * @return 返回符合查询条件的OA 申请类型列表；无匹配时返回空列表，不返回 null。
     */
    List<ApplicationTypeVO> listTypes();

    /**
     * 查询全部启用的申请类型。
     *
     * @return 返回符合查询条件的OA 申请类型列表；无匹配时返回空列表，不返回 null。
     */
    List<ApplicationTypeVO> listAllTypes();

    /**
     * 创建申请类型。
     *
     * @param from 申请类型编码、名称、表单定义、流程定义、启用状态和排序等配置字段。
     * @return 返回新建申请类型的唯一标识，供申请表单和申请校验引用；编码冲突或写入失败时抛出业务异常，不返回 null。
     */
    UUID createdType(ApplicationTypeSaveFrom from);

    /**
     * 修改申请类型。
     *
     * @param id   待修改申请类型的唯一标识。
     * @param from 申请类型编码、名称、表单定义、流程定义、启用状态和排序等修改字段。
     */
    void modifyType(UUID id, ApplicationTypeSaveFrom from);

    /**
     * 删除申请类型。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void deleteType(UUID id);

    /**
     * 创建业务申请草稿。
     *
     * @param typeCode 业务类型编码，用于选择对应处理规则。
     * @param bizId    关联业务对象的唯一标识。
     * @param title    业务标题，用于展示和检索。
     * @return 返回已创建的申请草稿实体，包含申请类型、关联业务 ID、标题和草稿状态；校验或写入失败时抛出业务异常，不返回 null。
     */
    Application createDraft(String typeCode, UUID bizId, String title);

    /**
     * 绑定申请关联的业务主键。
     *
     * @param id    目标OA 业务记录的唯一标识。
     * @param bizId 关联业务对象的唯一标识。
     */
    void bindBizId(UUID id, UUID bizId);

    /**
     * 绑定申请对应的流程实例。
     *
     * @param id                目标OA 业务记录的唯一标识。
     * @param processInstanceId 流程实例的唯一标识。
     */
    void bindProcessInstance(UUID id, String processInstanceId);

    /**
     * 查询申请实体，不校验当前用户可见性。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回指定申请的完整实体，供流程提交和状态迁移使用；申请不存在时抛出业务异常，不返回 null。
     */
    Application require(UUID id);

    /**
     * 查询并校验当前用户可见性的申请实体。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回当前用户可见的申请实体，供外部业务读取或状态迁移使用；申请不存在或不可见时抛出业务异常，不返回 null。
     */
    Application requireVisible(UUID id);

    /**
     * 提交申请进入审批流程。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void submit(UUID id);

    /**
     * 撤回审批中的申请。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void withdraw(UUID id);

    /**
     * 取消申请。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void cancel(UUID id);

    /**
     * 更新申请审批状态及原因。
     *
     * @param id     目标OA 业务记录的唯一标识。
     * @param status 目标记录的状态值，用于筛选或状态迁移。
     * @param reason 本次状态变更或撤销的业务原因。
     */
    void updateStatus(UUID id, String status, String reason);

    /**
     * 统计当前用户指定状态的申请数量。
     *
     * @param status 目标记录的状态值，用于筛选或状态迁移。
     * @return 返回当前用户处于指定状态的申请数量；没有匹配申请时返回 0，查询失败时抛出异常。
     */
    long countMine(String status);
}
