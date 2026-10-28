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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 部门目录只读契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
public interface DepartmentDirectoryReadService {

    List<DepartmentDirectoryRow> listAll();

    List<DepartmentDirectoryRow> findActiveByIds(Collection<UUID> departmentIds);

    record DepartmentDirectoryRow(UUID id, UUID pid, String name, String path) {
    }
}
