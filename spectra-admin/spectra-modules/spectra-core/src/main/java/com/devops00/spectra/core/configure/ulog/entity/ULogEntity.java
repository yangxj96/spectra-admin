/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.core.configure.ulog.entity;

import com.devops00.spectra.core.configure.ulog.enums.SysLogType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

/// 切面中存储的数据的实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/27
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ULogEntity implements Serializable {

    /// 日志类型
    private SysLogType type;

    /// 操作说明
    private String explain;

    /// 请求参数
    private String args;

    /// 请求IP
    private String ip;

    /// 请求方法
    private String method;

    /// 请求URL
    private String url;

    /// 响应状态
    private Short status;

    /// 响应内容
    @Nullable
    private String result;

    /// 耗时
    private Long timeCost;

    /// 当前用户ID
    @Nullable
    private String currentId;
}
