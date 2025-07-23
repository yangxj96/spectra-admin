/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.common.constant.Common;
import com.yangxj96.spectra.common.exception.DataNotExistException;
import com.yangxj96.spectra.common.utils.TreeBuilder;
import com.yangxj96.spectra.core.system.javabean.entity.Organization;
import com.yangxj96.spectra.core.system.javabean.from.OrganizationFrom;
import com.yangxj96.spectra.core.system.javabean.mapstruct.OrganizationMapstruct;
import com.yangxj96.spectra.core.system.javabean.vo.OrganizationTreeVo;
import com.yangxj96.spectra.core.system.mapper.OrganizationMapper;
import com.yangxj96.spectra.core.system.service.OrganizationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 组织机构业务层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-15
 */
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    @Resource
    private OrganizationMapstruct mapstruct;

    @Override
    public List<OrganizationTreeVo> tree() {
        var list = this.list();
        var vos = mapstruct.toTreeVOS(list);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    @Transactional
    public void created(OrganizationFrom from) {
        Organization entity = mapstruct.toEntity(from);
        this.save(entity);
    }

    @Override
    @Transactional
    public void modify(OrganizationFrom from) {
        Organization organization = this.getById(from.getId());
        if (null == organization) {
            throw new DataNotExistException("没找到组织机构信息");
        }
        Organization entity = mapstruct.toEntity(from);
        this.updateById(entity);
    }

}
