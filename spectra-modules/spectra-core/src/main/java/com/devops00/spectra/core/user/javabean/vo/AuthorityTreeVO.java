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

package com.devops00.spectra.core.user.javabean.vo;

import com.devops00.spectra.common.base.javabean.vo.Tree;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 权限树形VO
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/7/11 00:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityTreeVO implements Tree<AuthorityTreeVO>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    private UUID id;

    /**
     * 权限父级ID
     */
    private UUID pid;

    /**
     * 权限说明
     */
    private String name;

    /**
     * 权限编码
     */
    private String code;

    /**
     * Permission 允许的 Scope 模式；资源分组节点为空。
     */
    private List<String> allowedScopeModes;

    /**
     * tree必备字段,进行排序用,表中无这个字段,直接写死一个0
     */
    private Integer sort = 0;

    /**
     * 子级
     */
    private List<AuthorityTreeVO> children;
}
