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

package com.devops00.spectra.oa.contract.javabean.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/// 合同台账视图。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Data
public class ContractVO {
    private UUID id;
    private String contractNo;
    private String title;
    private String contractType;
    private String counterpartyName;
    private String counterpartyContact;
    private UUID ownerId;
    private UUID departmentId;
    private BigDecimal amount;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String signingStatus;
    private Instant signedAt;
    private String visibility;
    private String summary;
    private Instant createdAt;
    private Instant updatedAt;
    private ContractVersionVO currentVersion;
    private List<ContractVersionVO> versions = List.of();
    private List<ContractMilestoneVO> milestones = List.of();
}
