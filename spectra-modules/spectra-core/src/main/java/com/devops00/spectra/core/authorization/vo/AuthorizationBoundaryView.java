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

package com.devops00.spectra.core.authorization.vo;

import java.util.List;
import java.util.UUID;

/**
 * 面向管理端的单个 Permission Boundary 只读视图。
 * <p>
 * Access 与 Grant 分别返回，调用方不能据此推导另一类边界。
 */
public record AuthorizationBoundaryView(String permissionCode,
                                        String scopeMode,
                                        String resourceCode,
                                        List<ScopeRuleView> rules) {

    public record ScopeRuleView(String ruleType, UUID departmentId, boolean includeDescendants) {
    }
}
