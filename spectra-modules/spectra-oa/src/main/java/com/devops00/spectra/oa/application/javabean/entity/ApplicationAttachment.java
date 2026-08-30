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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * OA 申请附件关联。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_application_attachment", schema = "spectra_oa")
public class ApplicationAttachment extends BaseEntity {

    /**
     * 申请 ID。
     */
    @TableField("application_id")
    private UUID applicationId;

    /** 文件资产 ID。 */
    @TableField("file_asset_id")
    private UUID fileAssetId;

    /**
     * 文件名称。
     */
    @TableField("file_name")
    private String fileName;
}
