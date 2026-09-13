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

package com.devops00.spectra.core.security.authorization.javabean.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 封装授权边界视图相关的响应数据。
 *
 * @param permissionCode 权限编码
 * @param scopeMode      数据范围的匹配模式
 * @param resourceCode   受数据范围约束的资源编码
 * @param rules          用于限制数据范围的规则集合
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record AuthorizationBoundaryView(String permissionCode,
                                        String scopeMode,
                                        String resourceCode,
                                        List<ScopeRuleView> rules) {

    public AuthorizationBoundaryView {
        rules = rules == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(rules));
    }

    @Override
    public List<ScopeRuleView> rules() {
        return Collections.unmodifiableList(new ArrayList<>(rules));
    }

    /**
     * 封装范围规则视图相关的响应数据。
     *
     * @param ruleType           规则类型
     * @param departmentId       部门标识
     * @param includeDescendants 查询范围是否包含下级部门
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public record ScopeRuleView(String ruleType, UUID departmentId, boolean includeDescendants) {
    }
}
