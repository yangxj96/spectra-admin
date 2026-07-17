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
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.workflow.javabean.entity.FormDefinition;
import com.devops00.spectra.workflow.javabean.from.FormDefinitionSaveFrom;
import com.devops00.spectra.workflow.javabean.from.FormPageFrom;
import com.devops00.spectra.workflow.javabean.from.FormVersionSaveFrom;
import com.devops00.spectra.workflow.javabean.vo.FormDefinitionVO;
import com.devops00.spectra.workflow.javabean.vo.FormVersionVO;

import java.util.List;
import java.util.UUID;

/// 工作流-表单定义Service
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
public interface FormDefinitionService extends BaseService<FormDefinition> {

    /// 分页查询表单列表
    ///
    /// @param page   分页参数
    /// @param params 查询条件
    /// @return 分页结果
    IPage<FormDefinitionVO> page(PageFrom page, FormPageFrom params);

    /// 查询表单详情（含当前版本内容）
    ///
    /// @param id 表单定义ID
    /// @return 表单详情
    FormDefinitionVO getDetail(UUID id);

    /// 创建表单（同时创建版本1）
    ///
    /// @param from 创建参数
    void create(FormDefinitionSaveFrom from);

    /// 更新表单元数据
    ///
    /// @param id   表单定义ID
    /// @param from 更新参数
    void update(UUID id, FormDefinitionSaveFrom from);

    /// 删除表单（级联删除版本）
    ///
    /// @param id 表单定义ID
    void remove(UUID id);

    /// 保存新版本（版本号自增）
    ///
    /// @param id   表单定义ID
    /// @param from 版本内容
    void saveVersion(UUID id, FormVersionSaveFrom from);

    /// 查询版本历史
    ///
    /// @param id 表单定义ID
    /// @return 版本列表
    List<FormVersionVO> getVersions(UUID id);

    /// 查询指定版本详情
    ///
    /// @param id      表单定义ID
    /// @param version 版本号
    /// @return 版本详情
    FormVersionVO getVersion(UUID id, Integer version);

}
