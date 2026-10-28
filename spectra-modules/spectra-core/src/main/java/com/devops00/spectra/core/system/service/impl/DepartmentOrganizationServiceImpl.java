/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.javabean.entity.OrganizationVersion;
import com.devops00.spectra.core.system.mapper.DepartmentClosureMapper;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.system.mapper.OrganizationVersionMapper;
import com.devops00.spectra.core.system.service.DepartmentMutation;
import com.devops00.spectra.core.system.service.DepartmentOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 组织变更使用的部门持久化实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
@Service
@RequiredArgsConstructor
public class DepartmentOrganizationServiceImpl implements DepartmentOrganizationService {

    private final DepartmentMapper departmentMapper;

    private final DepartmentClosureMapper departmentClosureMapper;

    private final OrganizationVersionMapper organizationVersionMapper;

    @Override
    public DepartmentSnapshot find(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        var department = departmentMapper.selectById(departmentId);
        return department == null ? null : snapshot(department);
    }

    @Override
    public UUID create(DepartmentMutation mutation) {
        var department = new Department();
        department.setCode(mutation.getCode());
        department.setPid(mutation.getPid());
        department.setName(mutation.getName());
        department.setType(mutation.getType());
        department.setRegionId(mutation.getRegionId());
        department.setSort(mutation.getSort());
        department.setRemark(mutation.getRemark());
        if (departmentMapper.insert(department) != 1) {
            throw new DataException("新增部门失败");
        }
        return department.getId();
    }

    @Override
    public void update(DepartmentMutation mutation) {
        var update = new LambdaUpdateWrapper<Department>()
                .eq(Department::getId, mutation.getId())
                .set(Department::getPid, mutation.getPid())
                .set(Department::getName, mutation.getName())
                .set(Department::getType, mutation.getType())
                .set(Department::getRegionId, mutation.getRegionId())
                .set(Department::getSort, mutation.getSort())
                .set(Department::getRemark, mutation.getRemark());
        if (departmentMapper.update(null, update) != 1) {
            throw new DataException("编辑部门失败");
        }
    }

    @Override
    public void rebuildClosure() {
        departmentClosureMapper.clearClosure();
        departmentClosureMapper.rebuildClosure();
    }

    @Override
    public long readVersion() {
        var row = organizationVersionMapper.selectOne(new LambdaQueryWrapper<OrganizationVersion>()
                .eq(OrganizationVersion::getSingletonKey, "SYSTEM"));
        return row == null || row.getOrganizationVersion() == null ? 0L : row.getOrganizationVersion();
    }

    @Override
    public void advanceVersion(long expectedVersion) {
        var update = new LambdaUpdateWrapper<OrganizationVersion>()
                .eq(OrganizationVersion::getSingletonKey, "SYSTEM")
                .eq(OrganizationVersion::getOrganizationVersion, expectedVersion)
                .setSql("organization_version = organization_version + 1");
        if (organizationVersionMapper.update(null, update) != 1) {
            throw new DataException("organizationVersion 并发变化，组织变更已拒绝");
        }
    }

    private static DepartmentSnapshot snapshot(Department department) {
        return new DepartmentSnapshot(department.getId(), department.getPid(), department.getCode(),
                department.getName(), department.getType(), department.getRegionId(), department.getSort(),
                department.getRemark());
    }
}
