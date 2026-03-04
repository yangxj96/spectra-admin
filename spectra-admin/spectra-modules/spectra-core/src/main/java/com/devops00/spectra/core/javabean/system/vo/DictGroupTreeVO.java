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

package com.devops00.spectra.core.javabean.system.vo;

import com.devops00.spectra.common.base.javabean.vo.Tree;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/// 字典类型树VO
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/18
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictGroupTreeVO implements Tree<DictGroupTreeVO>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 数据id.
    private String id;

    /// 父级ID
    private String pid;

    /// 字典名称
    private String name;

    /// 字典编码
    private String code;

    /// 字典状态
    private Boolean state;

    /// 是否内置
    private Boolean builtin;

    /// 备注
    private String remark;

    /// tree必备字段,进行排序用,表中无这个字段,直接写死一个0
    private Integer sort = 0;

    /// 子级
    private List<DictGroupTreeVO> children = new ArrayList<>();

}
