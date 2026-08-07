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

package com.devops00.spectra.oa.contract.javabean.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// 合同履约节点实体。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Getter
@Setter
@ToString
@TableName(value = "oa_contract_milestone", schema = "spectra_oa")
public class ContractMilestone extends BaseEntity {

    @TableField("contract_id")
    private UUID contractId;

    @TableField("name")
    private String name;

    @TableField("milestone_type")
    private String milestoneType;

    @TableField("due_date")
    private LocalDate dueDate;

    @TableField("status")
    private String status;

    @TableField("assignee_id")
    private UUID assigneeId;

    @TableField("completed_at")
    private Instant completedAt;

    @TableField("reminder_sent_at")
    private Instant reminderSentAt;

    @TableField("remark")
    private String remark;
}
