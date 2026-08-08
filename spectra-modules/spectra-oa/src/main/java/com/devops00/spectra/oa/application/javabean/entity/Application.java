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

package com.devops00.spectra.oa.application.javabean.entity;

import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/// OA 通用申请主表。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_application", schema = "spectra_oa")
@DataScope
public class Application extends BaseEntity {

    @TableField("application_no")
    private String applicationNo;

    @TableField("type_code")
    private String typeCode;

    @TableField("biz_id")
    private UUID bizId;

    @TableField("applicant_id")
    private UUID applicantId;

    @TableField("department_id")
    private UUID departmentId;

    @TableField("title")
    private String title;

    @TableField("status")
    private String status;

    @TableField("process_instance_id")
    private String processInstanceId;

    @TableField("submitted_at")
    private Instant submittedAt;

    @TableField("completed_at")
    private Instant completedAt;

    @TableField("reject_reason")
    private String rejectReason;
}
