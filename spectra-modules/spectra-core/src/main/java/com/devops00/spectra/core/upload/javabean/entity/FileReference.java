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

package com.devops00.spectra.core.upload.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * 业务对象与文件资产之间的引用关系。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_reference", schema = "spectra_core")
public class FileReference extends BaseEntity {

    /**
     * 被业务对象引用的文件资产 ID。
     */
    @TableField("file_asset_id")
    private UUID fileAssetId;

    /**
     * 引用该文件的业务对象类型编码。
     */
    @TableField("reference_type")
    private String referenceType;

    /**
     * 引用该文件的业务对象 ID。
     */
    @TableField("reference_id")
    private UUID referenceId;

    /**
     * 文件在该业务对象中的用途编码。
     */
    @TableField("purpose")
    private String purpose;

    /**
     * 在该业务引用中展示给用户的文件名。
     */
    @TableField("display_name")
    private String displayName;
}
