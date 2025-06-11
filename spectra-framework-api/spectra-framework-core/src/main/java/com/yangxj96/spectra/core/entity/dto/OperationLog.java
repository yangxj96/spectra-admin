/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.core.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yangxj96.spectra.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 菜单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "db_system.t_operation_log")
public class OperationLog extends BaseEntity {
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
