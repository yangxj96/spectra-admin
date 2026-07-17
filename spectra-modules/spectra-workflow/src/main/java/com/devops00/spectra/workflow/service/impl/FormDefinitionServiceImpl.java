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

package com.devops00.spectra.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.workflow.javabean.entity.FormDefinition;
import com.devops00.spectra.workflow.javabean.entity.FormVersion;
import com.devops00.spectra.workflow.javabean.from.FormDefinitionSaveFrom;
import com.devops00.spectra.workflow.javabean.from.FormPageFrom;
import com.devops00.spectra.workflow.javabean.from.FormVersionSaveFrom;
import com.devops00.spectra.workflow.javabean.vo.FormDefinitionVO;
import com.devops00.spectra.workflow.javabean.vo.FormVersionVO;
import com.devops00.spectra.workflow.mapper.FormDefinitionMapper;
import com.devops00.spectra.workflow.mapper.FormVersionMapper;
import com.devops00.spectra.workflow.service.FormDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/// 工作流-表单定义Service实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
@Slf4j
@Service
@RequiredArgsConstructor
public class FormDefinitionServiceImpl extends BaseServiceImpl<FormDefinitionMapper, FormDefinition>
        implements FormDefinitionService {

    private final FormVersionMapper formVersionMapper;

    @Override
    public IPage<FormDefinitionVO> page(PageFrom page, FormPageFrom params) {
        var wrapper = new LambdaQueryWrapper<FormDefinition>();
        if (StringUtils.hasText(params.getName())) {
            wrapper.like(FormDefinition::getName, params.getName());
        }
        if (params.getActive() != null) {
            wrapper.eq(FormDefinition::getActive, params.getActive());
        }
        wrapper.orderByDesc(FormDefinition::getCreatedAt);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<FormDefinitionVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public FormDefinitionVO getDetail(UUID id) {
        var entity = this.getById(id);
        if (entity == null) {
            throw new DataNotExistException("表单定义不存在");
        }
        var vo = toVO(entity);
        // 查询当前版本内容
        var version = formVersionMapper.selectOne(
                new LambdaQueryWrapper<FormVersion>()
                        .eq(FormVersion::getFormDefinitionId, id)
                        .eq(FormVersion::getFormVersion, entity.getCurrentVersion())
        );
        if (version != null) {
            vo.setRuleJson(version.getRuleJson());
            vo.setOptionsJson(version.getOptionsJson());
            vo.setFormJson(version.getFormJson());
        }
        return vo;
    }

    @Override
    @Transactional
    public void create(FormDefinitionSaveFrom from) {
        // 创建表单定义
        var entity = new FormDefinition();
        entity.setName(from.getName());
        // 编码自动生成：UUID前8位大写
        entity.setCode(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        entity.setDescription(from.getDescription());
        entity.setCurrentVersion(1);
        entity.setActive(true);
        if (!this.save(entity)) {
            throw new DataSaveException("创建表单定义失败");
        }
        // 创建版本1
        var version = new FormVersion();
        version.setFormDefinitionId(entity.getId());
        version.setFormVersion(1);
        version.setRuleJson(from.getRuleJson());
        version.setOptionsJson(from.getOptionsJson());
        version.setFormJson(from.getFormJson());
        if (formVersionMapper.insert(version) <= 0) {
            throw new DataSaveException("创建表单版本失败");
        }
        log.info("创建表单定义成功: id={}, name={}", entity.getId(), entity.getName());
    }

    @Override
    @Transactional
    public void update(UUID id, FormDefinitionSaveFrom from) {
        var entity = this.getById(id);
        if (entity == null) {
            throw new DataNotExistException("表单定义不存在");
        }
        entity.setName(from.getName());
        entity.setCode(from.getCode());
        entity.setDescription(from.getDescription());
        if (!this.updateById(entity)) {
            throw new DataSaveException("更新表单定义失败");
        }
        log.info("更新表单定义成功: id={}", id);
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        var entity = this.getById(id);
        if (entity == null) {
            throw new DataNotExistException("表单定义不存在");
        }
        // 删除表单定义
        if (!this.removeById(id)) {
            throw new DataSaveException("删除表单定义失败");
        }
        // 级联删除版本
        formVersionMapper.delete(
                new LambdaQueryWrapper<FormVersion>()
                        .eq(FormVersion::getFormDefinitionId, id)
        );
        log.info("删除表单定义及版本成功: id={}", id);
    }

    @Override
    @Transactional
    public void saveVersion(UUID id, FormVersionSaveFrom from) {
        var entity = this.getById(id);
        if (entity == null) {
            throw new DataNotExistException("表单定义不存在");
        }
        // 版本号自增
        int newVersion = entity.getCurrentVersion() + 1;
        // 创建新版本
        var version = new FormVersion();
        version.setFormDefinitionId(id);
        version.setFormVersion(newVersion);
        version.setRuleJson(from.getRuleJson());
        version.setOptionsJson(from.getOptionsJson());
        version.setFormJson(from.getFormJson());
        if (formVersionMapper.insert(version) <= 0) {
            throw new DataSaveException("保存表单版本失败");
        }
        // 更新当前版本号
        entity.setCurrentVersion(newVersion);
        if (!this.updateById(entity)) {
            throw new DataSaveException("更新表单版本号失败");
        }
        log.info("保存表单新版本成功: formDefinitionId={}, version={}", id, newVersion);
    }

    @Override
    public List<FormVersionVO> getVersions(UUID id) {
        var versions = formVersionMapper.selectList(
                new LambdaQueryWrapper<FormVersion>()
                        .eq(FormVersion::getFormDefinitionId, id)
                        .orderByDesc(FormVersion::getFormVersion)
        );
        return versions.stream().map(this::toVersionVO).toList();
    }

    @Override
    public FormVersionVO getVersion(UUID id, Integer version) {
        var entity = formVersionMapper.selectOne(
                new LambdaQueryWrapper<FormVersion>()
                        .eq(FormVersion::getFormDefinitionId, id)
                        .eq(FormVersion::getFormVersion, version)
        );
        if (entity == null) {
            throw new DataNotExistException("表单版本不存在");
        }
        return toVersionVO(entity);
    }

    /// 实体转VO
    private FormDefinitionVO toVO(FormDefinition entity) {
        var vo = new FormDefinitionVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCode(entity.getCode());
        vo.setCurrentVersion(entity.getCurrentVersion());
        vo.setActive(entity.getActive());
        vo.setDescription(entity.getDescription());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    /// 版本实体转VO
    private FormVersionVO toVersionVO(FormVersion entity) {
        var vo = new FormVersionVO();
        vo.setId(entity.getId());
        vo.setFormDefinitionId(entity.getFormDefinitionId());
        vo.setFormVersion(entity.getFormVersion());
        vo.setRuleJson(entity.getRuleJson());
        vo.setOptionsJson(entity.getOptionsJson());
        vo.setFormJson(entity.getFormJson());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

}
