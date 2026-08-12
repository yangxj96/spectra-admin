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

package com.devops00.spectra.oa.asset.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 资产分类实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_asset_category", schema = "spectra_oa")
public class AssetCategory extends BaseEntity {

    /**
     * 父级 ID。
     */
    @TableField("pid")
    private UUID pid;

    /**
     * 编码。
     */
    @TableField("code")
    private String code;

    /**
     * 名称。
     */
    @TableField("name")
    private String name;

    /**
     * 资产类型。
     */
    @TableField("asset_type")
    private String assetType;

    /**
     * 排序号。
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 是否启用。
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 描述。
     */
    @TableField("description")
    private String description;
}
