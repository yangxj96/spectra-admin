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

import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.system.service.DepartmentDirectoryReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 部门目录只读实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
@Service
@RequiredArgsConstructor
public class DepartmentDirectoryReadServiceImpl implements DepartmentDirectoryReadService {

    private final DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentDirectoryRow> listAll() {
        return toRows(departmentMapper.selectList(null));
    }

    @Override
    public List<DepartmentDirectoryRow> findActiveByIds(Collection<UUID> departmentIds) {
        var ids = departmentIds == null
                ? List.<UUID>of()
                : departmentIds.stream().filter(id -> id != null).distinct().toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return toRows(departmentMapper.selectActiveByIds(ids));
    }

    private static List<DepartmentDirectoryRow> toRows(List<Department> departments) {
        return departments == null
                ? List.of()
                : departments.stream()
                        .filter(department -> department != null)
                        .map(department -> new DepartmentDirectoryRow(
                                department.getId(), department.getPid(), department.getName(), department.getPath()))
                        .toList();
    }
}
