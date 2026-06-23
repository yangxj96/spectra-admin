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

package com.devops00.spectra.core.system.javabean.vo;


import com.devops00.spectra.common.constant.RegionLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/// 行政区域响应VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/1/30 15:55
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionVO implements Serializable {

    /// 主键ID
    private UUID id;

    /// 区域名称
    private String name;

    /// 区域全称，如 北京市/北京市/东城区
    private String fullName;

    /// 简称
    private String shortName;

    /// 区域编码
    private String code;

    /// 区域路径，如 /110000/110100/110101
    private String path;

    /// 上级ID
    private UUID pid;

    /// 行政区划层级:1省 2地级市 3县级 4乡级 5村级
    private RegionLevel level;

    /// 状态：true-启用 false-停用
    private Boolean status;

    /// 排序
    private Integer sort;

}
