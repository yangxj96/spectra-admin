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

package com.devops00.spectra.oa.report.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.UUID;

import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// OA-报表表主表实体
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/5 23:53
@Getter
@Setter
@ToString
@TableName(value = "oa_report", schema = "spectra_oa")
@DataScope
public class Report extends BaseEntity {
    /// 所属部门ID
    @TableField("department_id")
    private UUID departmentId;
}
