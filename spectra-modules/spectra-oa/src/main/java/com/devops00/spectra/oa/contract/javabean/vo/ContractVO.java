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
import java.time.LocalDateTime;
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

    /// 主键 ID。
    private UUID id;

    /// 合同编号。
    private String contractNo;

    /// 标题。
    private String title;

    /// 合同类型字段。
    private String contractType;

    /// 对方名称。
    private String counterpartyName;

    /// 对方联系人。
    private String counterpartyContact;

    /// 所有者 ID。
    private UUID ownerId;

    /// 部门 ID。
    private UUID departmentId;

    /// 金额。
    private BigDecimal amount;

    /// 币种。
    private String currency;

    /// 开始日期。
    private LocalDate startDate;

    /// 结束日期。
    private LocalDate endDate;

    /// 状态。
    private String status;

    /// 签署状态。
    private String signingStatus;

    /// 签署时间。
    private LocalDateTime signedAt;

    /// 可见范围。
    private String visibility;

    /// 摘要。
    private String summary;

    /// 创建时间。
    private LocalDateTime createdAt;

    /// 更新时间。
    private LocalDateTime updatedAt;

    /// 当前版本字段。
    private ContractVersionVO currentVersion;
    private List<ContractVersionVO> versions = List.of();
    private List<ContractMilestoneVO> milestones = List.of();
}
