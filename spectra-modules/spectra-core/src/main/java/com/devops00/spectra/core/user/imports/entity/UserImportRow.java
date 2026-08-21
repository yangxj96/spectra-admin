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

package com.devops00.spectra.core.user.imports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.UUID;

/**
 * 用户批量导入暂存行。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_import_row", schema = "spectra_core", autoResultMap = true)
public class UserImportRow extends BaseEntity {

    @TableField("task_id")
    private UUID taskId;

    @TableField("row_number")
    private int rowNumber;

    @TableField("row_key")
    private String rowKey;

    @TableField(value = "raw_data", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> rawData;

    @TableField(value = "normalized_data", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> normalizedData;

    private String state;

    @TableField(value = "errors", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> errors;

    @TableField("user_id")
    private UUID userId;
}
