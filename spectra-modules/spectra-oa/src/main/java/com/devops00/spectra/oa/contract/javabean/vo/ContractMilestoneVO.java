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

import java.time.LocalDateTime;
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

    /// 主键 ID。
    private UUID id;

    /// 合同 ID。
    private UUID contractId;

    /// 名称。
    private String name;

    /// 履约节点类型字段。
    private String milestoneType;

    /// 到期日期。
    private LocalDate dueDate;

    /// 状态。
    private String status;

    /// 负责人 ID。
    private UUID assigneeId;

    /// 完成时间。
    private LocalDateTime completedAt;

    /// 提醒发送时间。
    private LocalDateTime reminderSentAt;

    /// 备注。
    private String remark;

    /// 创建时间。
    private LocalDateTime createdAt;
}
