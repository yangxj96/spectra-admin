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

package io.github.yangxj96.spectra.core.javabean.system.vo;

import io.github.yangxj96.spectra.common.base.javabean.vo.Tree;
import io.github.yangxj96.spectra.core.configure.assembler.NameFill;
import io.github.yangxj96.spectra.core.service.system.impl.RegionServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/// 组织机构树形
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/7/14
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTreeVo implements Tree<DepartmentTreeVo>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /// 上级ID
    private String pid;

    /// 名称
    private String name;

    /// 编码
    private String code;

    /// 组织机构类型
    private Short type;

    /// 行政区划ID
    private String regionId;

    /// 行政区划名称
    @NameFill(lookup = RegionServiceImpl.class, sourceField = "regionId")
    private String regionName;

    /// 路径
    private String path;

    /// 备注
    private String remark;

    /// tree必备字段,进行排序用,表中无这个字段,直接写死一个0
    private Integer sort = 0;

    /// 下级菜单
    private List<DepartmentTreeVo> children;
}
