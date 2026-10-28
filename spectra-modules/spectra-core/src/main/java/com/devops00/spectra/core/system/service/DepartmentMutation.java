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

import lombok.Data;

import java.util.UUID;

/**
 * 部门组织变更写入数据。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
@Data
public class DepartmentMutation {

    private UUID id;

    private UUID pid;

    private String code;

    private String name;

    private String type;

    private UUID regionId;

    private Integer sort;

    private String remark;
}
