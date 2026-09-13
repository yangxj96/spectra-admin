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

package com.devops00.spectra.core.user.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import com.devops00.spectra.framework.persistence.mybatis.PgJsonbTypeHandler;
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

    /**
     * 该暂存行所属的批量导入任务 ID。
     */
    @TableField("task_id")
    private UUID taskId;

    /**
     * 该行在导入文件中的原始行号。
     */
    @TableField("row_number")
    private int rowNumber;

    /**
     * 用于稳定识别该任务中导入行的业务键。
     */
    @TableField("row_key")
    private String rowKey;

    /**
     * 从导入文件读取的原始行内容，以 JSONB 格式保存。
     */
    @TableField(value = "raw_data", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> rawData;

    /**
     * 完成字段归一化后的行内容，以 JSONB 格式保存。
     */
    @TableField(value = "normalized_data", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> normalizedData;

    /**
     * 该导入行当前的校验或执行状态。
     */
    private String state;

    /**
     * 该导入行的校验错误集合，以 JSONB 格式保存。
     */
    @TableField(value = "errors", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> errors;

    /**
     * 该行最终关联或创建的用户 ID；未匹配到用户时可为空。
     */
    @TableField("user_id")
    private UUID userId;
}
