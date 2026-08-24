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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * 合同履约节点实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_contract_milestone", schema = "spectra_oa")
public class ContractMilestone extends BaseEntity {

    /**
     * 合同 ID。
     */
    @TableField("contract_id")
    private UUID contractId;

    /**
     * 名称。
     */
    @TableField("name")
    private String name;

    /**
     * 履约节点类型字段。
     */
    @TableField("milestone_type")
    private String milestoneType;

    /**
     * 到期日期。
     */
    @TableField("due_date")
    private Instant dueDate;

    /**
     * 状态。
     */
    @TableField("status")
    private String status;

    /**
     * 负责人 ID。
     */
    @TableField("assignee_id")
    private UUID assigneeId;

    /**
     * 完成时间。
     */
    @TableField("completed_at")
    private Instant completedAt;

    /**
     * 提醒发送时间。
     */
    @TableField("reminder_sent_at")
    private Instant reminderSentAt;

    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;
}
