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

import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// 合同文件版本实体。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Getter
@Setter
@ToString
@TableName(value = "oa_contract_version", schema = "spectra_oa")
public class ContractVersion extends BaseEntity {

    @TableField("contract_id")
    private UUID contractId;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("file_id")
    private UUID fileId;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Long fileSize;

    @TableField("content_type")
    private String contentType;

    @TableField("version_note")
    private String versionNote;

    @TableField("is_current")
    private Boolean currentVersion;
}
