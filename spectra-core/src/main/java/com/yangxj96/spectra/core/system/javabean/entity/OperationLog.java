/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.core.system.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yangxj96.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单表
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "db_system.t_operation_log")
public class OperationLog extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志说明
     */
    @TableField(value = "\"explain\"")
    private String explain;

    /**
     * 请求状态
     */
    @TableField(value = "\"status\"")
    private Short status;

    /**
     * 来源IP
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 请求方法
     */
    @TableField(value = "\"method\"")
    private String method;

    /**
     * 请求URL
     */
    @TableField(value = "url")
    private String url;

    /**
     * 请求参数
     */
    @TableField(value = "args")
    private String args;

    /**
     * 请求响应
     */
    @TableField(value = "\"result\"")
    private String result;

    /**
     * 耗时
     */
    @TableField(value = "time_cost")
    private Long timeCost;
}
