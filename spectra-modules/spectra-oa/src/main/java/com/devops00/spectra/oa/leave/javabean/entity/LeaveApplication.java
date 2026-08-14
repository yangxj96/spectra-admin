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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 请假申请业务明细。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_leave_application", schema = "spectra_oa")
@DataScope(readPermission = "oa:leave:read", writePermission = "oa:leave:update")
public class LeaveApplication extends BaseEntity {

    /**
     * 申请 ID。
     */
    @TableField("application_id")
    private UUID applicationId;

    /**
     * 部门 ID。
     */
    @TableField("department_id")
    private UUID departmentId;

    /**
     * 请假类型编码。
     */
    @TableField("leave_type_code")
    private String leaveTypeCode;

    /**
     * 开始时间。
     */
    @TableField("start_time")
    private Instant startTime;

    /**
     * 结束时间。
     */
    @TableField("end_time")
    private Instant endTime;

    /**
     * 时长（小时）。
     */
    @TableField("duration_hours")
    private BigDecimal durationHours;

    /**
     * 原因。
     */
    @TableField("reason")
    private String reason;

    /**
     * 联系地址。
     */
    @TableField("contact_address")
    private String contactAddress;
}
