/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.security.authorization.javabean.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部门合并或部分拆分的预览摘要。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
@Data
public class DepartmentRestructurePreviewVO {

    private String operation;

    private String newDepartmentName;

    private String newDepartmentType;

    private UUID newDepartmentParentId;

    private UUID newDepartmentRegionId;

    private int sourceDepartmentCount;

    private int movedDepartmentCount;

    private int affectedUserCount;

    private int primaryDepartmentCount;

    private int associatedDepartmentCount;

    private int deduplicatedAssociatedCount;

    private int affectedAssignmentCount;

    private int affectedProfileCount;

    private int accessRuleCount;

    private int grantRuleCount;

    private int profileAccessScopeCount;

    private int profileGrantScopeCount;

    private boolean authorizationBoundariesChanged;

    private boolean expandsEffectiveAuthority;

    private String effectiveScopeChangeSummary;

    private String historicalDataSummary;

    private Long expectedOrganizationVersion;

    private Long afterOrganizationVersion;

    private LocalDateTime expiresAt;

    private String previewToken;
}
