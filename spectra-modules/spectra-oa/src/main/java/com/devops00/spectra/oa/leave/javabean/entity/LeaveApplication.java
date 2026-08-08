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

package com.devops00.spectra.oa.leave.javabean.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/// 请假申请业务明细。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_leave_application", schema = "spectra_oa")
@DataScope
public class LeaveApplication extends BaseEntity {
    @TableField("application_id")
    private UUID applicationId;
    @TableField("department_id")
    private UUID departmentId;
    @TableField("leave_type_code")
    private String leaveTypeCode;
    @TableField("start_time")
    private Instant startTime;
    @TableField("end_time")
    private Instant endTime;
    @TableField("duration_hours")
    private BigDecimal durationHours;
    @TableField("reason")
    private String reason;
    @TableField("contact_address")
    private String contactAddress;
}
