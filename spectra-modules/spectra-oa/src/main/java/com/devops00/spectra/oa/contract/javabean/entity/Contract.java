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
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/// OA-合同表主表实体
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/5 23:53
@Getter
@Setter
@ToString
@TableName(value = "oa_contract", schema = "spectra_oa")
@DataScope
public class Contract extends BaseEntity {

    /// 合同编号
    @TableField("contract_no")
    private String contractNo;

    /// 合同标题
    @TableField("title")
    private String title;

    /// 合同类型
    @TableField("contract_type")
    private String contractType;

    /// 相对方名称
    @TableField("counterparty_name")
    private String counterpartyName;

    /// 相对方联系人
    @TableField("counterparty_contact")
    private String counterpartyContact;

    /// 合同负责人
    @TableField("owner_id")
    private UUID ownerId;

    /// 合同金额
    @TableField("amount")
    private BigDecimal amount;

    /// 币种
    @TableField("currency")
    private String currency;

    /// 生效日期
    @TableField("start_date")
    private Instant startDate;

    /// 到期日期
    @TableField("end_date")
    private Instant endDate;

    /// 合同生命周期状态
    @TableField("status")
    private String status;

    /// 签署状态
    @TableField("signing_status")
    private String signingStatus;

    /// 签署时间
    @TableField("signed_at")
    private Instant signedAt;

    /// 可见范围
    @TableField("visibility")
    private String visibility;

    /// 合同摘要
    @TableField("summary")
    private String summary;

    /// 所属部门ID
    @TableField("department_id")
    private UUID departmentId;
}
