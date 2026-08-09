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

package com.devops00.spectra.oa.document.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.UUID;
import java.time.Instant;

import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// OA-文档表主表实体
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/5 23:53
@Getter
@Setter
@ToString
@TableName(value = "oa_document", schema = "spectra_oa")
@DataScope
public class Document extends BaseEntity {

    /// 所属目录ID
    @TableField("folder_id")
    private UUID folderId;

    /// 文档标题
    @TableField("title")
    private String title;

    /// 文档摘要
    @TableField("summary")
    private String summary;

    /// 文档状态（DRAFT/PUBLISHED）
    @TableField("status")
    private String status;

    /// 可见范围（PUBLIC/DEPARTMENT/PRIVATE）
    @TableField("visibility")
    private String visibility;

    /// 文档所有者
    @TableField("owner_id")
    private UUID ownerId;

    /// 发布时间
    @TableField("published_at")
    private Instant publishedAt;

    /// 所属部门ID
    @TableField("department_id")
    private UUID departmentId;
}
