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

package com.devops00.spectra.oa.notice.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

/**
 * OA 公告实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_notice", schema = "spectra_oa")
@DataScope(ignore = true)
public class Notice extends BaseEntity {

    /**
     * 部门 ID。
     */
    @TableField("department_id")
    private UUID departmentId;

    /**
     * 标题。
     */
    @TableField("title")
    private String title;

    /**
     * 摘要。
     */
    @TableField("summary")
    private String summary;

    /**
     * 内容。
     */
    @TableField("content")
    private String content;

    /**
     * 状态。
     */
    @TableField("status")
    private String status;

    /**
     * 目标类型字段。
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标部门 ID。
     */
    @TableField("target_department_id")
    private UUID targetDepartmentId;

    /**
     * 发布人 ID。
     */
    @TableField("publisher_id")
    private UUID publisherId;

    /**
     * 发布时间。
     */
    @TableField("publish_at")
    private Instant publishAt;

    /**
     * 是否要求阅读。
     */
    @TableField("required_read")
    private Boolean requiredRead;
}
