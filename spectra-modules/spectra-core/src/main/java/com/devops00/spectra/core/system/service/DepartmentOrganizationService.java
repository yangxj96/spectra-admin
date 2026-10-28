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
package com.devops00.spectra.core.system.service;

import java.util.UUID;

/**
 * 组织变更使用的部门持久化契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
public interface DepartmentOrganizationService {

    DepartmentSnapshot find(UUID departmentId);

    UUID create(DepartmentMutation mutation);

    void update(DepartmentMutation mutation);

    void rebuildClosure();

    long readVersion();

    void advanceVersion(long expectedVersion);

    record DepartmentSnapshot(UUID id, UUID pid, String code, String name, String type, UUID regionId,
                              Integer sort, String remark) {
    }
}
