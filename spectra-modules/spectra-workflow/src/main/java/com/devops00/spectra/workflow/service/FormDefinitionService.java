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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.base.BaseService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.workflow.javabean.entity.FormDefinition;
import com.devops00.spectra.workflow.javabean.from.FormDefinitionSaveFrom;
import com.devops00.spectra.workflow.javabean.from.FormPageFrom;
import com.devops00.spectra.workflow.javabean.from.FormVersionSaveFrom;
import com.devops00.spectra.workflow.javabean.vo.FormDefinitionVO;
import com.devops00.spectra.workflow.javabean.vo.FormVersionVO;

import java.util.List;
import java.util.UUID;

/**
 * 工作流-表单定义Service
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/17
 */
public interface FormDefinitionService extends BaseService<FormDefinition> {

    /**
     * 分页查询表单列表
     *
     * @param page   表单定义列表的页码、页大小及排序字段。
     * @param params 表单名称、编码和启用状态等分页筛选条件。
     * @return 返回按分页条件查询的工作流表单定义分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<FormDefinitionVO> page(PageFrom page, FormPageFrom params);

    /**
     * 查询表单详情（含当前版本内容）
     *
     * @param id 要读取当前版本内容的表单定义唯一标识。
     * @return 返回表单定义及其当前版本内容；表单不存在时抛出业务异常，不返回 null。
     */
    FormDefinitionVO getDetail(UUID id);

    /**
     * 创建表单（同时创建版本1）
     *
     * @param from 表单编码、名称、描述及初始版本字段定义和内容。
     */
    void created(FormDefinitionSaveFrom from);

    /**
     * 更新表单元数据
     *
     * @param id   待修改表单定义的唯一标识。
     * @param from 表单编码、名称、描述和启用状态等元数据字段。
     */
    void modify(UUID id, FormDefinitionSaveFrom from);

    /**
     * 删除表单（级联删除版本）
     *
     * @param id 待删除表单定义的唯一标识；删除时同时清理其版本记录。
     */
    void deleteById(UUID id);

    /**
     * 保存新版本（版本号自增）
     *
     * @param id   要新增版本的表单定义唯一标识。
     * @param from 新版本号对应的字段定义、表单内容和版本说明。
     */
    void saveVersion(UUID id, FormVersionSaveFrom from);

    /**
     * 查询版本历史
     *
     * @param id 要查询版本历史的表单定义唯一标识。
     * @return 返回符合查询条件的表单版本列表；无匹配时返回空列表，不返回 null。
     */
    List<FormVersionVO> getVersions(UUID id);

    /**
     * 查询指定版本详情
     *
     * @param id      要查询版本的表单定义唯一标识。
     * @param version 要读取的表单版本号。
     * @return 返回指定表单版本的字段和内容；表单或版本不存在时抛出业务异常，不返回 null。
     */
    FormVersionVO getVersion(UUID id, Integer version);
}
