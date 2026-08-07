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

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

/// 合同履约节点视图。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Data
public class ContractMilestoneVO {
    private UUID id;
    private UUID contractId;
    private String name;
    private String milestoneType;
    private LocalDate dueDate;
    private String status;
    private UUID assigneeId;
    private Instant completedAt;
    private Instant reminderSentAt;
    private String remark;
    private Instant createdAt;
}
