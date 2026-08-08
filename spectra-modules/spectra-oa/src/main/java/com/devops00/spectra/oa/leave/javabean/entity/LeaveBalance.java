/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.oa.leave.javabean.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/// 用户年度请假额度。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_leave_balance", schema = "spectra_oa")
@DataScope
public class LeaveBalance extends BaseEntity {

    /// 用户 ID。
    @TableField("user_id")
    private UUID userId;

    /// 部门 ID。
    @TableField("department_id")
    private UUID departmentId;

    /// 请假类型编码。
    @TableField("leave_type_code")
    private String leaveTypeCode;

    /// 年份字段。
    @TableField("year")
    private Integer year;

    /// 总时长。
    @TableField("total_hours")
    private BigDecimal totalHours;

    /// 已用时长。
    @TableField("used_hours")
    private BigDecimal usedHours;

    /// 已预留时长。
    @TableField("reserved_hours")
    private BigDecimal reservedHours;
}
