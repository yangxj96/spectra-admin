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
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * 合同文件版本实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_contract_version", schema = "spectra_oa")
public class ContractVersion extends BaseEntity {

    /**
     * 合同 ID。
     */
    @TableField("contract_id")
    private UUID contractId;

    /**
     * 版本号。
     */
    @TableField("version_no")
    private Integer versionNo;

    /**
     * 文件 ID。
     */
    @TableField("file_id")
    private UUID fileId;

    /**
     * 文件名称。
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小。
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 内容类型。
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 版本说明。
     */
    @TableField("version_note")
    private String versionNote;

    /**
     * 当前版本字段。
     */
    @TableField("is_current")
    private Boolean currentVersion;
}
