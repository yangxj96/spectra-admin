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

package com.devops00.spectra.common.security.authorization;

import java.util.UUID;

/**
 * 组织层级查询端口。授权领域不直接依赖组织表实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@FunctionalInterface
public interface DepartmentHierarchy {

    /**
     * 判断 descendant 是否为 ancestor 本身或其下级。
     *
     * @param ancestor   祖先部门
     * @param descendant 后代部门
     * @return 是否属于该组织子树
     */
    boolean contains(UUID ancestor, UUID descendant);
}
